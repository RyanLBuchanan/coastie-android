package ai.adaskids.coastie.ui.scenarios

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ScenariosScreen(
    onPickScenario: (Scenario) -> Unit
) {
    // Background gradient + soft blobs (native approximation of your web backdrop)
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
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            HeroCard()

            Text(
                text = "Quick scenarios",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            ScenarioButton(
                title = "Make a Canvas page accessible",
                subtitle = "Check headings, alt text, links, contrast, and structure.",
                onClick = { onPickScenario(Scenario.Accessibility) }
            )

            ScenarioButton(
                title = "Convert a quiz to AI-resilient assessment",
                subtitle = "Shift toward authentic tasks, reasoning, and process evidence.",
                onClick = { onPickScenario(Scenario.AIResilientAssessment) }
            )

            ScenarioButton(
                title = "Build a rubric + integrity note",
                subtitle = "Create criteria + performance levels + a student-facing integrity statement.",
                onClick = { onPickScenario(Scenario.RubricIntegrity) }
            )

            Spacer(Modifier.height(6.dp))

            AssistChip(
                onClick = { /* later: show policy */ },
                label = { Text("Integrity-first: Coastie won’t complete graded work for students.") }
            )
        }
    }
}

@Composable
private fun HeroCard() {
    val glass = Color(0xCCFFFFFF) // translucent “glass”
    val border = Color(0x66FFFFFF)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = glass),
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
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0B1220)),
                ) {
                    // simple logo placeholder (matches your 🌊 vibe)
                    Text(
                        text = "🌊",
                        modifier = Modifier.padding(start = 10.dp, top = 8.dp),
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Coastal CTL Assistant",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Make course design feel as smooth as the shoreline.",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Text(
                text = "Coastie guides educators with ethical, student-first AI support. Build clearer Canvas pages, resilient assessments, and feedback that nurtures academic integrity.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FeatureTile("Canvas clarity", "Accessible pages")
                FeatureTile("Assessment design", "AI-resilient tasks")
                FeatureTile("Ethics coaching", "Student growth")
            }

            // “Live demo” pill like your web header
            Surface(
                color = Color(0xFF0B1220),
                contentColor = Color.White,
                shape = RoundedCornerShape(999.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("●", color = Color(0xFF24B0B8), fontWeight = FontWeight.Black)
                    Text("CTL demonstration agent", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun FeatureTile(label: String, value: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
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
private fun ScenarioButton(title: String, subtitle: String, onClick: () -> Unit) {
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

enum class Scenario {
    Accessibility,
    AIResilientAssessment,
    RubricIntegrity
}
