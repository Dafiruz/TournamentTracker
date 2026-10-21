package com.tournamenttracker.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tournamenttracker.app.ui.navigation.AppNavHost
import com.tournamenttracker.app.ui.theme.TournamentTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Keep the screen awake while tracking life totals or running a bracket.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            TournamentTrackerTheme {
                AppNavHost()
            }
        }
    }
}
