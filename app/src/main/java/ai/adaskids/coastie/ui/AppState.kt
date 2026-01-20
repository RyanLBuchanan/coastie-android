package ai.adaskids.coastie.ui

import ai.adaskids.coastie.ui.scenarios.Scenario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class PendingPrompt(
    val scenario: Scenario,
    val title: String,
    val prompt: String
)

class AppState {
    private val _pending = MutableStateFlow<PendingPrompt?>(null)
    val pending: StateFlow<PendingPrompt?> = _pending

    fun setPending(p: PendingPrompt) {
        _pending.value = p
    }

    fun clearPending() {
        _pending.value = null
    }
}
