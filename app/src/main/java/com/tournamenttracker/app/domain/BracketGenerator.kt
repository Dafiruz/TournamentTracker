package com.tournamenttracker.app.domain

import com.tournamenttracker.app.domain.model.DraftBracket
import com.tournamenttracker.app.domain.model.DraftPlayer
import com.tournamenttracker.app.domain.model.Round
import kotlin.random.Random

/**
 * Builds single-elimination brackets for 2-32 players.
 *
 * Rules:
 * - Bracket size is the smallest of 2/4/8/16/32 that fits all players.
 * - Empty slots in the first round become byes.
 * - With 3 players the first round is called "Quarter Finals" (one match plus
 *   one bye), then the winner meets the remaining player in the final.
 */
object BracketGenerator {

    const val MIN_PLAYERS = 2
    const val MAX_PLAYERS = 32

    fun bracketSizeFor(playerCount: Int): Int = when {
        playerCount <= 2 -> 2
        playerCount <= 4 -> 4
        playerCount <= 8 -> 8
        playerCount <= 16 -> 16
        else -> 32
    }

    fun roundsFor(playerCount: Int): List<Round> = when (bracketSizeFor(playerCount)) {
        2 -> listOf(Round.FINAL)
        4 -> if (playerCount == 3) {
            listOf(Round.QUARTER_FINAL, Round.FINAL)
        } else {
            listOf(Round.SEMI_FINAL, Round.FINAL)
        }
        8 -> listOf(Round.QUARTER_FINAL, Round.SEMI_FINAL, Round.FINAL)
        16 -> listOf(Round.ROUND_OF_16, Round.QUARTER_FINAL, Round.SEMI_FINAL, Round.FINAL)
        else -> listOf(
            Round.ROUND_OF_32,
            Round.ROUND_OF_16,
            Round.QUARTER_FINAL,
            Round.SEMI_FINAL,
            Round.FINAL,
        )
    }

    fun generate(
        players: List<DraftPlayer>,
        random: Random = Random.Default,
    ): DraftBracket {
        require(players.size in MIN_PLAYERS..MAX_PLAYERS) {
            "Player count must be between $MIN_PLAYERS and $MAX_PLAYERS, was ${players.size}"
        }
        val size = bracketSizeFor(players.size)
        val rounds = roundsFor(players.size)
        val firstRoundMatches = MatchPairer.pair(players, size, rounds.first(), random)
        return DraftBracket(size = size, rounds = rounds, firstRoundMatches = firstRoundMatches)
    }
}
