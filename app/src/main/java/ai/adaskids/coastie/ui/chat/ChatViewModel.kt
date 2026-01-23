package ai.adaskids.coastie.ui.chat

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.adaskids.coastie.data.CoastieApi
import ai.adaskids.coastie.data.local.HistoryRepository
import ai.adaskids.coastie.data.local.entity.HistoryEntryEntity
import ai.adaskids.coastie.ui.ExportStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ChatUiState(
    val input: String = "",
    val isLoading: Boolean = false,
    val messages: List<ChatMessage> = emptyList(),
    val error: String? = null,
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
    private val api: CoastieApi,
    private val historyRepo: HistoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state

    // Optional: scenario context set from PromptEditor/Scenario run
    private var scenarioTitle: String? = null
    fun setScenarioTitle(title: String?) { scenarioTitle = title }

    fun setInput(v: String) {
        _state.value = _state.value.copy(input = v)
    }

    fun setAttachment(attachment: AttachmentUi) {
        _state.value = _state.value.copy(attachment = attachment)
    }

    fun clearAttachment() {
        _state.value = _state.value.copy(attachment = null)
    }

    fun send(contentResolver: ContentResolver) {
        val msg = _state.value.input.trim()
        if (msg.isEmpty()) return

        val attachment = _state.value.attachment

        _state.value = _state.value.copy(
            input = "",
            isLoading = true,
            error = null,
            messages = _state.value.messages + ChatMessage("user", msg)
        )

        fun onFinal(resultReply: String, resultError: String?) {
            val finalReply = resultReply.trim().ifEmpty { "[No reply]" }

            // Exports tab
            ExportStore.update(lastUserPrompt = msg, lastAssistantReply = finalReply)

            // History (local, demo)
            viewModelScope.launch {
                historyRepo.insert(
                    HistoryEntryEntity(
                        scenarioTitle = scenarioTitle,
                        prompt = msg,
                        response = finalReply,
                        createdAtEpochMs = System.currentTimeMillis(),
                        attachmentName = attachment?.name,
                        attachmentMimeType = attachment?.mimeType,
                        attachmentSizeBytes = attachment?.sizeBytes
                    )
                )
            }

            _state.value = _state.value.copy(
                isLoading = false,
                attachment = null,
                messages = _state.value.messages + ChatMessage("assistant", finalReply),
                error = resultError
            )
        }

        if (attachment == null) {
            api.chatJsonAsync(msg) { result ->
                onFinal(result.reply.orEmpty(), result.error)
            }
        } else {
            api.chatMultipartAsync(
                contentResolver = contentResolver,
                message = msg,
                fileUri = attachment.uri,
                fileName = attachment.name,
                mimeType = attachment.mimeType
            ) { result ->
                onFinal(result.reply.orEmpty(), result.error)
            }
        }
    }
}
