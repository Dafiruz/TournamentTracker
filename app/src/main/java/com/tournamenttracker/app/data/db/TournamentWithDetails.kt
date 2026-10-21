package com.tournamenttracker.app.data.db

import androidx.room.Embedded
import androidx.room.Relation
import com.tournamenttracker.app.data.entity.DeckEntity
import com.tournamenttracker.app.data.entity.MatchEntity
import com.tournamenttracker.app.data.entity.PlayerEntity
import com.tournamenttracker.app.data.entity.TournamentEntity

/** A tournament together with its players, decks and matches. */
data class TournamentWithDetails(
    @Embedded val tournament: TournamentEntity,
    @Relation(parentColumn = "id", entityColumn = "tournamentId")
    val players: List<PlayerEntity>,
    @Relation(parentColumn = "id", entityColumn = "tournamentId")
    val decks: List<DeckEntity>,
    @Relation(parentColumn = "id", entityColumn = "tournamentId")
    val matches: List<MatchEntity>,
)
