package com.tournamenttracker.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tournamenttracker.app.TournamentTrackerApp
import com.tournamenttracker.app.data.TournamentRepository
import com.tournamenttracker.app.data.db.TournamentWithDetails
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(repository: TournamentRepository) : ViewModel() {

    val tournaments: StateFlow<List<TournamentWithDetails>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as TournamentTrackerApp
                HomeViewModel(app.repository)
            }
        }
    }
}
