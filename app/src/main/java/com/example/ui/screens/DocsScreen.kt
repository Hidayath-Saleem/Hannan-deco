package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.example.data.DecoCurriculum
import com.example.ui.components.LabSectionCard
import com.example.ui.theme.MonoCodeStyle

@Composable
fun DocsScreen(
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var subTab by rememberSaveable { mutableIntStateOf(0) }
    val clipboard = LocalClipboardManager.current

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TabRow(
                    selectedTabIndex = subTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.clip(MaterialTheme.shapes.medium)
                ) {
                    Tab(
                        selected = subTab == 0,
                        onClick = { subTab = 0 },
                        text = { Text("Project Report") },
                        modifier = Modifier.testTag("docs_tab_report")
                    )
                    Tab(
                        selected = subTab == 1,
                        onClick = { subTab = 1 },
                        text = { Text("5-Min Demo Script") },
                        modifier = Modifier.testTag("docs_tab_demo")
                    )
                }
            }

            if (subTab == 0) {
                item {
                    OutlinedButton(
                        onClick = {
                            val full = DecoCurriculum.projectReportSections.joinToString("\n\n") {
                                "${it.title}\n${it.content}"
                            }
                            clipboard.setText(AnnotatedString(full))
                            onShowMessage("Project report copied to clipboard.")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                    ) {
                        Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Copy Full Project Report")
                    }
                }
                items(DecoCurriculum.projectReportSections) { section ->
                    LabSectionCard(title = section.title) {
                        Text(
                            text = section.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                item {
                    OutlinedButton(
                        onClick = {
                            val full = DecoCurriculum.demoScriptSegments.joinToString("\n\n") {
                                "[${it.timeRange}] ${it.title}\nSay: ${it.speakerNotes}\nDo: ${it.appActions}"
                            }
                            clipboard.setText(AnnotatedString(full))
                            onShowMessage("5-minute demo script copied to clipboard.")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                    ) {
                        Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Copy 5-Minute Demo Script")
                    }
                }
                items(DecoCurriculum.demoScriptSegments) { seg ->
                    LabSectionCard(title = seg.title) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = seg.timeRange,
                                style = MonoCodeStyle,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = "Speaker Notes: ${seg.speakerNotes}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                                Text(
                                    text = "Live Action: ${seg.appActions}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
