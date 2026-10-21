package com.tournamenttracker.app.ui.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tournamenttracker.app.domain.BracketGenerator
import com.tournamenttracker.app.domain.model.DraftMatch
import com.tournamenttracker.app.domain.model.DraftPlayer
import com.tournamenttracker.app.domain.model.Round

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTournamentScreen(
    onBack: () -> Unit,
    onStarted: (Long) -> Unit,
    viewModel: CreateViewModel = viewModel(factory = CreateViewModel.Factory),
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Tournament") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            if (state.preview == null) {
                EditingContent(state = state, viewModel = viewModel)
            } else {
                PreviewContent(
                    preview = state.preview!!,
                    hasDecks = state.decks.isNotEmpty(),
                    onRerollDecks = viewModel::rerollDecks,
                    onRerollMatches = viewModel::rerollMatches,
                    onStart = { viewModel.start(onStarted) },
                )
            }
        }
    }
}

@Composable
private fun EditingContent(
    state: CreateState,
    viewModel: CreateViewModel,
) {
    OutlinedTextField(
        value = state.name,
        onValueChange = viewModel::setName,
        label = { Text("Tournament name") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )

    NameListSection(
        title = "Players",
        subtitle = "${state.players.size} / ${BracketGenerator.MAX_PLAYERS}",
        placeholder = "Add player",
        items = state.players,
        onRemove = viewModel::removePlayer,
        onAdd = viewModel::addPlayer,
    )

    NameListSection(
        title = "Decks",
        subtitle = "${state.decks.size} (optional)",
        placeholder = "Add deck",
        items = state.decks,
        onRemove = viewModel::removeDeck,
        onAdd = viewModel::addDeck,
    )

    Spacer(Modifier.height(24.dp))
    Button(
        onClick = viewModel::randomize,
        enabled = state.name.isNotBlank() &&
            state.players.size in BracketGenerator.MIN_PLAYERS..BracketGenerator.MAX_PLAYERS,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("Randomize decks & matches")
    }
}

@Composable
private fun NameListSection(
    title: String,
    subtitle: String,
    placeholder: String,
    items: List<String>,
    onRemove: (String) -> Unit,
    onAdd: (String) -> Unit,
) {
    var input by remember { mutableStateOf("") }

    Spacer(Modifier.height(24.dp))
    Text(title, style = MaterialTheme.typography.titleMedium)
    Text(subtitle, style = MaterialTheme.typography.bodySmall)

    if (items.isNotEmpty()) {
        Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items.forEach { name ->
                AssistChip(
                    onClick = {},
                    label = { Text(name) },
                    trailingIcon = {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Remove $name",
                            modifier = Modifier
                                .clickable { onRemove(name) }
                                .size(18.dp),
                        )
                    },
                )
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            placeholder = { Text(placeholder) },
            singleLine = true,
            modifier = Modifier.weight(1f),
        )
        IconButton(
            onClick = {
                onAdd(input)
                input = ""
            },
            enabled = input.isNotBlank(),
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add")
        }
    }
}

@Composable
private fun PreviewContent(
    preview: Preview,
    hasDecks: Boolean,
    onRerollDecks: () -> Unit,
    onRerollMatches: () -> Unit,
    onStart: () -> Unit,
) {
    if (hasDecks) {
        Text("Deck assignments", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        preview.assignments.sortedBy { it.name }.forEach { player ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(player.name, Modifier.weight(1f))
                Text(
                    player.deckName ?: "\u2014",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))
    }

    Text("Bracket", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    preview.bracket.rounds.forEachIndexed { roundIndex, round ->
        Text(
            round.displayName,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
        )
        if (roundIndex == 0) {
            preview.bracket.firstRoundMatches.forEach { match ->
                PreviewMatchRow(match)
            }
        } else {
            Text(
                text = "${preview.bracket.size / (1 shl (roundIndex + 1))} match(es) \u2014 winners advance",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (preview.mirrorCount > 0) {
        Spacer(Modifier.height(8.dp))
        Text(
            "Contains ${preview.mirrorCount} mirror match(es) \u2014 unavoidable with these decks.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
    }

    Spacer(Modifier.height(24.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (hasDecks) {
            OutlinedButton(onClick = onRerollDecks, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Decks")
            }
        }
        OutlinedButton(onClick = onRerollMatches, modifier = Modifier.weight(1f)) {
            Icon(Icons.Filled.Refresh, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("Matches")
        }
    }
    Spacer(Modifier.height(8.dp))
    Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
        Text("Start Tournament")
    }
}

@Composable
private fun PreviewMatchRow(match: DraftMatch) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PreviewPlayer(player = match.player1, isBye = match.player1 == null, Modifier.weight(1f))
        Text(
            "vs",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        PreviewPlayer(player = match.player2, isBye = match.player2 == null, Modifier.weight(1f))
    }
}

@Composable
private fun PreviewPlayer(
    player: DraftPlayer?,
    isBye: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        if (isBye) {
            Text(
                "Bye",
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Text(player!!.name, style = MaterialTheme.typography.bodyMedium)
            player.deckName?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
