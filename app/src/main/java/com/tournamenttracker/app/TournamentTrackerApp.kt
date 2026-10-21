package com.tournamenttracker.app

import android.app.Application
import com.tournamenttracker.app.data.TournamentRepository
import com.tournamenttracker.app.data.db.AppDatabase

class TournamentTrackerApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    val repository: TournamentRepository by lazy {
        TournamentRepository(database.tournamentDao(), database)
    }
}
