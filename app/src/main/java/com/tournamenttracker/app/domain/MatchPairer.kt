package com.tournamenttracker.app.domain

import com.tournamenttracker.app.domain.model.DraftMatch
import com.tournamenttracker.app.domain.model.DraftPlayer
import com.tournamenttracker.app.domain.model.Round
import kotlin.random.Random

/**
 * Pairs players into the first round of a bracket.
 *
 * Pairings are randomized and mirror matches (both players on the same deck)
 * are avoided whenever possible: several shuffled candidate pairings are
 * generated and the one with the fewest mirrors wins. Bye slots are free to
 * move between players, which also helps break up mirrors.
 */
object MatchPairer {

    const val DEFAULT_ATTEMPTS = 200

    fun pair(
        players: List<DraftPlayer>,
        size: Int,
        firstRound: Round,
        random: Random = Random.Default,
        attempts: Int = DEFAULT_ATTEMPTS,
    ): List<DraftMatch> {
        val slotCount = size / 2
        val realMatchCount = players.size - slotCount
        require(realMatchCount in 0..slotCount) {
            "Invalid player count ${players.size} for bracket size $size"
        }

        var best: List<DraftMatch> = emptyList()
        var bestMirrorCount = Int.MAX_VALUE

        repeat(attempts) {
            val shuffled = players.shuffled(random)
            val matches = buildList {
                for (i in 0 until realMatchCount) {
                    add(DraftMatch(firstRound, i, shuffled[2 * i], shuffled[2 * i + 1]))
                }
                for (i in realMatchCount until slotCount) {
                    add(DraftMatch(firstRound, i, shuffled[i], null))
                }
            }
            val mirrors = mirrorCount(matches)
            if (mirrors < bestMirrorCount) {
                bestMirrorCount = mirrors
                best = matches
            }
            if (mirrors == 0) return matches
        }
        return best
    }

    fun mirrorCount(matches: List<DraftMatch>): Int = matches.count(::isMirror)

    private fun isMirror(match: DraftMatch): Boolean {
        val deck1 = match.player1?.deckName
        val deck2 = match.player2?.deckName
        return deck1 != null && deck1 == deck2
    }
}
