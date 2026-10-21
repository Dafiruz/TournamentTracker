package com.tournamenttracker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tournamenttracker.app.ui.navigation.AppNavHost
import com.tournamenttracker.app.ui.theme.TournamentTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TournamentTrackerTheme {
                AppNavHost()
            }
        }
    }
}
