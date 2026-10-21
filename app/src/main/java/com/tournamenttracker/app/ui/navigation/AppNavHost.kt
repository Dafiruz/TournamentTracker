package com.tournamenttracker.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tournamenttracker.app.ui.create.CreateTournamentScreen
import com.tournamenttracker.app.ui.home.HomeScreen
import com.tournamenttracker.app.ui.tournament.TournamentScreen

object Routes {
    const val HOME = "home"
    const val CREATE = "create"
    const val TOURNAMENT = "tournament/{tournamentId}"

    fun tournament(tournamentId: Long) = "tournament/$tournamentId"
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onOpenTournament = { id -> navController.navigate(Routes.tournament(id)) },
                onCreateTournament = { navController.navigate(Routes.CREATE) },
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
            )
        }
    }
}
