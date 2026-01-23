package ai.adaskids.coastie.ui.chat

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import ai.adaskids.coastie.data.CoastieApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ChatUiState(
    val input: String = "",
    val isLoading: Boolean = false,
    val messages: List<ChatMessage> = emptyList(),
    val error: String? = null,

    // Attachment (metadata only, no bytes stored)
    val attachment: AttachmentUi? = null
)

data class ChatMessage(
    val role: String, // "user" or "assistant"
    val text: String
)

data class AttachmentUi(
    val uri: Uri,
    val name: String,
    val mimeType: String,
    val sizeBytes: Long? = null
)

class ChatViewModel(
    private val api: CoastieApi
) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state

    fun setInput(v: String) {
        _state.value = _state.value.copy(input = v)
    }

    fun setAttachment(attachment: AttachmentUi) {
        _state.value = _state.value.copy(attachment = attachment)
    }

    fun clearAttachment() {
        _state.value = _state.value.copy(attachment = null)
    }

    /**
     * Send the current message.
     * If an attachment is present, we send multipart/form-data (message + file).
     * Otherwise, we send JSON { message: "..." }.
     */
    fun send(contentResolver: ContentResolver) {
        val msg = _state.value.input.trim()
        if (msg.isEmpty()) return

        val attachment = _state.value.attachment

        // Immediately update UI (user message appears right away)
        _state.value = _state.value.copy(
            input = "",
            isLoading = true,
            error = null,
            messages = _state.value.messages + ChatMessage("user", msg)
        )

        if (attachment == null) {
            api.chatJsonAsync(msg) { result ->
                val replyText = result.reply?.trim().orEmpty()
                _state.value = _state.value.copy(
                    isLoading = false,
                    messages = _state.value.messages + ChatMessage(
                        role = "assistant",
                        text = if (replyText.isNotEmpty()) replyText else "[No reply]"
                    ),
                    error = result.error
                )
            }
        } else {
            api.chatMultipartAsync(
                contentResolver = contentResolver,
                message = msg,
                fileUri = attachment.uri,
                fileName = attachment.name,
                mimeType = attachment.mimeType
            ) { result ->
                val replyText = result.reply?.trim().orEmpty()
                _state.value = _state.value.copy(
                    isLoading = false,
                    // Clear attachment after successful send attempt (even if server error returns)
                    attachment = null,
                    messages = _state.value.messages + ChatMessage(
                        role = "assistant",
                        text = if (replyText.isNotEmpty()) replyText else "[No reply]"
                    ),
                    error = result.error
                )
            }
        }
    }
}
