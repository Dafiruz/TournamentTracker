package com.tournamenttracker.app.domain

import com.tournamenttracker.app.domain.model.DraftPlayer
import com.tournamenttracker.app.domain.model.Round
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Test

class MatchPairerTest {

    @Test
    fun `avoids mirrors when possible`() {
        val players = listOf(
            DraftPlayer("P1", "A"), DraftPlayer("P2", "A"),
            DraftPlayer("P3", "B"), DraftPlayer("P4", "B"),
            DraftPlayer("P5", "C"), DraftPlayer("P6", "C"),
            DraftPlayer("P7", "D"), DraftPlayer("P8", "D"),
        )
        val matches = MatchPairer.pair(players, 8, Round.QUARTER_FINAL, Random(42))
        assertEquals(4, matches.size)
        assertEquals(0, MatchPairer.mirrorCount(matches))
    }

    @Test
    fun `mirrors happen when unavoidable`() {
        val players = listOf(DraftPlayer("P1", "A"), DraftPlayer("P2", "A"))
        val matches = MatchPairer.pair(players, 2, Round.FINAL, Random(42))
        assertEquals(1, MatchPairer.mirrorCount(matches))
    }

    @Test
    fun `bye placement helps avoid mirrors`() {
        // 5 players in an 8 bracket: one real match, three byes.
        val players = listOf(
            DraftPlayer("P1", "A"), DraftPlayer("P2", "A"),
            DraftPlayer("P3", "B"), DraftPlayer("P4", "B"),
            DraftPlayer("P5", "C"),
        )
        val matches = MatchPairer.pair(players, 8, Round.QUARTER_FINAL, Random(42))
        assertEquals(4, matches.size)
        assertEquals(3, matches.count { it.isBye })
        assertEquals(0, MatchPairer.mirrorCount(matches))
    }

    @Test
    fun `every player appears exactly once`() {
        val players = listOf(
            DraftPlayer("P1", "A"), DraftPlayer("P2", "A"),
            DraftPlayer("P3", "B"), DraftPlayer("P4", "C"),
            DraftPlayer("P5", "C"), DraftPlayer("P6", "D"),
            DraftPlayer("P7", "D"),
        )
        val matches = MatchPairer.pair(players, 8, Round.QUARTER_FINAL, Random(99))
        val names = matches.flatMap { listOfNotNull(it.player1, it.player2) }.map { it.name }
        assertEquals(players.map { it.name }.sorted(), names.sorted())
    }

    @Test
    fun `mirror free pairing for the seven player example`() {
        // 4 decks / 7 players, balanced assignment A A B B C C D.
        val players = listOf(
            DraftPlayer("P1", "A"), DraftPlayer("P2", "A"),
            DraftPlayer("P3", "B"), DraftPlayer("P4", "B"),
            DraftPlayer("P5", "C"), DraftPlayer("P6", "C"),
            DraftPlayer("P7", "D"),
        )
        val matches = MatchPairer.pair(players, 8, Round.QUARTER_FINAL, Random(123))
        assertEquals(0, MatchPairer.mirrorCount(matches))
    }
}
