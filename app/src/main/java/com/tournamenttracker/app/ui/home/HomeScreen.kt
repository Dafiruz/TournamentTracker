package com.tournamenttracker.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tournamenttracker.app.data.db.TournamentWithDetails
import com.tournamenttracker.app.ui.components.StatusChip
import java.text.DateFormat
import java.util.Date

@Composable
fun HomeScreen(
    onOpenTournament: (Long) -> Unit,
    onCreateTournament: () -> Unit,
    onOpenLifeTracker: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
) {
    val tournaments by viewModel.tournaments.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tournament Tracker") },
                actions = {
                    IconButton(onClick = onOpenLifeTracker) {
                        Icon(
                            Icons.Filled.Favorite,
                            contentDescription = "Life tracker",
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateTournament) {
                Icon(Icons.Filled.Add, contentDescription = "New tournament")
            }
        },
    ) { innerPadding ->
        if (tournaments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No tournaments yet.\nTap + to create one.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(tournaments, key = { it.tournament.id }) { entry ->
                    TournamentCard(entry, onClick = { onOpenTournament(entry.tournament.id) })
                }
            }
        }
    }
}

@Composable
private fun TournamentCard(entry: TournamentWithDetails, onClick: () -> Unit) {
    val tournament = entry.tournament
    ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    tournament.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                StatusChip(tournament.status)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "${entry.players.size} players \u00b7 ${formatDate(tournament.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            tournament.championId?.let { championId ->
                entry.players.firstOrNull { it.id == championId }?.let { champion ->
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Champion: ${champion.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String =
    DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(timestamp))
