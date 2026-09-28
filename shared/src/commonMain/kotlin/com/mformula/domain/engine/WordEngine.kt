package com.mformula.domain.engine

import mformula.shared.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.concurrent.Volatile
import kotlin.jvm.JvmName
import kotlin.random.Random

class TrieNode {
    val children = HashMap<Char, TrieNode>()
    @Volatile
    var isEndOfWord = false
}

class WordTrie {
    val root = TrieNode()

    fun insert(word: String) {
        var current = root
        for (ch in word) {
            current = current.children.getOrPut(ch) { TrieNode() }
        }
        current.isEndOfWord = true
    }

    fun contains(word: String): Boolean {
        if (word.length != 4) return false
        var current = root
        for (ch in word) {
            current = current.children[ch] ?: return false
        }
        return current.isEndOfWord
    }
}

data class DailyPuzzle(
    val dayNumber: Long,
    val startWord: String,
    val targetWord: String,
    val parMoves: Int,
    val optimalPath: List<String>,
) {
    val dateKey: String
        get() = "Day #$dayNumber"

    @get:JvmName("getOptimalChainProperty")
    @get:Suppress("unused")
    val optimalChain: List<String>
        get() = optimalPath
}

object WordEngine {
    private val DEFAULT_DICTIONARY: List<String> = listOf(
        "COLD", "CORD", "CARD", "WARD", "WARM", "WORM", "WORD", "WOOD", "GOOD", "GOLD",
        "HOLD", "HEAD", "HEAL", "TEAL", "TEAR", "BEAR", "BEAT", "BOAT", "COAT", "CHAT",
        "THAT", "THIS", "THIN", "CHIN", "COIN", "JOIN", "LION", "LINE", "LANE", "LATE",
        "DATE", "DATA", "DARE", "CARE", "CORE", "MORE", "MARE", "MARK", "PARK", "PART",
        "PORT", "POST", "PAST", "FAST", "FIST", "FISH", "WISH", "WASH", "BASH", "BASE",
        "CASE", "CAST", "COST", "LOST", "LAST", "EAST", "EASY", "FALL", "BALL", "CALL",
        "TALL", "TELL", "BELL", "BELT", "BOLT", "BOOT", "FOOT", "FOOD", "FOLD", "FORD",
        "FIRE", "FILE", "FILL", "HILL", "HALL", "HALT", "HATE", "HAVE", "GAVE", "GAME",
        "GATE", "GIFT", "LIFT", "LIFE", "LIKE", "LIME", "TIME", "TIRE", "WIRE", "WIDE",
        "RIDE", "ROAD", "ROAR", "SOAR", "SOUP", "SOUR", "FOUR", "FOUL", "FOOL", "POOL",
        "POOR", "PEER", "PEAR", "PEAK", "PACK", "PICK", "PINK", "PUNK", "PUMP", "JUMP",
        "CAMP", "LAMP", "RAMP", "RAIN", "PAIN", "PAIR", "PAGE", "RAGE", "RACE", "RICE",
        "MINE", "MINT", "PINT", "PINE", "FINE", "FIND", "WIND", "WILD", "WILL", "WELL",
    ).sorted()

    @Volatile
    private var mutableDictionary: List<String> = DEFAULT_DICTIONARY

    @Volatile
    private var lookupSet: Set<String> = DEFAULT_DICTIONARY.toSet()

    @Volatile
    var trie: WordTrie = WordTrie().apply { DEFAULT_DICTIONARY.forEach { insert(it) } }
        private set

    @Volatile
    var isInitialized = false
        private set

    val DICTIONARY: List<String>
        get() = mutableDictionary

    @OptIn(ExperimentalResourceApi::class)
    suspend fun initialize() {
        if (isInitialized) return
        try {
            val bytes = Res.readBytes("files/words_4letter.txt")
            val sanitizedWords = bytes.decodeToString()
                .lineSequence()
                .map { line ->
                    line.replace(Regex("[^A-Za-z]"), "").uppercase()
                }
                .filter { word -> (word.length == 4) && word.all { ch -> ((ch >= 'A') && (ch <= 'Z')) } }
                .distinct()
                .sorted()
                .toList()

            if (sanitizedWords.isNotEmpty()) {
                val newTrie = WordTrie()
                sanitizedWords.forEach { newTrie.insert(it) }
                val newSet = sanitizedWords.toSet()

                this.mutableDictionary = sanitizedWords
                this.lookupSet = newSet
                this.trie = newTrie
                this.isInitialized = true
                return
            }
        } catch (_: Throwable) {
            // Fallback to default dictionary if asset reading fails
        }
        isInitialized = true
    }

    fun isOneEditAway(w1: String, w2: String): Boolean {
        if ((w1.length != 4) || (w2.length != 4)) return false
        var diff = 0
        for (i in 0 until 4) {
            if (w1[i] != w2[i]) {
                diff++
                if (diff > 1) return false
            }
        }
        return diff == 1
    }

