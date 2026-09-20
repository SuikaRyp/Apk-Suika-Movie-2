package com.suikamovie.app.data.model

/**
 * Sistem level & EXP - port 1:1 dari public/js/user-data.js.
 * Kurva EXP progresif: cumulative EXP buat nyampe level N = 50 * (N-1) * N
 */
const val EXP_PER_WATCH = 20

private data class LevelTier(val maxLevel: Int, val title: String)

private val LEVEL_TITLE_TIERS = listOf(
    LevelTier(4, "Pemula"),
    LevelTier(9, "Penikmat Film"),
    LevelTier(19, "Kolektor Genre"),
    LevelTier(34, "Master Streaming"),
    LevelTier(49, "Legenda SuikaMovie"),
    LevelTier(Int.MAX_VALUE, "SUIKA GRANDMASTER"),
)

data class LevelInfo(
    val level: Int,
    val title: String,
    val exp: Int,
    val expIntoLevel: Int,
    val expNeededForLevel: Int,
    val progressPercent: Int,
)

private fun cumulativeExpForLevel(level: Int): Int = 50 * (level - 1) * level

fun titleForLevel(level: Int): String =
    LEVEL_TITLE_TIERS.firstOrNull { level <= it.maxLevel }?.title ?: LEVEL_TITLE_TIERS.last().title

fun getLevelInfo(exp: Int): LevelInfo {
    val safeExp = exp.coerceAtLeast(0)
    var level = 1
    while (cumulativeExpForLevel(level + 1) <= safeExp) level++

    val currentLevelBaseExp = cumulativeExpForLevel(level)
    val nextLevelExp = cumulativeExpForLevel(level + 1)
    val expIntoLevel = safeExp - currentLevelBaseExp
    val expNeededForLevel = nextLevelExp - currentLevelBaseExp
    val progressPercent = if (expNeededForLevel > 0) {
        ((expIntoLevel.toDouble() / expNeededForLevel) * 100).toInt().coerceIn(0, 100)
    } else 100

    return LevelInfo(
        level = level,
        title = titleForLevel(level),
        exp = safeExp,
        expIntoLevel = expIntoLevel,
        expNeededForLevel = expNeededForLevel,
        progressPercent = progressPercent,
    )
}
