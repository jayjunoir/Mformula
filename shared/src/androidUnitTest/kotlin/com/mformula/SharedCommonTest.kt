package com.mformula

import com.mformula.domain.engine.WordEngine
import com.mformula.domain.engine.WordTrie
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@Suppress("unused")
class SharedCommonTest {

    @Test
    fun testWordTrieAndEditDistance() {
        val trie = WordTrie()
        trie.insert("COLD")
        trie.insert("CORD")

        assertTrue(trie.contains("COLD"))
        assertTrue(trie.contains("CORD"))
        assertFalse(trie.contains("CARD"))

        assertTrue(WordEngine.isOneEditAway("COLD", "CORD"))
        assertFalse(WordEngine.isOneEditAway("COLD", "CARD"))
        assertFalse(WordEngine.isOneEditAway("COLD", "COLD"))
    }

    @Test
    fun testDeterministicPuzzleGeneration() {
        val epochDay = 20000L
        val puzzle1 = WordEngine.getDailyPuzzle(epochDay)
        val puzzle2 = WordEngine.getDailyPuzzle(epochDay)

        assertEquals(puzzle1.startWord, puzzle2.startWord)
        assertEquals(puzzle1.targetWord, puzzle2.targetWord)
        assertEquals(puzzle1.parMoves, puzzle2.parMoves)
        assertEquals(puzzle1.optimalPath, puzzle2.optimalPath)
    }

    @Test
    fun testBranchingPuzzleConstraints() {
        for (day in 20000L..20010L) {
            val puzzle = WordEngine.getDailyPuzzle(day)

            assertNotNull(puzzle)
            assertTrue(puzzle.startWord != puzzle.targetWord, "Start and target must differ")
            assertTrue((puzzle.parMoves in 4..6), "Par moves must be between 4 and 6")
            assertEquals(puzzle.parMoves + 1, puzzle.optimalPath.size, "Path size must be parMoves + 1")

            var matchingChars = 0
            for (i in 0 until 4) {
                if (puzzle.startWord[i] == puzzle.targetWord[i]) matchingChars++
            }
            assertTrue(matchingChars < 3, "Start and target must not share 3 or more identical characters")
        }
    }
}
