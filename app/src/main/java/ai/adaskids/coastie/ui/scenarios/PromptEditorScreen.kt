package ai.adaskids.coastie.ui.scenarios

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ai.adaskids.coastie.ui.AppState
import ai.adaskids.coastie.ui.PendingPrompt

@Composable
fun PromptEditorScreen(
    appState: AppState,
    onRun: (String) -> Unit,
    onBack: () -> Unit
) {
    val pending by appState.pending.collectAsState()

    // If somehow opened with no pending prompt, bounce back.
    if (pending == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    val title = pending!!.title
    var prompt by remember { mutableStateOf(pending!!.prompt) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))

        Text(
            "Edit the prompt if needed, then run it in Chat.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            label = { Text("Prompt") }
        )

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                Text("Back")
            }
            Button(
                onClick = {
                    appState.setPending(pending!!.copy(prompt = prompt))
                    onRun(prompt)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Run in Chat")
            }
        }
    }
}
