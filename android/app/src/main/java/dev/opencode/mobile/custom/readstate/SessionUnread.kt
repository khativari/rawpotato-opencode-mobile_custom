package dev.opencode.mobile.custom.readstate

/**
 * Решает, показывать ли сессию непрочитанной.
 *
 * Источники: ручная метка (сильнее всего), локальная дата прочтения (перебивает серверный
 * признак) и сам серверный признак `unreadOutcome` для v2.
 */
object SessionUnread {
    fun isUnread(
        updatedAtEpochMs: Long?,
        serverUnread: Boolean,
        state: SessionReadState?,
    ): Boolean {
        if (state?.manuallyUnread == true) return true
        val readAt = state?.lastReadAtEpochMs
        // Локально прочитано позже последнего обновления — серверный признак устарел.
        if (readAt != null && updatedAtEpochMs != null && readAt >= updatedAtEpochMs) return false
        // Время обновления неизвестно, но сессию читали — считаем прочитанной.
        if (readAt != null && updatedAtEpochMs == null) return false
        return serverUnread
    }
}
