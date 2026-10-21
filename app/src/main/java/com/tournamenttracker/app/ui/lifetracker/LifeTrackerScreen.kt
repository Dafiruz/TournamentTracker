package com.tournamenttracker.app.ui.lifetracker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A simple two-player life counter. No commander damage, no poison.
 *
 * Tracking layout is split 50/50: player 2 occupies the bottom half in normal
 * orientation, player 1 the top half rotated 180 degrees so their number
 * faces them across the table. Tapping the right side of a player's half
 * raises their life by one, the left side lowers it by one.
 */
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

    if (started) {
        TrackingContent(
            name1 = name1.ifBlank { "Player 1" },
            name2 = name2.ifBlank { "Player 2" },
            life1 = life1,
            life2 = life2,
            onDelta1 = { life1 += it },
            onDelta2 = { life2 += it },
            onReset = {
                life1 = startingLife
                life2 = startingLife
            },
            onBack = onBack,
        )
    } else {
        SetupContent(
            name1 = name1,
            name2 = name2,
            startingLife = startingLife,
            onName1Change = { name1 = it },
            onName2Change = { name2 = it },
            onStartingLifeChange = { startingLife = it },
            onStart = {
                life1 = startingLife
                life2 = startingLife
                started = true
            },
            onBack = onBack,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetupContent(
    name1: String,
    name2: String,
    startingLife: Int,
    onName1Change: (String) -> Unit,
    onName2Change: (String) -> Unit,
    onStartingLifeChange: (Int) -> Unit,
    onStart: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Match") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            OutlinedTextField(
                value = name1,
                onValueChange = onName1Change,
                label = { Text("Player 1") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = name2,
                onValueChange = onName2Change,
                label = { Text("Player 2") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(24.dp))
            Text("Starting life", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { if (startingLife > 1) onStartingLifeChange(startingLife - 1) },
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
                    onClick = { if (startingLife < 999) onStartingLifeChange(startingLife + 1) },
                    enabled = startingLife < 999,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Increase")
                }
                Spacer(Modifier.size(16.dp))
                FilterChip(
                    selected = startingLife == 20,
                    onClick = { onStartingLifeChange(20) },
                    label = { Text("20") },
                )
                Spacer(Modifier.size(8.dp))
                FilterChip(
                    selected = startingLife == 40,
                    onClick = { onStartingLifeChange(40) },
                    label = { Text("40") },
                )
            }
            Spacer(Modifier.height(24.dp))
            Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
                Text("Start Match")
            }
        }
    }
}

@Composable
private fun TrackingContent(
    name1: String,
    name2: String,
    life1: Int,
    life2: Int,
    onDelta1: (Int) -> Unit,
    onDelta2: (Int) -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
        ) {
            // Player 1: top half, rotated 180 degrees to face them.
            PlayerPanel(
                name = name1,
                life = life1,
                onDelta = onDelta1,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .graphicsLayer { rotationZ = 180f },
            )
            HorizontalDivider(
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            // Player 2: bottom half, normal orientation.
            PlayerPanel(
                name = name2,
                life = life2,
                onDelta = onDelta2,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
        }
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(4.dp),
        ) {
            IconButton(onClick = onReset) {
                Icon(Icons.Filled.Refresh, contentDescription = "Reset life")
            }
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }
    }
}

/**
 * One player's half of the screen: the huge life total in the middle, the
 * player name at their edge, and two invisible tap zones - left half lowers
 * life by one, right half raises it by one.
 */
@Composable
private fun PlayerPanel(
    name: String,
    life: Int,
    onDelta: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Cumulative change since the last tap, shown briefly above the number.
    var pending by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    var clearJob by remember { mutableStateOf<Job?>(null) }

    val onTap: (Int) -> Unit = { delta ->
        pending += delta
        clearJob?.cancel()
        clearJob = scope.launch {
            delay(3_000)
            pending = 0
        }
        onDelta(delta)
    }

    BoxWithConstraints(modifier = modifier) {
        val numberSize = (maxHeight * 0.40f).value.sp
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                // Tap zones first (they stay behind the number in the
                // semantics tree, so the life total remains accessible);
                // the number itself is not clickable, so taps fall through.
                Row(modifier = Modifier.fillMaxSize()) {
                    TapZone(
                        label = "\u22121",
                        onClick = { onTap(-1) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        labelAlignment = Alignment.CenterStart,
                    )
                    TapZone(
                        label = "+1",
                        onClick = { onTap(+1) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        labelAlignment = Alignment.CenterEnd,
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Reserved space keeps the number from jumping around.
                    Box(
                        modifier = Modifier.height(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (pending != 0) {
                            Text(
                                text = if (pending > 0) "+$pending" else "$pending",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (pending > 0) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                },
                            )
                        }
                    }
                    Text(
                        text = "$life",
                        fontSize = numberSize,
                        fontWeight = FontWeight.Black,
                        color = if (life <= 0) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun TapZone(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    labelAlignment: Alignment,
) {
    Box(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick,
        ),
        contentAlignment = labelAlignment,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.padding(horizontal = 14.dp),
        )
    }
}
