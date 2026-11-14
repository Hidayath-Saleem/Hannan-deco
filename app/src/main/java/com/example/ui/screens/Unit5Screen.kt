package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.example.engine.CacheMappingMode
import com.example.engine.CacheSimulatorEngine
import com.example.engine.IoTransferEngine
import com.example.engine.IoTransferMode
import com.example.engine.PagePolicy
import com.example.engine.PagingSimulatorEngine
import com.example.ui.components.LabSectionCard
import com.example.ui.components.PresetChipRow
import com.example.ui.components.SignalReadoutChip
import com.example.ui.components.StepExplanationCard
import com.example.ui.components.TheorySummaryCard
import com.example.ui.theme.LocalDecoSignalColors
import com.example.ui.theme.MonoCodeStyle
import com.example.ui.theme.MonoValueStyle

@Composable
fun Unit5Screen(modifier: Modifier = Modifier) {
    val unit = DecoCurriculum.units[4]
    var subTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("Cache Mapping", "Virtual Paging", "Interrupts & DMA")

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TheorySummaryCard(unit = unit, initiallyExpanded = false)
            }
            item {
                TabRow(
                    selectedTabIndex = subTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.clip(MaterialTheme.shapes.medium)
                ) {
                    tabs.forEachIndexed { idx, label ->
                        Tab(
                            selected = subTab == idx,
                            onClick = { subTab = idx },
                            text = { Text(label, maxLines = 1) },
                            modifier = Modifier.testTag("unit5_tab_$idx")
                        )
                    }
                }
            }
            when (subTab) {
                0 -> item { CacheMappingSection() }
                1 -> item { VirtualPagingSection() }
                else -> item { InterruptDmaSection() }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CacheMappingSection() {
    var mode by rememberSaveable { mutableStateOf(CacheMappingMode.DIRECT) }
    var sequenceInput by rememberSaveable { mutableStateOf("0, 4, 0, 8, 1, 0, 4, 1") }
    var simState by remember {
        mutableStateOf(
            CacheSimulatorEngine.reset(
                CacheMappingMode.DIRECT,
                4,
                CacheSimulatorEngine.parseAddressSequence("0, 4, 0, 8, 1, 0, 4, 1")
            )
        )
    }
    val signals = LocalDecoSignalColors.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LabSectionCard(
            title = "Cache Mapping & Hit/Miss Visualizer",
            subtitle = mode.subtitle
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CacheMappingMode.entries.forEach { m ->
                    FilterChip(
                        selected = mode == m,
                        onClick = {
                            mode = m
                            val parsed = CacheSimulatorEngine.parseAddressSequence(sequenceInput)
                            simState = CacheSimulatorEngine.reset(m, 4, parsed)
                        },
                        label = { Text(m.title) },
                        modifier = Modifier.testTag("cache_mode_${m.name}")
                    )
                }
            }

            OutlinedTextField(
                value = sequenceInput,
                onValueChange = { raw ->
                    sequenceInput = raw
                    val parsed = CacheSimulatorEngine.parseAddressSequence(raw)
                    if (parsed.isNotEmpty()) {
                        simState = CacheSimulatorEngine.reset(mode, 4, parsed)
                    }
                },
                label = { Text("Block Address Trace (comma-separated 0..63)") },
                textStyle = MonoValueStyle,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            PresetChipRow(
                presets = listOf("Thrash (0,4,0,8)", "Spatial Loop", "Set Conflict"),
                onSelect = { idx ->
                    val raw = CacheSimulatorEngine.sampleSequences[idx].second
                    sequenceInput = raw
                    simState = CacheSimulatorEngine.reset(
                        mode,
                        4,
                        CacheSimulatorEngine.parseAddressSequence(raw)
                    )
                }
            )

            // Trace sequence pills
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                simState.sequence.forEachIndexed { idx, addr ->
                    val isDone = idx < simState.nextPointer
                    val isNext = idx == simState.nextPointer
                    Surface(
                        color = when {
                            isNext -> signals.clockPulse.copy(alpha = 0.25f)
                            isDone -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        border = BorderStroke(
                            1.dp,
                            if (isNext) signals.clockPulse else MaterialTheme.colorScheme.outlineVariant
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = addr.toString(),
                            style = MonoCodeStyle.copy(fontWeight = FontWeight.Bold),
                            color = if (isNext) signals.clockPulse else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Step Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { simState = CacheSimulatorEngine.step(simState) },
                    enabled = simState.nextPointer < simState.sequence.size,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("cache_step_btn")
                ) {
                    Text("Step Access")
                }
                OutlinedButton(
                    onClick = {
                        var s = simState
                        while (s.nextPointer < s.sequence.size) {
                            s = CacheSimulatorEngine.step(s)
                        }
                        simState = s
                    },
                    enabled = simState.nextPointer < simState.sequence.size,
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Run All")
                }
                OutlinedButton(
                    onClick = {
                        simState = CacheSimulatorEngine.reset(
                            mode,
                            4,
                            CacheSimulatorEngine.parseAddressSequence(sequenceInput)
                        )
                    },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Reset")
                }
            }

            // Hit / Miss Ratio Meter
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SignalReadoutChip("Hits", simState.hits.toString(), true)
                        SignalReadoutChip("Misses", simState.misses.toString(), false)
                        SignalReadoutChip("Hit Ratio", "${simState.hitRatioPercent}%", simState.hitRatioPercent >= 50)
                    }
                    LinearProgressIndicator(
                        progress = { simState.hitRatioPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = signals.hit,
                        trackColor = signals.miss.copy(alpha = 0.25f)
                    )
                }
            }

            // 4-Line Cache Table
            Surface(
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("Line / Set", style = MonoCodeStyle, modifier = Modifier.weight(1.1f))
                        Text("Valid", style = MonoCodeStyle, modifier = Modifier.weight(0.7f))
                        Text("Tag", style = MonoCodeStyle, modifier = Modifier.weight(0.7f))
                        Text("Block", style = MonoCodeStyle, modifier = Modifier.weight(0.9f))
                    }
                    simState.lines.forEach { slot ->
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        val highlighted = simState.lastTrace?.targetLine == slot.lineIndex
                        val traceHit = simState.lastTrace?.isHit == true
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    when {
                                        highlighted && traceHit -> signals.hit.copy(alpha = 0.14f)
                                        highlighted && !traceHit -> signals.miss.copy(alpha = 0.14f)
                                        else -> MaterialTheme.colorScheme.surface
                                    }
                                )
                                .padding(horizontal = 12.dp, vertical = 9.dp)
                        ) {
                            Text(
                                "L${slot.lineIndex} (S${slot.setIndex})",
                                style = MonoCodeStyle,
                                modifier = Modifier.weight(1.1f)
                            )
                            Text(
                                if (slot.valid) "1" else "0",
                                style = MonoValueStyle,
                                color = if (slot.valid) signals.hit else signals.signalLow,
                                modifier = Modifier.weight(0.7f)
                            )
                            Text(
                                slot.tag?.toString() ?: "-",
                                style = MonoValueStyle,
                                modifier = Modifier.weight(0.7f)
                            )
                            Text(
                                slot.blockAddress?.let { "Blk $it" } ?: "Empty",
                                style = MonoValueStyle,
                                color = if (slot.blockAddress != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(0.9f)
                            )
                        }
                    }
                }
            }
        }

        StepExplanationCard(
            title = "Cache Lookup & Replacement Trace",
            steps = listOfNotNull(
                simState.lastTrace?.let {
                    "[${it.missType}] ${it.explanation}"
                } ?: "Tap 'Step Access' to trace Tag/Index calculation and Hit/Miss classification."
            ),
            accentColor = when (simState.lastTrace?.isHit) {
                true -> signals.hit
                false -> signals.miss
                null -> MaterialTheme.colorScheme.primary
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VirtualPagingSection() {
    var policy by rememberSaveable { mutableStateOf(PagePolicy.LRU) }
    var pagingState by remember { mutableStateOf(PagingSimulatorEngine.reset(PagePolicy.LRU, listOf(1, 2, 3, 4, 1, 2, 5, 1, 2, 3))) }
    val signals = LocalDecoSignalColors.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LabSectionCard(
            title = "Virtual Memory Paging Visualizer",
            subtitle = "Translate Virtual Page Numbers (VPN) to 3 Physical RAM Frames using FIFO or LRU replacement."
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PagePolicy.entries.forEach { p ->
                    FilterChip(
                        selected = policy == p,
                        onClick = {
                            policy = p
                            pagingState = PagingSimulatorEngine.reset(p, pagingState.pageSequence)
                        },
                        label = { Text(p.title) }
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                pagingState.pageSequence.forEachIndexed { idx, vpn ->
                    val isNext = idx == pagingState.pointer
                    Surface(
                        color = if (isNext) signals.clockPulse.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, if (isNext) signals.clockPulse else MaterialTheme.colorScheme.outlineVariant),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "P$vpn",
                            style = MonoCodeStyle.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { pagingState = PagingSimulatorEngine.step(pagingState) },
                    enabled = pagingState.pointer < pagingState.pageSequence.size,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("paging_step_btn")
                ) {
                    Text("Next Page Access")
                }
                OutlinedButton(
                    onClick = { pagingState = PagingSimulatorEngine.reset(policy, pagingState.pageSequence) },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Reset")
                }
            }

            // Physical Frames
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                pagingState.frames.forEach { frame ->
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = if (frame.loadedPage != null) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Frame ${frame.frameIndex}", style = MaterialTheme.typography.labelSmall)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = frame.loadedPage?.let { "Page $it" } ?: "Empty",
                                style = MonoValueStyle.copy(fontWeight = FontWeight.Bold),
                                color = if (frame.loadedPage != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SignalReadoutChip("Page Hits", pagingState.hits.toString(), true)
                SignalReadoutChip("Page Faults", pagingState.faults.toString(), false)
                SignalReadoutChip("Phys Addr", pagingState.lastTranslatedPhysical, true)
            }
        }

        StepExplanationCard(
            title = "MMU Address Translation Trace",
            steps = listOf(pagingState.stepExplanation),
            accentColor = when (pagingState.lastIsFault) {
                false -> signals.hit
                true -> signals.miss
                null -> MaterialTheme.colorScheme.primary
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InterruptDmaSection() {
    var mode by rememberSaveable { mutableStateOf(IoTransferMode.DMA) }
    var activeStep by rememberSaveable { mutableIntStateOf(0) }
    val steps = remember(mode) { IoTransferEngine.stepsFor(mode) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LabSectionCard(
            title = "Interrupts & DMA I/O Explorer",
            subtitle = "Efficiency: ${mode.cpuEfficiency}"
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IoTransferMode.entries.forEach { m ->
                    FilterChip(
                        selected = mode == m,
                        onClick = { mode = m; activeStep = 0 },
                        label = { Text(m.title) }
                    )
                }
            }

            steps.forEachIndexed { idx, step ->
                val isSelected = idx == activeStep
                Surface(
                    onClick = { activeStep = idx },
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(
                        if (isSelected) 1.5.dp else 1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                step.phaseTitle,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Bus: ${step.busOwner}",
                                style = MonoCodeStyle,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Text(
                            "CPU: ${step.cpuState}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            step.detail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { activeStep = (activeStep - 1 + steps.size) % steps.size },
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Prev Step")
                }
                Button(
                    onClick = { activeStep = (activeStep + 1) % steps.size },
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Next Step")
                }
            }
        }
    }
}
