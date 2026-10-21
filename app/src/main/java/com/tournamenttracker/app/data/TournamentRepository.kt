package com.tournamenttracker.app.data

import com.tournamenttracker.app.data.db.AppDatabase
import com.tournamenttracker.app.data.db.TournamentDao
import com.tournamenttracker.app.data.db.TournamentWithDetails
import com.tournamenttracker.app.data.entity.DeckEntity
import com.tournamenttracker.app.data.entity.MatchEntity
import com.tournamenttracker.app.data.entity.PlayerEntity
import com.tournamenttracker.app.data.entity.TournamentEntity
import com.tournamenttracker.app.data.entity.TournamentStatus
import com.tournamenttracker.app.domain.model.DraftBracket
import com.tournamenttracker.app.domain.model.Round
import kotlinx.coroutines.flow.Flow

class TournamentRepository(
    private val dao: TournamentDao,
    private val db: AppDatabase,
) {

    fun observeAll(): Flow<List<TournamentWithDetails>> = dao.observeAll()

    fun observeTournament(tournamentId: Long): Flow<TournamentWithDetails?> =
        dao.observeTournament(tournamentId)

    suspend fun getTournament(tournamentId: Long): TournamentWithDetails? =
        dao.getTournament(tournamentId)

    suspend fun setStatus(tournamentId: Long, status: TournamentStatus) {
        val tournament = dao.getTournament(tournamentId)?.tournament ?: return
        dao.updateTournament(
            tournament.copy(status = status, updatedAt = System.currentTimeMillis())
        )
    }

    suspend fun deleteTournament(tournamentId: Long) {
        dao.deleteTournament(tournamentId)
    }

    /**
     * Persists a generated bracket: tournament, decks, players (with their
     * deck assignments) and all first-round matches. Bye matches are won by
     * the present player right away.
     */
    suspend fun createTournament(name: String, bracket: DraftBracket): Long = withTransaction {
        val now = System.currentTimeMillis()
        val tournamentId = dao.insertTournament(
            TournamentEntity(
                name = name.trim(),
                status = TournamentStatus.IN_PROGRESS,
                createdAt = now,
                updatedAt = now,
                bracketSize = bracket.size,
            )
        )

        val allPlayers = bracket.firstRoundMatches
            .flatMap { listOfNotNull(it.player1, it.player2) }

        val deckNames = allPlayers.mapNotNull { it.deckName }.distinct()
        val deckIdByName: Map<String, Long> = if (deckNames.isEmpty()) {
            emptyMap()
        } else {
            val deckIds = dao.insertDecks(
                deckNames.map { DeckEntity(tournamentId = tournamentId, name = it) }
            )
            deckNames.zip(deckIds).toMap()
        }

        val playerIdByName = mutableMapOf<String, Long>()
        allPlayers.forEach { player ->
            if (player.name !in playerIdByName) {
                val playerId = dao.insertPlayers(
                    listOf(
                        PlayerEntity(
                            tournamentId = tournamentId,
                            name = player.name,
                            assignedDeckId = player.deckName?.let { deckIdByName[it] },
                        )
                    )
                ).first()
                playerIdByName[player.name] = playerId
            }
        }

        val matches = mutableListOf<MatchEntity>()
        bracket.firstRoundMatches.forEach { match ->
            val player1Id = match.player1?.let { playerIdByName[it.name] }
            val player2Id = match.player2?.let { playerIdByName[it.name] }
            matches.add(
                MatchEntity(
                    tournamentId = tournamentId,
                    roundNumber = 0,
                    round = match.round,
                    matchIndex = match.index,
                    player1Id = player1Id,
                    player2Id = player2Id,
                    // Bye matches are won by the present player.
                    winnerId = if (match.isBye) player1Id ?: player2Id else null,
                )
            )
        }
        // Later rounds start empty; winners cascade into them.
        for ((roundNumber, round) in bracket.rounds.withIndex()) {
            if (roundNumber == 0) continue
            val slotCount = bracket.size / (1 shl (roundNumber + 1))
            for (index in 0 until slotCount) {
                matches.add(
                    MatchEntity(
                        tournamentId = tournamentId,
                        roundNumber = roundNumber,
                        round = round,
                        matchIndex = index,
                    )
                )
            }
        }
        dao.insertMatches(matches)
        tournamentId
    }

    /**
     * Sets (or clears, when tapped again) the winner of a match and cascades
     * the change up the bracket: the winner takes their slot in the next
     * round, and winners that no longer have a valid participant are cleared.
     * Completes the tournament once the final has a winner.
     */
    suspend fun setWinner(tournamentId: Long, matchId: Long, winnerId: Long) {
        withTransaction {
            val snapshot = dao.getTournament(tournamentId) ?: return@withTransaction
            if (snapshot.tournament.status != TournamentStatus.IN_PROGRESS) return@withTransaction

            val match = snapshot.matches.firstOrNull { it.id == matchId } ?: return@withTransaction
            if (winnerId != match.player1Id && winnerId != match.player2Id) return@withTransaction

            // Tapping the current winner clears the result.
            val newWinnerId = if (match.winnerId == winnerId) null else winnerId

            var current = match.copy(winnerId = newWinnerId)
            val changed = mutableListOf(current)

            while (true) {
                val parent = snapshot.matches.firstOrNull {
                    it.roundNumber == current.roundNumber + 1 &&
                        it.matchIndex == current.matchIndex / 2
                } ?: break

                val parentUpdated = if (current.matchIndex % 2 == 0) {
                    parent.copy(player1Id = current.winnerId)
                } else {
                    parent.copy(player2Id = current.winnerId)
                }.let { updated ->
                    if (updated.winnerId != null &&
                        updated.winnerId != updated.player1Id &&
                        updated.winnerId != updated.player2Id
                    ) {
                        updated.copy(winnerId = null)
                    } else {
                        updated
                    }
                }

                changed.add(parentUpdated)
                current = parentUpdated
            }

            dao.updateMatches(changed)

            // Complete (or reopen) the tournament based on the final.
            val finalMatch = snapshot.matches.firstOrNull { it.round == Round.FINAL } ?: return@withTransaction
            val finalResult = changed.firstOrNull { it.id == finalMatch.id } ?: finalMatch
            if (finalResult.winnerId != null &&
                snapshot.tournament.status == TournamentStatus.IN_PROGRESS
            ) {
                dao.updateTournament(
                    snapshot.tournament.copy(
                        status = TournamentStatus.DONE,
                        championId = finalResult.winnerId,
                        updatedAt = System.currentTimeMillis(),
                    )
                )
            } else if (finalResult.winnerId == null &&
                snapshot.tournament.status == TournamentStatus.DONE
            ) {
                dao.updateTournament(
                    snapshot.tournament.copy(
                        status = TournamentStatus.IN_PROGRESS,
                        championId = null,
                        updatedAt = System.currentTimeMillis(),
                    )
                )
            }
        }
    }

    /** Runs [block] inside a database transaction. */
    suspend fun <T> withTransaction(block: suspend () -> T): T =
        db.withTransaction { block() }
}
