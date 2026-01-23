package ai.adaskids.coastie.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Demo-first, in-memory store (no persistence) to share the latest assistant reply across tabs.
 * This avoids threading ChatViewModel through navigation.
 *
 * No PII policy: this is volatile memory only; cleared when app is killed.
 */
data class ExportState(
    val lastUserPrompt: String? = null,
    val lastAssistantReply: String? = null,
    val updatedAtEpochMs: Long? = null
)

object ExportStore {
    private val _state = MutableStateFlow(ExportState())
    val state: StateFlow<ExportState> = _state

    fun update(lastUserPrompt: String, lastAssistantReply: String) {
        _state.value = ExportState(
            lastUserPrompt = lastUserPrompt,
            lastAssistantReply = lastAssistantReply,
            updatedAtEpochMs = System.currentTimeMillis()
        )
    }

    fun clear() {
        _state.value = ExportState()
    }
}
