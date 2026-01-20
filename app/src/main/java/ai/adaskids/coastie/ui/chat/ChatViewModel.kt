package ai.adaskids.coastie.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.adaskids.coastie.data.CoastieApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ChatUiState(
    val input: String = "",
    val isLoading: Boolean = false,
    val messages: List<ChatMessage> = emptyList(),
    val error: String? = null
)

data class ChatMessage(
    val role: String, // "user" or "assistant"
    val text: String
)

class ChatViewModel(
    private val api: CoastieApi
) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state

    fun setInput(v: String) {
        _state.value = _state.value.copy(input = v)
    }

    fun send() {
        val msg = _state.value.input.trim()
        if (msg.isEmpty()) return

        _state.value = _state.value.copy(
            input = "",
            isLoading = true,
            error = null,
            messages = _state.value.messages + ChatMessage("user", msg)
        )

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                api.chatJson(msg)
            }

            val replyText = result.reply?.trim().orEmpty()

            _state.value = _state.value.copy(
                isLoading = false,
                messages = _state.value.messages + ChatMessage(
                    "assistant",
                    if (replyText.isNotEmpty()) replyText else "[No reply]"
                ),
                error = result.error
            )
        }
    }
}
