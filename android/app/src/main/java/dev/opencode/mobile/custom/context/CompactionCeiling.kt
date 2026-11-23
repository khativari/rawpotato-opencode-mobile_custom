package dev.opencode.mobile.custom.context

/**
 * Порог автокомпакции opencode-сервера, повторённый клиентом.
 *
 * Источник: `anomalyco/opencode`, `packages/core/src/session/compaction.ts` —
 * `window = min(limit.input, limit.context)`,
 * `reserve = max(floor(window * 0.15), window >= 32000 ? 16000 : 0)`,
 * `ceiling = window - reserve`, компакция — когда измеренный промпт дошёл до `ceiling`.
 *
 * Отсюда: для окон 1M/200k/128k порог 85 %, для 100k — 84 %, для 64k — 75 %, для 32k — 50 %.
 */
object CompactionCeiling {
    const val RESERVE_SHARE = 0.15
    const val RESERVE_MIN = 16_000L
    const val RESERVE_MIN_WINDOW = 2 * RESERVE_MIN

    /** Объём окна, на котором срабатывает автокомпакция; null — окно неизвестно. */
    fun ceiling(window: Long?): Long? {
        if (window == null || window <= 0L) return null
        val share = (window * RESERVE_SHARE).toLong()
        val floor = if (window >= RESERVE_MIN_WINDOW) RESERVE_MIN else 0L
        val reserve = maxOf(share, floor)
        return (window - reserve).coerceAtLeast(1L)
    }

    /**
     * Доля кольца от порога компакции: 1.0 ровно тогда, когда сервер собирается компактить.
     * Не от лимита окна — иначе кольцо врало бы на тесных моделях.
     */
    fun ringFraction(used: Long?, window: Long?): Float? {
        val ceiling = ceiling(window) ?: return null
        val tokens = used ?: return null
        if (tokens <= 0L) return 0f
        return (tokens.toFloat() / ceiling.toFloat()).coerceIn(0f, 1f)
    }

    /** Цветовая зона кольца: до 95 % нейтрально, дальше предупреждение, к самому порогу — ошибка. */
    enum class Severity { Neutral, Warn, Error }

    fun severity(ring: Float?): Severity =
        when {
            ring == null -> Severity.Neutral
            ring >= 0.99f -> Severity.Error
            ring >= 0.95f -> Severity.Warn
            else -> Severity.Neutral
        }
}
