package com.tournamenttracker.app.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tournamenttracker.app.ui.create.CreateTournamentScreen
import com.tournamenttracker.app.ui.home.HomeScreen
import com.tournamenttracker.app.ui.lifetracker.LifeTrackerScreen
import com.tournamenttracker.app.ui.tournament.TournamentScreen

object Routes {
    const val HOME = "home"
    const val CREATE = "create"
    const val TOURNAMENT = "tournament/{tournamentId}"
    const val LIFE_TRACKER = "lifetracker?p1={p1}&p2={p2}"

    fun tournament(tournamentId: Long) = "tournament/$tournamentId"

    fun lifeTracker(player1: String?, player2: String?) =
        "lifetracker?p1=${Uri.encode(player1 ?: "")}&p2=${Uri.encode(player2 ?: "")}"
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onOpenTournament = { id -> navController.navigate(Routes.tournament(id)) },
                onCreateTournament = { navController.navigate(Routes.CREATE) },
                onOpenLifeTracker = {
                    navController.navigate(Routes.lifeTracker(null, null))
                },
            )
        }
        composable(Routes.CREATE) {
            CreateTournamentScreen(
                onBack = { navController.popBackStack() },
                onStarted = { id ->
                    navController.navigate(Routes.tournament(id)) {
                        popUpTo(Routes.HOME)
                    }
                },
            )
        }
        composable(
            route = Routes.TOURNAMENT,
            arguments = listOf(navArgument("tournamentId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val tournamentId = backStackEntry.arguments?.getLong("tournamentId")
                ?: return@composable
            TournamentScreen(
                tournamentId = tournamentId,
                onBack = { navController.popBackStack() },
                onOpenLifeTracker = { player1, player2 ->
                    navController.navigate(Routes.lifeTracker(player1, player2))
                },
            )
        }
        composable(
            route = Routes.LIFE_TRACKER,
            arguments = listOf(
                navArgument("p1") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("p2") {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { backStackEntry ->
            LifeTrackerScreen(
                player1Name = backStackEntry.arguments
                    ?.getString("p1")
                    ?.takeIf { it.isNotBlank() },
                player2Name = backStackEntry.arguments
                    ?.getString("p2")
                    ?.takeIf { it.isNotBlank() },
                onBack = { navController.popBackStack() },
            )
        }
    }
}
