package com.tournamenttracker.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.tournamenttracker.app.data.entity.DeckEntity
import com.tournamenttracker.app.data.entity.MatchEntity
import com.tournamenttracker.app.data.entity.PlayerEntity
import com.tournamenttracker.app.data.entity.TournamentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TournamentDao {

    @Transaction
    @Query("SELECT * FROM tournaments ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<TournamentWithDetails>>

    @Transaction
    @Query("SELECT * FROM tournaments WHERE id = :tournamentId")
    fun observeTournament(tournamentId: Long): Flow<TournamentWithDetails?>

    @Transaction
    @Query("SELECT * FROM tournaments WHERE id = :tournamentId")
    suspend fun getTournament(tournamentId: Long): TournamentWithDetails?

    @Insert
    suspend fun insertTournament(tournament: TournamentEntity): Long

    @Insert
    suspend fun insertPlayers(players: List<PlayerEntity>): List<Long>

    @Insert
    suspend fun insertDecks(decks: List<DeckEntity>): List<Long>

    @Insert
    suspend fun insertMatches(matches: List<MatchEntity>)

    @Update
    suspend fun updateTournament(tournament: TournamentEntity)

    @Update
    suspend fun updateMatches(matches: List<MatchEntity>)

    @Query("DELETE FROM tournaments WHERE id = :tournamentId")
    suspend fun deleteTournament(tournamentId: Long)
}
