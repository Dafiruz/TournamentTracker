package com.tournamenttracker.app.domain.model

/** A round in a single-elimination bracket, ordered from first to last. */
enum class Round(val displayName: String) {
    ROUND_OF_32("Round of 32"),
    ROUND_OF_16("Round of 16"),
    QUARTER_FINAL("Quarter Finals"),
    SEMI_FINAL("Semi Finals"),
    FINAL("Finals"),
}
