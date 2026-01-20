package ai.adaskids.coastie.ui.scenarios

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ai.adaskids.coastie.ui.AppState

@Composable
fun PromptEditorScreen(
    appState: AppState,
    onRun: () -> Unit,
    onBack: () -> Unit
) {
    val pending by appState.pending.collectAsState()

    if (pending == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    var prompt by remember(pending) { mutableStateOf(pending!!.prompt) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = pending!!.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Edit the prompt if needed, then run it in Chat.",
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Back")
            }

            Button(
                onClick = {
                    // Save edited prompt back into AppState BEFORE navigation
                    appState.setPending(pending!!.copy(prompt = prompt))
                    onRun()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Run in Chat")
            }
        }
    }
}
