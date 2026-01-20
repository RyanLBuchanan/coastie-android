package ai.adaskids.coastie.ui.scenarios

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ai.adaskids.coastie.ui.AppState
import ai.adaskids.coastie.ui.PendingPrompt

enum class Scenario {
    Accessibility,
    AIResilientAssessment,
    RubricIntegrity
}

private data class ScenarioCard(
    val scenario: Scenario,
    val title: String,
    val subtitle: String
)

@Composable
fun ScenariosScreen(
    appState: AppState,
    onGoEdit: () -> Unit
) {
    val cards = listOf(
        ScenarioCard(
            scenario = Scenario.Accessibility,
            title = "Make a Canvas page accessible",
            subtitle = "Check headings, alt text, links, contrast, and structure."
        ),
        ScenarioCard(
            scenario = Scenario.AIResilientAssessment,
            title = "Convert a quiz to AI-resilient assessment",
            subtitle = "Shift toward authentic tasks, reasoning, and process evidence."
        ),
        ScenarioCard(
            scenario = Scenario.RubricIntegrity,
            title = "Build a rubric + integrity note",
            subtitle = "Create criteria, performance levels, and a student-facing integrity statement."
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFF9FBFF),
                        Color(0xFFEEF3FB),
                        Color(0xFFE9F6F7)
                    )
                )
            )
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // HERO
            item {
                HeroCard()
            }

            // SECTION HEADER
            item {
                Text(
                    text = "Quick scenarios",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // SCENARIO CARDS
            items(cards) { card ->
                ScenarioButton(
                    title = card.title,
                    subtitle = card.subtitle,
                    onClick = {
                        val (title, prompt) = scenarioPrompt(card.scenario)
                        appState.setPending(
                            PendingPrompt(
                                scenario = card.scenario,
                                title = title,
                                prompt = prompt
                            )
                        )
                        onGoEdit()
                    }
                )
            }

            // INTEGRITY CHIP
            item {
                AssistChip(
                    onClick = { },
                    label = { Text("Integrity-first: Coastie will not complete graded work for students.") }
                )
            }
        }
    }
}

private fun scenarioPrompt(s: Scenario): Pair<String, String> =
    when (s) {
        Scenario.Accessibility ->
            "Make a Canvas page accessible" to
                    """
                I’m working on a Canvas page for COASTAL. Please help me improve accessibility and clarity.

                1) Provide an accessibility checklist.
                2) Identify issues with headings, links, alt text, contrast, and structure.
                3) Suggest Canvas-friendly corrections.

                Content (paste below):
                """.trimIndent()

        Scenario.AIResilientAssessment ->
            "Convert a quiz to AI-resilient assessment" to
                    """
                I have a quiz or assessment that may be vulnerable to AI-assisted completion.

                1) Identify vulnerabilities.
                2) Propose an AI-resilient redesign emphasizing reasoning and authenticity.
                3) Include a student-facing integrity note.

                Assessment (paste below):
                """.trimIndent()

        Scenario.RubricIntegrity ->
            "Build a rubric + integrity note" to
                    """
                Please create a rubric and an integrity note for this assignment.

                1) Rubric criteria with performance levels.
                2) Guidance for student success.
                3) Ethical AI use note.

                Assignment description (paste below):
                """.trimIndent()
    }

@Composable
private fun HeroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xCCFFFFFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFF0B1220), RoundedCornerShape(16.dp)),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text("🌊", style = MaterialTheme.typography.titleLarge)
                }

                Column {
                    Text(
                        text = "Coastal CTL Assistant",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Make course design feel as smooth as the shoreline.",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Text(
                text = "Coastie supports ethical, student-first AI use for Canvas design, assessment resilience, and academic integrity.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FeatureTile("Canvas clarity", "Accessible pages", Modifier.weight(1f))
                FeatureTile("Assessment design", "AI-resilient tasks", Modifier.weight(1f))
                FeatureTile("Ethics coaching", "Student growth", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun FeatureTile(
    label: String,
    value: String,
    modifier: Modifier
) {
    Surface(
        modifier = modifier.padding(4.dp),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xBFFFFFFF),
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ScenarioButton(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xEFFFFFFF)),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
