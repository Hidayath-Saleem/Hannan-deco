package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.DecoCurriculum
import com.example.data.UserProgressState
import com.example.ui.components.LabSectionCard
import com.example.ui.theme.LocalDecoSignalColors
import com.example.ui.theme.MonoValueStyle

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuizScreen(
    selectedUnitId: Int,
    progress: UserProgressState,
    onSelectUnit: (Int) -> Unit,
    onQuizCompleted: (unitId: Int, score: Int) -> Unit,
    onResetScores: () -> Unit,
    modifier: Modifier = Modifier
) {
    val questions = remember(selectedUnitId) {
        DecoCurriculum.quizzes.filter { it.unitId == selectedUnitId }
    }
    var currentIndex by rememberSaveable(selectedUnitId) { mutableIntStateOf(0) }
    var selectedOption by rememberSaveable(selectedUnitId, currentIndex) { mutableStateOf<Int?>(null) }
    var currentRunScore by rememberSaveable(selectedUnitId) { mutableIntStateOf(0) }
    var finished by rememberSaveable(selectedUnitId) { mutableStateOf(false) }

    val signals = LocalDecoSignalColors.current

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Score Tracker Dashboard
                LabSectionCard(
                    title = "Score Tracker & Unit Mastery",
                    subtitle = "5 MCQs per unit (25 total). Best scores are saved automatically."
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.EmojiEvents,
                                contentDescription = "Score trophy",
                                tint = signals.clockPulse,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "${progress.totalBestScore} / ${progress.maxTotalScore}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Total Mastery (${(progress.totalBestScore * 100) / 25}%)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = onResetScores,
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                        ) {
                            Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Reset")
                        }
                    }

                    // Per-unit mini progress bars
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        DecoCurriculum.units.forEach { unit ->
                            val best = progress.bestScores[unit.id] ?: 0
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "U${unit.id}",
                                    style = MonoValueStyle,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.width(28.dp)
                                )
                                LinearProgressIndicator(
                                    progress = { best / 5f },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(7.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = if (best == 5) signals.hit else MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Text(
                                    text = "$best/5",
                                    style = MonoValueStyle,
                                    color = if (best == 5) signals.hit else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.width(34.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                // Unit Selector Chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DecoCurriculum.units.forEach { unit ->
                        FilterChip(
                            selected = selectedUnitId == unit.id,
                            onClick = { onSelectUnit(unit.id) },
                            label = { Text("U${unit.id}: ${unit.shortTitle}") },
                            modifier = Modifier.testTag("quiz_unit_chip_${unit.id}")
                        )
                    }
                }
            }

            item {
                if (finished) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.EmojiEvents,
                                contentDescription = "Completed quiz",
                                tint = signals.clockPulse,
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = "Unit $selectedUnitId Quiz Complete!",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = "You scored $currentRunScore / ${questions.size}",
                                style = MonoValueStyle.copy(fontWeight = FontWeight.Bold),
                                color = if (currentRunScore >= 4) signals.hit else MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (currentRunScore == 5) {
                                    "Perfect score! You mastered Unit $selectedUnitId."
                                } else {
                                    "Review the unit simulator and try again to reach 5/5!"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = {
                                    currentIndex = 0
                                    selectedOption = null
                                    currentRunScore = 0
                                    finished = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("quiz_retake_btn")
                            ) {
                                Text("Retake Unit $selectedUnitId Quiz")
                            }
                        }
                    }
                } else {
                    val q = questions[currentIndex]
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "UNIT $selectedUnitId • QUESTION ${currentIndex + 1} OF ${questions.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Run Score: $currentRunScore",
                                    style = MonoValueStyle,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            LinearProgressIndicator(
                                progress = { (currentIndex + 1) / questions.size.toFloat() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )

                            Text(
                                text = q.question,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            q.options.forEachIndexed { idx, optText ->
                                val answered = selectedOption != null
                                val isChosen = selectedOption == idx
                                val isCorrect = idx == q.correctIndex

                                val borderColor = when {
                                    !answered -> MaterialTheme.colorScheme.outlineVariant
                                    isCorrect -> signals.hit
                                    isChosen -> signals.miss
                                    else -> MaterialTheme.colorScheme.outlineVariant
                                }
                                val bgColor = when {
                                    !answered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                    isCorrect -> signals.hit.copy(alpha = 0.16f)
                                    isChosen -> signals.miss.copy(alpha = 0.16f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                                }

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 48.dp)
                                        .clip(MaterialTheme.shapes.medium)
                                        .clickable(enabled = !answered) {
                                            selectedOption = idx
                                            if (idx == q.correctIndex) {
                                                currentRunScore += 1
                                            }
                                        }
                                        .testTag("quiz_option_$idx"),
                                    color = bgColor,
                                    border = BorderStroke(if (answered && (isCorrect || isChosen)) 1.5.dp else 1.dp, borderColor),
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${('A' + idx)}.  $optText",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (answered && isCorrect) {
                                            Icon(Icons.Outlined.CheckCircle, contentDescription = "Correct", tint = signals.hit)
                                        } else if (answered && isChosen) {
                                            Icon(Icons.Outlined.Close, contentDescription = "Incorrect", tint = signals.miss)
                                        }
                                    }
                                }
                            }

                            AnimatedVisibility(visible = selectedOption != null) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                        shape = MaterialTheme.shapes.medium
                                    ) {
                                        Column(Modifier.padding(12.dp)) {
                                            Text(
                                                text = if (selectedOption == q.correctIndex) "CORRECT" else "EXPLANATION",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (selectedOption == q.correctIndex) signals.hit else MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = q.explanation,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            if (currentIndex + 1 < questions.size) {
                                                currentIndex += 1
                                                selectedOption = null
                                            } else {
                                                onQuizCompleted(selectedUnitId, currentRunScore)
                                                finished = true
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .defaultMinSize(minHeight = 48.dp)
                                            .testTag("quiz_next_btn")
                                    ) {
                                        Text(
                                            if (currentIndex + 1 < questions.size) "Next Question" else "Finish & Save Score"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
