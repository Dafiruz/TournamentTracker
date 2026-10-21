package com.tournamenttracker.app.ui.tournament

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tournamenttracker.app.TournamentTrackerApp
import com.tournamenttracker.app.data.TournamentRepository
import com.tournamenttracker.app.data.db.TournamentWithDetails
import com.tournamenttracker.app.data.entity.TournamentStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TournamentViewModel(
    private val repository: TournamentRepository,
    private val tournamentId: Long,
) : ViewModel() {

    val tournament: StateFlow<TournamentWithDetails?> = repository.observeTournament(tournamentId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Sets the winner of [matchId] to [playerId]; tapping the current winner clears it. */
    fun pickWinner(matchId: Long, playerId: Long) {
        viewModelScope.launch {
            repository.setWinner(tournamentId, matchId, playerId)
        }
    }

    fun setStatus(status: TournamentStatus) {
        viewModelScope.launch {
            repository.setStatus(tournamentId, status)
        }
    }

    fun delete(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteTournament(tournamentId)
            onDeleted()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as TournamentTrackerApp
                val tournamentId = createSavedStateHandle().get<Long>("tournamentId") ?: -1L
                TournamentViewModel(app.repository, tournamentId)
            }
        }
    }
}
