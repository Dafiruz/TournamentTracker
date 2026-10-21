package com.tournamenttracker.app.ui.tournament

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tournamenttracker.app.data.db.TournamentWithDetails
import com.tournamenttracker.app.data.entity.DeckEntity
import com.tournamenttracker.app.data.entity.MatchEntity
import com.tournamenttracker.app.data.entity.PlayerEntity
import com.tournamenttracker.app.data.entity.TournamentStatus
import com.tournamenttracker.app.ui.components.StatusChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentScreen(
    tournamentId: Long,
    onBack: () -> Unit,
    onOpenLifeTracker: (String, String) -> Unit,
    viewModel: TournamentViewModel = viewModel(factory = TournamentViewModel.Factory),
) {
    val entry by viewModel.tournament.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    val current = entry
    val tournament = current?.tournament
    val status = tournament?.status

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tournament?.name ?: "Tournament") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (status == TournamentStatus.IN_PROGRESS) {
                        TextButton(onClick = { viewModel.setStatus(TournamentStatus.PAUSED) }) {
                            Text("Pause")
                        }
                    } else if (status == TournamentStatus.PAUSED) {
                        TextButton(onClick = { viewModel.setStatus(TournamentStatus.IN_PROGRESS) }) {
                            Text("Resume")
                        }
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete tournament")
                    }
                },
            )
        },
    ) { innerPadding ->
        if (current == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Tournament not found.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            TournamentContent(
                entry = current!!,
                onPickWinner = viewModel::pickWinner,
                onOpenLifeTracker = onOpenLifeTracker,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete tournament?") },
            text = { Text("This permanently removes the tournament and its history.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.delete(onDeleted = onBack)
                    },
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun TournamentContent(
    entry: TournamentWithDetails,
    onPickWinner: (Long, Long) -> Unit,
    onOpenLifeTracker: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tournament = entry.tournament
    val playerById = entry.players.associateBy { it.id }
    val deckById = entry.decks.associateBy { it.id }
    val editable = tournament.status == TournamentStatus.IN_PROGRESS

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${entry.players.size} players \u00b7 ${entry.decks.size} decks",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                StatusChip(tournament.status)
            }
        }

        tournament.championId?.let { championId ->
            item {
                playerById[championId]?.let { champion ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "\uD83C\uDFC6 Champion: ${champion.name}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(16.dp),
                        )
                    }
                }
            }
        }

        if (editable) {
            item {
                Text(
                    "Tap a player to set them as the match winner.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        entry.matches
            .groupBy { it.roundNumber }
            .toSortedMap()
            .forEach { (_, matches) ->
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        matches.first().round.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                items(matches.sortedBy { it.matchIndex }) { match ->
                    val player1 = match.player1Id?.let { playerById[it] }
                    val player2 = match.player2Id?.let { playerById[it] }
                    MatchCard(
                        match = match,
                        playerById = playerById,
                        deckById = deckById,
                        editable = editable,
                        onPickWinner = { playerId -> onPickWinner(match.id, playerId) },
                        onOpenLifeTracker = if (player1 != null && player2 != null) {
                            { onOpenLifeTracker(player1.name, player2.name) }
                        } else {
                            null
                        },
                    )
                }
            }
    }
}

@Composable
private fun MatchCard(
    match: MatchEntity,
    playerById: Map<Long, PlayerEntity>,
    deckById: Map<Long, DeckEntity>,
    editable: Boolean,
    onPickWinner: (Long) -> Unit,
    onOpenLifeTracker: (() -> Unit)?,
) {
    val player1 = match.player1Id?.let { playerById[it] }
    val player2 = match.player2Id?.let { playerById[it] }
    val hasBye = (match.player1Id == null) != (match.player2Id == null)

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MatchPlayerSlot(
                player = player1,
                deckName = player1?.assignedDeckId?.let { deckById[it]?.name },
                isWinner = player1 != null && match.winnerId == player1.id,
                emptyLabel = if (hasBye) "Bye" else "TBD",
                editable = editable,
                onClick = { player1?.let { onPickWinner(it.id) } },
                modifier = Modifier.weight(1f),
            )
            Text(
                "vs",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            MatchPlayerSlot(
                player = player2,
                deckName = player2?.assignedDeckId?.let { deckById[it]?.name },
                isWinner = player2 != null && match.winnerId == player2.id,
                emptyLabel = if (hasBye) "Bye" else "TBD",
                editable = editable,
                onClick = { player2?.let { onPickWinner(it.id) } },
                modifier = Modifier.weight(1f),
            )
            if (onOpenLifeTracker != null) {
                IconButton(onClick = onOpenLifeTracker) {
                    Icon(
                        Icons.Filled.FavoriteBorder,
                        contentDescription = "Open life tracker",
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun MatchPlayerSlot(
    player: PlayerEntity?,
    deckName: String?,
    isWinner: Boolean,
    emptyLabel: String,
    editable: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = if (isWinner) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .then(
                if (editable && player != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                player?.name ?: emptyLabel,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal,
                fontStyle = if (player == null) FontStyle.Italic else FontStyle.Normal,
                color = if (player == null) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.weight(1f, fill = false),
            )
            if (isWinner) {
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "Winner",
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        deckName?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
