package com.tournamenttracker.app.domain

import com.tournamenttracker.app.domain.model.DraftPlayer
import com.tournamenttracker.app.domain.model.Round
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BracketGeneratorTest {

    @Test
    fun `bracket size for player counts`() {
        assertEquals(2, BracketGenerator.bracketSizeFor(2))
        assertEquals(4, BracketGenerator.bracketSizeFor(3))
        assertEquals(4, BracketGenerator.bracketSizeFor(4))
        assertEquals(8, BracketGenerator.bracketSizeFor(5))
        assertEquals(8, BracketGenerator.bracketSizeFor(8))
        assertEquals(16, BracketGenerator.bracketSizeFor(9))
        assertEquals(16, BracketGenerator.bracketSizeFor(16))
        assertEquals(32, BracketGenerator.bracketSizeFor(17))
        assertEquals(32, BracketGenerator.bracketSizeFor(32))
    }

    @Test
    fun `rounds match expected naming`() {
        assertEquals(listOf(Round.FINAL), BracketGenerator.roundsFor(2))
        assertEquals(
            listOf(Round.QUARTER_FINAL, Round.FINAL),
            BracketGenerator.roundsFor(3),
        )
        assertEquals(
            listOf(Round.SEMI_FINAL, Round.FINAL),
            BracketGenerator.roundsFor(4),
        )
        assertEquals(
            listOf(Round.QUARTER_FINAL, Round.SEMI_FINAL, Round.FINAL),
            BracketGenerator.roundsFor(8),
        )
        assertEquals(
            listOf(Round.ROUND_OF_16, Round.QUARTER_FINAL, Round.SEMI_FINAL, Round.FINAL),
            BracketGenerator.roundsFor(16),
        )
        assertEquals(
            listOf(
                Round.ROUND_OF_32,
                Round.ROUND_OF_16,
                Round.QUARTER_FINAL,
                Round.SEMI_FINAL,
                Round.FINAL,
            ),
            BracketGenerator.roundsFor(32),
        )
    }

    @Test
    fun `first round has correct match and bye counts for every player count`() {
        for (count in 2..32) {
            val players = (1..count).map { DraftPlayer("P$it", null) }
            val bracket = BracketGenerator.generate(players, Random(42))
            val size = BracketGenerator.bracketSizeFor(count)
            assertEquals(size / 2, bracket.firstRoundMatches.size)
            assertEquals(size - count, bracket.firstRoundMatches.count { it.isBye })
            val names = bracket.firstRoundMatches
                .flatMap { listOfNotNull(it.player1, it.player2) }
                .map { it.name }
            assertEquals(players.map { it.name }.sorted(), names.sorted())
        }
    }

    @Test
    fun `bye matches keep the player in slot one`() {
        val players = (1..7).map { DraftPlayer("P$it", null) }
        val bracket = BracketGenerator.generate(players, Random(1))
        bracket.firstRoundMatches.filter { it.isBye }.forEach { match ->
            assertEquals(true, match.isBye)
            assertTrue(match.player1 != null)
            assertNull(match.player2)
        }
    }

    @Test
    fun `generate rejects invalid player counts`() {
        assertThrows(IllegalArgumentException::class.java) {
            BracketGenerator.generate(listOf(DraftPlayer("A", null)), Random(1))
        }
        assertThrows(IllegalArgumentException::class.java) {
            BracketGenerator.generate(
                (1..33).map { DraftPlayer("P$it", null) },
                Random(1),
            )
        }
    }
}
