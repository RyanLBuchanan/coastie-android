package ai.adaskids.coastie.ui.exports

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
    Announcement("Announcement text")
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
        snackbarHost = { SnackbarHost(hostState = snack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Exports",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Export content from the most recent Coastie reply. (Demo-first: no history persistence here.)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (reply.isBlank()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
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
                            text = "Go to Chat, send a message, then come back here to copy/share the result.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                return@Column
            }

            // Format picker
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Format",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

            // Preview
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Preview",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = exportText,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Actions
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        copyToClipboard(ctx, exportText)
                        scope.launch { snack.showSnackbar("Copied to clipboard") }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Copy") }

                OutlinedButton(
                    onClick = {
                        shareText(ctx, exportText, subject = "Coastie Export")
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
    val chooser = Intent.createChooser(sendIntent, "Share export")
    ctx.startActivity(chooser)
}

private fun buildPlainExport(prompt: String, reply: String): String {
    val p = if (prompt.isBlank()) "" else "Prompt:\n$prompt\n\n"
    return p + "Coastie Reply:\n$reply"
}

private fun buildAnnouncementExport(prompt: String, reply: String): String {
    // Demo-friendly heuristic: turn the reply into an instructor-facing announcement draft.
    return """
Title: Course Update / Instructional Note

Context (from instructor):
${prompt.ifBlank { "(No prompt captured)" }}

Draft announcement:
${reply.trim()}

Suggested closing:
If you have questions or need accommodations, please reach out. We’re here to support you.
""".trim()
}

private fun buildCanvasHtmlExport(prompt: String, reply: String): String {
    // Lightweight Canvas-ready HTML scaffold (simple + accessible, no heavy styling).
    // Stakeholders will "get it" immediately.
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
