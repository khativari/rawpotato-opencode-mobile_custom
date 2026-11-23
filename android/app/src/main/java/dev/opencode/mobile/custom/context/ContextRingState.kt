package dev.opencode.mobile.custom.context

import dev.opencode.mobile.domain.message.MessageRole
import dev.opencode.mobile.domain.model.ModelSelection
import dev.opencode.mobile.domain.model.ModelSelectionOption
import dev.opencode.mobile.ui.sessionchat.SessionChatMessageItem

/**
 * Доля кольца для шапки сессии: израсходованный контекст к порогу автокомпакции.
 *
 * Считаем от того же сообщения, что и `SessionInfo.contextUsageFraction` (последний ответ
 * ассистента с измеренным промптом) и той же модели, чтобы кольцо и экран «контекст сессии»
 * не расходились.
 */
object ContextRingState {
    fun ringFraction(
        messages: List<SessionChatMessageItem>,
        modelOptions: List<ModelSelectionOption>,
        selectedModel: ModelSelection?,
    ): Float? {
        val latest =
            messages.asReversed().firstOrNull {
                it.role == MessageRole.Assistant && (it.contextTotalTokens ?: it.contextInputTokens) != null
            } ?: return null
        // Весь промпт (input + cache + output) — так же считает экран «Контекст сессии».
        val used = latest.contextTotalTokens ?: latest.contextInputTokens ?: return null
        val window = windowOf(latest, modelOptions) ?: selectedModel?.option?.contextLimitTokens
        return CompactionCeiling.ringFraction(used, window)
    }

    private fun windowOf(
        message: SessionChatMessageItem,
        modelOptions: List<ModelSelectionOption>,
    ): Long? {
        val providerId = message.modelProviderId ?: return null
        val modelId = message.modelId ?: return null
        return modelOptions
            .firstOrNull { it.providerId == providerId && it.modelId == modelId }
            ?.contextLimitTokens
    }
}
