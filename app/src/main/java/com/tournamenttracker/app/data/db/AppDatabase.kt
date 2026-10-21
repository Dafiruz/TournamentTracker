package com.tournamenttracker.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.tournamenttracker.app.data.entity.DeckEntity
import com.tournamenttracker.app.data.entity.MatchEntity
import com.tournamenttracker.app.data.entity.PlayerEntity
import com.tournamenttracker.app.data.entity.TournamentEntity
import com.tournamenttracker.app.data.entity.TournamentStatus
import com.tournamenttracker.app.domain.model.Round

class Converters {
    @TypeConverter
    fun roundToString(round: Round): String = round.name

    @TypeConverter
    fun stringToRound(value: String): Round = Round.valueOf(value)

    @TypeConverter
    fun statusToString(status: TournamentStatus): String = status.name

    @TypeConverter
    fun stringToStatus(value: String): TournamentStatus = TournamentStatus.valueOf(value)
}

@Database(
    entities = [
        TournamentEntity::class,
        PlayerEntity::class,
        DeckEntity::class,
        MatchEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun tournamentDao(): TournamentDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tournament_tracker.db",
                ).build().also { instance = it }
            }
    }
}
