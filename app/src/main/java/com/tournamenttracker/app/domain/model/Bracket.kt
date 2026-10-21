package com.tournamenttracker.app.domain.model

/** A player taking part in a draft bracket, possibly with an assigned deck. */
data class DraftPlayer(
    val name: String,
    val deckName: String?,
)

/** One match slot in a bracket. A null player means a bye. */
data class DraftMatch(
    val round: Round,
    val index: Int,
    val player1: DraftPlayer?,
    val player2: DraftPlayer?,
) {
    /** True when one of the two slots is empty (a bye). */
    val isBye: Boolean get() = player1 == null || player2 == null
}

/** A freshly generated single-elimination bracket. */
data class DraftBracket(
    val size: Int,
    val rounds: List<Round>,
    val firstRoundMatches: List<DraftMatch>,
)
