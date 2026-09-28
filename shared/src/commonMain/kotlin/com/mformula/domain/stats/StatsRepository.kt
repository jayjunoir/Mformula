package com.mformula.domain.stats

import com.mformula.domain.engine.DailyPuzzle
import com.russhwolf.settings.Settings

data class GameStats(
    val played: Int,
    val streak: Int,
    val maxStreak: Int,
    val bestParDiff: Int,
)

class StatsRepository(private val settings: Settings = createDefaultSettings()) {
    companion object {
        private const val KEY_PLAYED = "m_played"
        private const val KEY_STREAK = "m_streak"
        private const val KEY_MAX_STREAK = "m_max_streak"
        private const val KEY_BEST_DIFF = "m_best_par_diff"
        private const val KEY_LAST_DAY = "m_last_epoch_day"
        private const val KEY_SOLVED_PREFIX = "m_solved_day_"

        private fun createDefaultSettings(): Settings {
            return try {
                Settings()
            } catch (_: Throwable) {
                InMemorySettings()
            }
        }
    }

    fun isDaySolved(epochDay: Long): Boolean =
        settings.getBoolean("$KEY_SOLVED_PREFIX$epochDay", defaultValue = false)

    fun getStats(): GameStats = GameStats(
        played = settings.getInt(KEY_PLAYED, 0),
        streak = settings.getInt(KEY_STREAK, 0),
        maxStreak = settings.getInt(KEY_MAX_STREAK, 0),
        bestParDiff = settings.getInt(KEY_BEST_DIFF, 999),
    )

    fun recordSolve(epochDay: Long, movesTaken: Int, par: Int) {
        if (isDaySolved(epochDay)) return
        settings.putBoolean("$KEY_SOLVED_PREFIX$epochDay", value = true)

        val lastDay = settings.getLong(KEY_LAST_DAY, -1L)
        val streak = when {
            lastDay == -1L -> 1
            epochDay == (lastDay + 1) -> settings.getInt(KEY_STREAK, 0) + 1
            else -> 1
        }

        val maxStreak = maxOf(streak, settings.getInt(KEY_MAX_STREAK, 0))
        val played = settings.getInt(KEY_PLAYED, 0) + 1
        val diff = movesTaken - par
        val bestDiff = minOf(diff, settings.getInt(KEY_BEST_DIFF, 999))

        settings.putInt(KEY_PLAYED, played)
        settings.putInt(KEY_STREAK, streak)
        settings.putInt(KEY_MAX_STREAK, maxStreak)
        settings.putInt(KEY_BEST_DIFF, bestDiff)
        settings.putLong(KEY_LAST_DAY, epochDay)
    }

    @Suppress("unused")
    fun generateShareCard(puzzle: DailyPuzzle, moves: List<String>): String {
        val totalMoves = moves.size - 1
        val diff = totalMoves - puzzle.parMoves
        val resultBadge = when {
            diff <= 0 -> "PAR CRUSHED"
            diff == 1 -> "ON PAR"
            else -> "+$diff OVER PAR"
        }

        val grid = buildString {
            for (i in 1 until moves.size) {
                val prev = moves[i - 1]
                val curr = moves[i]
                for (c in 0 until 4) {
                    when {
                        curr[c] == puzzle.targetWord[c] -> append("+")
                        curr[c] != prev[c] -> append("+")
                        else -> append("⬛")
                    }
                }
                appendLine()
            }
        }.trimEnd()

        return """
            [Mformula] #${puzzle.dayNumber}
            ${puzzle.startWord} ➔ ${puzzle.targetWord}
            Score: $totalMoves Moves ($resultBadge)
            
            $grid
        """.trimIndent()
    }
}

private class InMemorySettings : Settings {
    private val map = mutableMapOf<String, Any>()

    override val keys: Set<String> get() = map.keys
    override val size: Int get() = map.size

    override fun clear() = map.clear()
    override fun remove(key: String) { map.remove(key) }
    override fun hasKey(key: String): Boolean = map.containsKey(key)

    override fun putInt(key: String, value: Int) { map[key] = value }
    override fun getInt(key: String, defaultValue: Int): Int = (map[key] as? Int) ?: defaultValue
    override fun getIntOrNull(key: String): Int? = map[key] as? Int

    override fun putLong(key: String, value: Long) { map[key] = value }
    override fun getLong(key: String, defaultValue: Long): Long = (map[key] as? Long) ?: defaultValue
    override fun getLongOrNull(key: String): Long? = map[key] as? Long

    override fun putString(key: String, value: String) { map[key] = value }
    override fun getString(key: String, defaultValue: String): String = (map[key] as? String) ?: defaultValue
    override fun getStringOrNull(key: String): String? = map[key] as? String

    override fun putFloat(key: String, value: Float) { map[key] = value }
    override fun getFloat(key: String, defaultValue: Float): Float = (map[key] as? Float) ?: defaultValue
    override fun getFloatOrNull(key: String): Float? = map[key] as? Float

    override fun putDouble(key: String, value: Double) { map[key] = value }
    override fun getDouble(key: String, defaultValue: Double): Double = (map[key] as? Double) ?: defaultValue
    override fun getDoubleOrNull(key: String): Double? = map[key] as? Double

    override fun putBoolean(key: String, value: Boolean) { map[key] = value }
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = (map[key] as? Boolean) ?: defaultValue
    override fun getBooleanOrNull(key: String): Boolean? = map[key] as? Boolean
}
