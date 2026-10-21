package com.tournamenttracker.app.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tournamenttracker.app.TournamentTrackerApp
import com.tournamenttracker.app.data.TournamentRepository
import com.tournamenttracker.app.domain.BracketGenerator
import com.tournamenttracker.app.domain.DeckAssigner
import com.tournamenttracker.app.domain.MatchPairer
import com.tournamenttracker.app.domain.model.DraftBracket
import com.tournamenttracker.app.domain.model.DraftPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

/** Deck assignment + bracket preview shown before a tournament starts. */
data class Preview(
    val assignments: List<DraftPlayer>,
    val bracket: DraftBracket,
    val mirrorCount: Int,
)

data class CreateState(
    val name: String = "",
    val players: List<String> = emptyList(),
    val decks: List<String> = emptyList(),
    val preview: Preview? = null,
    val message: String? = null,
)

class CreateViewModel(private val repository: TournamentRepository) : ViewModel() {

    private val _state = MutableStateFlow(CreateState())
    val state: StateFlow<CreateState> = _state.asStateFlow()

    private val random = Random.Default

    fun setName(value: String) = _state.update { it.copy(name = value) }

    fun addPlayer(rawName: String) {
        val name = rawName.trim()
        if (name.isEmpty()) return notify("Enter a player name")
        if (_state.value.players.size >= BracketGenerator.MAX_PLAYERS) {
            return notify("Maximum is ${BracketGenerator.MAX_PLAYERS} players")
        }
        if (_state.value.players.any { it.equals(name, ignoreCase = true) }) {
            return notify("Player already added")
        }
        _state.update { it.copy(players = it.players + name) }
    }

    fun removePlayer(name: String) = _state.update {
        it.copy(players = it.players - name)
    }

    fun addDeck(rawName: String) {
        val name = rawName.trim()
        if (name.isEmpty()) return notify("Enter a deck name")
        if (_state.value.decks.any { it.equals(name, ignoreCase = true) }) {
            return notify("Deck already added")
        }
        _state.update { it.copy(decks = it.decks + name) }
    }

    fun removeDeck(name: String) = _state.update {
        it.copy(decks = it.decks - name)
    }

    /** Randomizes deck assignment and match pairings. */
    fun randomize() {
        val current = _state.value
        if (current.name.isBlank()) return notify("Enter a tournament name")
        if (current.players.size < BracketGenerator.MIN_PLAYERS) {
            return notify("Add at least ${BracketGenerator.MIN_PLAYERS} players")
        }
        _state.update { it.copy(preview = buildPreview(current)) }
    }

    /** Re-randomizes deck assignment and re-pairs matches to match the new decks. */
    fun rerollDecks() {
        val current = _state.value
        val assignments = DeckAssigner.assign(current.players, current.decks, random)
        val bracket = BracketGenerator.generate(assignments, random)
        _state.update {
            it.copy(
                preview = Preview(
                    assignments = assignments,
                    bracket = bracket,
                    mirrorCount = MatchPairer.mirrorCount(bracket.firstRoundMatches),
                )
            )
        }
    }

    /** Re-pairs matches only, keeping the current deck assignments. */
    fun rerollMatches() {
        val current = _state.value
        val preview = current.preview ?: return
        val bracket = BracketGenerator.generate(preview.assignments, random)
        _state.update {
            it.copy(
                preview = preview.copy(
                    bracket = bracket,
                    mirrorCount = MatchPairer.mirrorCount(bracket.firstRoundMatches),
                )
            )
        }
    }

    fun start(onStarted: (Long) -> Unit) {
        val current = _state.value
        val preview = current.preview ?: return
        viewModelScope.launch {
            val tournamentId = repository.createTournament(current.name, preview.bracket)
            onStarted(tournamentId)
        }
    }

    fun consumeMessage() = _state.update { it.copy(message = null) }

    private fun buildPreview(state: CreateState): Preview {
        val assignments = DeckAssigner.assign(state.players, state.decks, random)
        val bracket = BracketGenerator.generate(assignments, random)
        return Preview(
            assignments = assignments,
            bracket = bracket,
            mirrorCount = MatchPairer.mirrorCount(bracket.firstRoundMatches),
        )
    }

    private fun notify(message: String) = _state.update { it.copy(message = message) }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as TournamentTrackerApp
                CreateViewModel(app.repository)
            }
        }
    }
}
