package com.tournamenttracker.app.domain

import com.tournamenttracker.app.domain.model.DraftPlayer
import kotlin.random.Random

/**
 * Randomly assigns decks from a pool to players, battlebox style.
 *
 * - Deck names in the pool are unique.
 * - Every player receives exactly one deck.
 * - When there are more players than decks, decks repeat, but the assignment
 *   is kept as balanced as possible (deck counts differ by at most one).
 * - When there are at least as many decks as players, every assigned deck is
 *   unique.
 */
object DeckAssigner {

    fun assign(
        players: List<String>,
        decks: List<String>,
        random: Random = Random.Default,
    ): List<DraftPlayer> {
        if (decks.isEmpty()) return players.map { DraftPlayer(it, null) }

        val playerCount = players.size
        val deckOrder = decks.shuffled(random)

        // Balanced multiset: each deck appears floor(n/m) or ceil(n/m) times.
        val multiset = buildList {
            for ((i, deck) in deckOrder.withIndex()) {
                val count = playerCount / decks.size + if (i < playerCount % decks.size) 1 else 0
                repeat(count) { add(deck) }
            }
        }.shuffled(random)

        return players.mapIndexed { index, player ->
            DraftPlayer(player, multiset[index])
        }
    }
}
