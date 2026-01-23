package ai.adaskids.coastie.ui.chat

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.coastal.coastie.core.ui.MarkdownText
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(vm: ChatViewModel) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Auto-scroll when new content appears
    LaunchedEffect(state.messages.size, state.isLoading) {
        if (state.messages.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(state.messages.size - 1)
            }
        }
    }

    val bubbleShapeUser = RoundedCornerShape(18.dp, 18.dp, 6.dp, 18.dp)
    val bubbleShapeAssistant = RoundedCornerShape(18.dp, 18.dp, 18.dp, 6.dp)

    // Document picker: allows PDF/images/text/word docs (backend allows one file)
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult

        // Best effort: persist permission so it works reliably across configuration changes
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Throwable) {
            // Not all providers allow persistable permissions. Safe to ignore for demo.
        }

        val meta = queryMeta(context, uri)
        val name = meta.name ?: "attachment"
        val mime = meta.mimeType ?: (context.contentResolver.getType(uri) ?: "application/octet-stream")

        vm.setAttachment(
            AttachmentUi(
                uri = uri,
                name = name,
                mimeType = mime,
                sizeBytes = meta.sizeBytes
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Messages area
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = listState,
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            itemsIndexed(state.messages) { _, m ->
                val isUser = m.role == "user"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Column(modifier = Modifier.widthIn(max = 360.dp)) {

                        Text(
                            text = if (isUser) "You" else "Coastie",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )

                        Surface(
                            shape = if (isUser) bubbleShapeUser else bubbleShapeAssistant,
                            color = if (isUser)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = 1.dp
                        ) {
                            if (isUser) {
                                Text(
                                    text = m.text,
                                    modifier = Modifier.padding(14.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            } else {
                                // Assistant: render light markdown for professional formatting
                                Box(modifier = Modifier.padding(14.dp)) {
                                    MarkdownText(text = m.text)
                                }
                            }
                        }
                    }
                }
            }

            if (state.isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Column(modifier = Modifier.widthIn(max = 360.dp)) {
                            Text(
                                text = "Coastie",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )

                            Surface(
                                shape = bubbleShapeAssistant,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                tonalElevation = 1.dp
                            ) {
                                Text(
                                    text = "Typing…",
                                    modifier = Modifier.padding(14.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Error
        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        // Attachment pill (if present)
        state.attachment?.let { att ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.AttachFile, contentDescription = "Attachment")
                    Spacer(Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = att.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        val sizeLabel = att.sizeBytes?.let { formatBytes(it) } ?: "Size unknown"
                        Text(
                            text = "${att.mimeType} • $sizeLabel",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = vm::clearAttachment) {
                        Icon(Icons.Filled.Close, contentDescription = "Remove attachment")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }

        // Trust footer (above input; doesn't block bottom nav)
        Text(
            text = "Powered by Microsoft Azure AI Foundry • Inputs are not used to train public models • Do not include student PII",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(8.dp))

        // Input row (attach + text + send)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    picker.launch(
                        arrayOf(
                            "application/pdf",
                            "image/*",
                            "text/plain",
                            "application/msword",
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                        )
                    )
                },
                enabled = !state.isLoading,
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp)
            ) {
                Icon(Icons.Filled.AttachFile, contentDescription = "Attach file")
            }

            OutlinedTextField(
                value = state.input,
                onValueChange = vm::setInput,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask Coastie…") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                enabled = !state.isLoading
            )

            Button(
                onClick = { vm.send(context.contentResolver) },
                enabled = !state.isLoading,
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Text("Send")
            }
        }
    }
}

/** Simple metadata helper */
private data class UriMeta(
    val name: String?,
    val sizeBytes: Long?,
    val mimeType: String?
)

private fun queryMeta(context: Context, uri: Uri): UriMeta {
    var name: String? = null
    var size: Long? = null

    val mime = context.contentResolver.getType(uri)

    val cursor: Cursor? = try {
        context.contentResolver.query(uri, null, null, null, null)
    } catch (_: Throwable) {
        null
    }

    cursor?.use {
        val nameIdx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIdx = it.getColumnIndex(OpenableColumns.SIZE)
        if (it.moveToFirst()) {
            if (nameIdx >= 0) name = it.getString(nameIdx)
            if (sizeIdx >= 0) size = it.getLong(sizeIdx)
        }
    }

    return UriMeta(name = name, sizeBytes = size, mimeType = mime)
}

private fun formatBytes(bytes: Long): String {
    val kb = 1024.0
    val mb = kb * 1024.0
    val gb = mb * 1024.0
    return when {
        bytes >= gb -> String.format("%.2f GB", bytes / gb)
        bytes >= mb -> String.format("%.2f MB", bytes / mb)
        bytes >= kb -> String.format("%.1f KB", bytes / kb)
        else -> "$bytes B"
    }
}