    fun getNeighbors(word: String, currentLookupSet: Set<String> = lookupSet): List<String> {
        val neighbors = ArrayList<String>()
        val charArray = word.toCharArray()
        for (i in 0 until 4) {
            val original = charArray[i]
            for (c in 'A'..'Z') {
                if (c == original) continue
                charArray[i] = c
                val candidate = String(charArray)
                if (currentLookupSet.contains(candidate)) {
                    neighbors.add(candidate)
                }
            }
            charArray[i] = original
        }
        return neighbors
    }

    fun findShortestChain(
        start: String,
        target: String,
        currentLookupSet: Set<String> = lookupSet,
    ): List<String>? {
        if (start == target) return listOf(start)
        if (!currentLookupSet.contains(start) || !currentLookupSet.contains(target)) return null

        val startQueue = ArrayDeque<String>().apply { add(start) }
        val targetQueue = ArrayDeque<String>().apply { add(target) }

        val startVisited = mutableMapOf<String, String?>().apply { put(start, null) }
        val targetVisited = mutableMapOf<String, String?>().apply { put(target, null) }

        var intersection: String? = null

        while (startQueue.isNotEmpty() && targetQueue.isNotEmpty()) {
            intersection = if (startQueue.size <= targetQueue.size) {
                expandLevel(startQueue, startVisited, targetVisited, currentLookupSet)
            } else {
                expandLevel(targetQueue, targetVisited, startVisited, currentLookupSet)
            }
            if (intersection != null) break
        }

        if (intersection == null) return null

        val pathFromStart = mutableListOf<String>()
        var curr: String? = intersection
        while (curr != null) {
            pathFromStart.add(curr)
            curr = startVisited[curr]
        }
        pathFromStart.reverse()

        val pathFromTarget = mutableListOf<String>()
        curr = targetVisited[intersection]
        while (curr != null) {
            if (curr != intersection) {
                pathFromTarget.add(curr)
            }
            curr = targetVisited[curr]
        }

        return pathFromStart + pathFromTarget
    }

    private fun expandLevel(
        queue: ArrayDeque<String>,
        visitedThisSide: MutableMap<String, String?>,
        visitedOtherSide: MutableMap<String, String?>,
        currentLookupSet: Set<String>,
    ): String? {
        val levelSize = queue.size
        repeat(levelSize) {
            val current = queue.removeFirst()
            val neighbors = getNeighbors(current, currentLookupSet)
            for (neighbor in neighbors) {
                if (visitedOtherSide.containsKey(neighbor)) {
                    visitedThisSide[neighbor] = current
                    return neighbor
                }
                if (!visitedThisSide.containsKey(neighbor)) {
                    visitedThisSide[neighbor] = current
                    queue.add(neighbor)
                }
            }
        }
        return null
    }

    private fun sharesThreeOrMoreChars(w1: String, w2: String): Boolean {
        var count = 0
        for (i in 0 until 4) {
            if (w1[i] == w2[i]) count++
        }
        return count >= 3
    }

    private fun countAlternativeMutations(
        word: String,
        prevWord: String,
        nextWord: String,
        currentLookupSet: Set<String>,
    ): Int {
        val neighbors = getNeighbors(word, currentLookupSet)
        return neighbors.count { (it != prevWord) && (it != nextWord) }
    }

    fun getDailyPuzzle(epochDays: Long): DailyPuzzle {
        val seed = epochDays * 2654435761L
        val rng = Random(seed)
        val dayNumber = (epochDays - 20000L).coerceAtLeast(1L)

        val dict = DICTIONARY
        val currentSet = lookupSet

        var bestFallback: DailyPuzzle? = null

        repeat(1001) {
            val start = dict[rng.nextInt(dict.size)]
            val target = dict[rng.nextInt(dict.size)]
            if ((start != target) && !sharesThreeOrMoreChars(start, target)) {
                val chain = findShortestChain(start, target, currentSet)
                if (chain != null) {
                    val moves = chain.size - 1
                    if ((moves >= 4) && (moves <= 6)) {
                        var branchingCount = 0
                        for (i in 1 until (chain.size - 1)) {
                            val altMutations = countAlternativeMutations(chain[i], chain[i - 1], chain[i + 1], currentSet)
                            if (altMutations >= 3) {
                                branchingCount++
                            }
                        }

                        val puzzle = DailyPuzzle(
                            dayNumber = dayNumber,
                            startWord = start,
                            targetWord = target,
                            parMoves = moves,
                            optimalPath = chain,
                        )

                        if (branchingCount >= 2) {
                            return puzzle
                        } else if (bestFallback == null) {
                            bestFallback = puzzle
                        }
                    }
                }
            }
        }

        return bestFallback ?: DailyPuzzle(
            dayNumber = dayNumber,
            startWord = "COLD",
            targetWord = "WARM",
            parMoves = 4,
            optimalPath = listOf("COLD", "CORD", "CARD", "WARD", "WARM"),
        )
    }
}
