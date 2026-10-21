package com.tournamenttracker.app.ui.lifetracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** A simple two-player life counter. No commander damage, no poison. */
@Composable
fun LifeTrackerScreen(
    player1Name: String?,
    player2Name: String?,
    onBack: () -> Unit,
) {
    var started by remember { mutableStateOf(false) }
    var name1 by remember { mutableStateOf(player1Name ?: "") }
    var name2 by remember { mutableStateOf(player2Name ?: "") }
    var startingLife by remember { mutableIntStateOf(20) }
    var life1 by remember { mutableIntStateOf(0) }
    var life2 by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (started) "Life Tracker" else "New Match") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (started) {
                        IconButton(
                            onClick = {
                                life1 = startingLife
                                life2 = startingLife
                            },
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Reset life")
                        }
                        TextButton(onClick = { started = false }) {
                            Text("Setup")
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            if (!started) {
                OutlinedTextField(
                    value = name1,
                    onValueChange = { name1 = it },
                    label = { Text("Player 1") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = name2,
                    onValueChange = { name2 = it },
                    label = { Text("Player 2") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(24.dp))
                Text("Starting life", style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (startingLife > 1) startingLife-- },
                        enabled = startingLife > 1,
                    ) {
                        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Decrease")
                    }
                    Text(
                        "$startingLife",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                    IconButton(
                        onClick = { if (startingLife < 999) startingLife++ },
                        enabled = startingLife < 999,
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Increase")
                    }
                    Spacer(Modifier.size(16.dp))
                    FilterChip(
                        selected = startingLife == 20,
                        onClick = { startingLife = 20 },
                        label = { Text("20") },
                    )
                    Spacer(Modifier.size(8.dp))
                    FilterChip(
                        selected = startingLife == 40,
                        onClick = { startingLife = 40 },
                        label = { Text("40") },
                    )
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        life1 = startingLife
                        life2 = startingLife
                        started = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Start Match")
                }
            } else {
                LifePanel(
                    name = name1.ifBlank { "Player 1" },
                    life = life1,
                    onDelta = { life1 += it },
                )
                Spacer(Modifier.height(12.dp))
                LifePanel(
                    name = name2.ifBlank { "Player 2" },
                    life = life2,
                    onDelta = { life2 += it },
                )
            }
        }
    }
}

@Composable
private fun LifePanel(
    name: String,
    life: Int,
    onDelta: (Int) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "$life",
                style = MaterialTheme.typography.displayLarge,
                color = if (life <= 0) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.padding(vertical = 8.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    8.dp,
                    Alignment.CenterHorizontally,
                ),
            ) {
                LifeButton("-5") { onDelta(-5) }
                LifeButton("-1") { onDelta(-1) }
                LifeButton("+1") { onDelta(+1) }
                LifeButton("+5") { onDelta(+5) }
            }
        }
    }
}

@Composable
private fun LifeButton(label: String, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.size(64.dp),
        contentPadding = PaddingValues(0.dp),
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium)
    }
}
