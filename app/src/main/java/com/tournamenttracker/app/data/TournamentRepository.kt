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

        dao.insertMatches(
            bracket.firstRoundMatches.map { match ->
                val player1Id = match.player1?.let { playerIdByName[it.name] }
                val player2Id = match.player2?.let { playerIdByName[it.name] }
                MatchEntity(
                    tournamentId = tournamentId,
                    round = match.round,
                    matchIndex = match.index,
                    player1Id = player1Id,
                    player2Id = player2Id,
                    // Bye matches are won by the present player.
                    winnerId = if (match.isBye) player1Id ?: player2Id else null,
                )
            }
        )
        tournamentId
    }

    /** Runs [block] inside a database transaction. */
    suspend fun <T> withTransaction(block: suspend () -> T): T =
        db.withTransaction { block() }
}
