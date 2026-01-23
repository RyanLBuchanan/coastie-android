package ai.adaskids.coastie.ui.exports

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ai.adaskids.coastie.ui.ExportStore
import kotlinx.coroutines.launch

private enum class ExportFormat(val label: String) {
    Plain("Plain text"),
    CanvasHtml("Canvas-ready HTML"),
    Announcement("Announcement")
}

@Composable
fun ExportsScreen() {
    val ctx = LocalContext.current
    val exportState by ExportStore.state.collectAsState()

    val reply = exportState.lastAssistantReply?.trim().orEmpty()
    val prompt = exportState.lastUserPrompt?.trim().orEmpty()

    var format by remember { mutableStateOf(ExportFormat.Plain) }

    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val exportText = remember(reply, prompt, format) {
        when (format) {
            ExportFormat.Plain -> buildPlainExport(prompt, reply)
            ExportFormat.CanvasHtml -> buildCanvasHtmlExport(prompt, reply)
            ExportFormat.Announcement -> buildAnnouncementExport(prompt, reply)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snack) },
        bottomBar = {
            // Sticky actions: always visible for demo impact
            Surface(tonalElevation = 2.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                if (reply.isBlank()) {
                                    scope.launch { snack.showSnackbar("Nothing to copy yet. Send a chat first.") }
                                } else {
                                    copyToClipboard(ctx, exportText)
                                    scope.launch { snack.showSnackbar("Copied to clipboard") }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) { Text("Copy") }

                        OutlinedButton(
                            onClick = {
                                if (reply.isBlank()) {
                                    scope.launch { snack.showSnackbar("Nothing to share yet. Send a chat first.") }
                                } else {
                                    shareText(ctx, exportText, subject = "Coastie Export")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) { Text("Share") }
                    }

                    Text(
                        text = "Powered by Microsoft Azure AI Foundry • Inputs are not used to train public models • Do not include student PII",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    ) { padding ->
        val pageScroll = rememberScrollState()
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(pageScroll),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Exports",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Export content from the most recent Coastie reply. (Demo-first: in-memory only.)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // If nothing to export yet, show a friendly empty state
            if (reply.isBlank()) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "No reply to export yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Go to Chat, send a message, then return here to copy/share it.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                return@Column
            }

            // Format picker
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Format",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ExportChip(
                            selected = format == ExportFormat.Plain,
                            label = ExportFormat.Plain.label,
                            onClick = { format = ExportFormat.Plain }
                        )
                        ExportChip(
                            selected = format == ExportFormat.CanvasHtml,
                            label = ExportFormat.CanvasHtml.label,
                            onClick = { format = ExportFormat.CanvasHtml }
                        )
                        ExportChip(
                            selected = format == ExportFormat.Announcement,
                            label = ExportFormat.Announcement.label,
                            onClick = { format = ExportFormat.Announcement }
                        )
                    }
                }
            }

            // Preview (fixed height + internal scroll so it doesn’t push buttons off-screen)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Preview",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))

                    val previewScroll = rememberScrollState()
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp) // tweakable; keeps action bar visible
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .verticalScroll(previewScroll)
                        ) {
                            Text(
                                text = exportText,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Tip: Choose “Canvas-ready HTML” when you want something you can paste into a Canvas page editor.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Add a little extra space above bottom bar so content doesn’t feel cramped
            Spacer(Modifier.height(90.dp))
        }
    }
}

@Composable
private fun ExportChip(selected: Boolean, label: String, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) }
    )
}

private fun copyToClipboard(ctx: Context, text: String) {
    val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("Coastie Export", text))
}

private fun shareText(ctx: Context, text: String, subject: String) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    ctx.startActivity(Intent.createChooser(sendIntent, "Share export"))
}

private fun buildPlainExport(prompt: String, reply: String): String {
    val p = if (prompt.isBlank()) "" else "Prompt:\n$prompt\n\n"
    return p + "Coastie Reply:\n$reply"
}

private fun buildAnnouncementExport(prompt: String, reply: String): String {
    // Instructor-facing announcement draft (clean, short, stakeholder-friendly)
    return """
Course Announcement (Draft)

Context:
${prompt.ifBlank { "(No prompt captured.)" }}

Message:
${reply.trim()}

Closing:
If you have questions or need support, please reach out. The CTL team is happy to help.
""".trim()
}

private fun buildCanvasHtmlExport(prompt: String, reply: String): String {
    // Lightweight Canvas-friendly HTML scaffold (simple + accessible).
    val safePrompt = escapeHtml(prompt.ifBlank { "Instructor request not captured." })
    val safeReply = escapeHtml(reply)

    return """
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8" />
  <title>Coastie Export</title>
</head>
<body>
  <h2>Overview</h2>
  <p><strong>Instructor request:</strong> $safePrompt</p>

  <h2>Draft Content</h2>
  <p>$safeReply</p>

  <hr />
  <p style="font-size: 12px; color: #666;">
    Generated with Coastie (COASTAL CTL demo). Review for accuracy, accessibility, and policy alignment.
  </p>
</body>
</html>
""".trim()
}

private fun escapeHtml(s: String): String {
    return s
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;")
        .replace("\n", "<br/>")
}
