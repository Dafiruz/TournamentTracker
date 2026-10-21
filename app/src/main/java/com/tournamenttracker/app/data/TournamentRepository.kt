package com.tournamenttracker.app.data

import com.tournamenttracker.app.data.db.AppDatabase
import com.tournamenttracker.app.data.db.TournamentDao
import com.tournamenttracker.app.data.db.TournamentWithDetails
import com.tournamenttracker.app.data.entity.TournamentStatus
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

    /** Runs [block] inside a database transaction. */
    suspend fun <T> withTransaction(block: suspend () -> T): T =
        db.withTransaction { block() }
}
