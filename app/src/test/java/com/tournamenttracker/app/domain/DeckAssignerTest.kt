package com.tournamenttracker.app.domain

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeckAssignerTest {

    @Test
    fun `no decks means no assignment`() {
        val assigned = DeckAssigner.assign(listOf("P1", "P2", "P3"), emptyList(), Random(1))
        assertTrue(assigned.all { it.deckName == null })
        assertEquals(listOf("P1", "P2", "P3"), assigned.map { it.name })
    }

    @Test
    fun `deck counts are balanced when players outnumber decks`() {
        // The 4 decks / 7 players example: three decks end up with 2 players,
        // one deck with a single player.
        val decks = listOf("A", "B", "C", "D")
        val players = (1..7).map { "P$it" }
        val assigned = DeckAssigner.assign(players, decks, Random(7))
        val counts = assigned.groupingBy { it.deckName }.eachCount()
        assertEquals(setOf("A", "B", "C", "D"), counts.keys)
        assertEquals(7, counts.values.sum())
        assertTrue(counts.values.all { it == 1 || it == 2 })
        assertEquals(3, counts.values.count { it == 2 })
        assertEquals(1, counts.values.count { it == 1 })
    }

    @Test
    fun `more decks than players keeps decks unique`() {
        val decks = listOf("A", "B", "C", "D", "E")
        val players = (1..3).map { "P$it" }
        val assigned = DeckAssigner.assign(players, decks, Random(3))
        assertEquals(3, assigned.mapNotNull { it.deckName }.distinct().size)
        assertTrue(assigned.all { it.deckName != null })
    }

    @Test
    fun `equal decks and players gives a bijection`() {
        val decks = listOf("A", "B", "C", "D")
        val players = (1..4).map { "P$it" }
        val assigned = DeckAssigner.assign(players, decks, Random(5))
        assertEquals(4, assigned.mapNotNull { it.deckName }.distinct().size)
        assertTrue(assigned.none { it.deckName == null })
    }

    @Test
    fun `different seeds produce different assignments`() {
        val decks = listOf("A", "B", "C", "D")
        val players = (1..7).map { "P$it" }
        val a = DeckAssigner.assign(players, decks, Random(1)).map { it.deckName }
        val b = DeckAssigner.assign(players, decks, Random(2)).map { it.deckName }
        assertNotEquals(a, b)
    }

    @Test
    fun `deckless assignment keeps every player`() {
        val assigned = DeckAssigner.assign(listOf("Alice"), emptyList(), Random(1))
        assertEquals("Alice", assigned.single().name)
        assertNull(assigned.single().deckName)
    }
}
