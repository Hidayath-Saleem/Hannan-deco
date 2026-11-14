package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.DecoCurriculum
import com.example.engine.Cpu8BitEngine
import com.example.engine.FdePhase
import com.example.ui.components.LabSectionCard
import com.example.ui.components.SignalReadoutChip
import com.example.ui.components.StepExplanationCard
import com.example.ui.components.TheorySummaryCard
import com.example.ui.theme.LocalDecoSignalColors
import com.example.ui.theme.MonoCodeStyle
import com.example.ui.theme.MonoValueStyle

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Unit4Screen(modifier: Modifier = Modifier) {
    val unit = DecoCurriculum.units[3]
    var assemblyText by rememberSaveable { mutableStateOf(Cpu8BitEngine.presets.first().code) }
    var cpuState by remember {
        mutableStateOf(Cpu8BitEngine.assemble(Cpu8BitEngine.presets.first().code).getOrThrow())
    }
    var compileError by remember { mutableStateOf<String?>(null) }
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
                TheorySummaryCard(unit = unit, initiallyExpanded = false)
            }

            item {
                LabSectionCard(
                    title = "8-Bit CPU & Fetch-Decode-Execute Cycle",
                    subtitle = "Step phase-by-phase (FETCH → DECODE → EXECUTE) and watch buses, ALU, and registers update."
                ) {
                    // Phase Indicator Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(FdePhase.FETCH, FdePhase.DECODE, FdePhase.EXECUTE, FdePhase.HALTED).forEach { phase ->
                            val isActive = cpuState.phase == phase
                            val bg by animateColorAsState(
                                targetValue = when {
                                    isActive && phase == FdePhase.HALTED -> signals.miss.copy(alpha = 0.2f)
                                    isActive -> signals.signalHigh.copy(alpha = 0.22f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                },
                                label = "phase_bg"
                            )
                            Surface(
                                modifier = Modifier.weight(1f),
                                color = bg,
                                border = BorderStroke(
                                    1.5.dp,
                                    if (isActive) signals.signalHigh else MaterialTheme.colorScheme.outlineVariant
                                ),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = phase.label,
                                    style = MonoCodeStyle.copy(fontWeight = FontWeight.Bold),
                                    color = if (isActive) signals.signalHigh else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Control Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { cpuState = Cpu8BitEngine.stepPhase(cpuState) },
                            enabled = cpuState.phase != FdePhase.HALTED,
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("cpu_step_phase_btn")
                        ) {
                            Icon(Icons.Outlined.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Step Phase")
                        }
                        OutlinedButton(
                            onClick = { cpuState = Cpu8BitEngine.stepFullInstruction(cpuState) },
                            enabled = cpuState.phase != FdePhase.HALTED,
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("cpu_step_instr_btn")
                        ) {
                            Icon(Icons.Outlined.SkipNext, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Step Instr")
                        }
                        OutlinedButton(
                            onClick = {
                                Cpu8BitEngine.assemble(assemblyText)
                                    .onSuccess {
                                        cpuState = it
                                        compileError = null
                                    }
                                    .onFailure { compileError = it.message }
                            },
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("cpu_reset_btn")
                        ) {
                            Icon(Icons.Outlined.Refresh, contentDescription = "Reset CPU")
                        }
                    }

                    // Datapath Architectural Blocks
                    Text(
                        text = "CPU DATAPATH & REGISTERS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DatapathBox(
                            title = "PC",
                            value = "0x${hex2(cpuState.pc)}",
                            active = "PC" in cpuState.lastActiveBlocks,
                            modifier = Modifier.weight(1f)
                        )
                        DatapathBox(
                            title = "MAR",
                            value = "0x${hex2(cpuState.mar)}",
                            active = "MAR" in cpuState.lastActiveBlocks,
                            modifier = Modifier.weight(1f)
                        )
                        DatapathBox(
                            title = "MDR",
                            value = "0x${hex2(cpuState.mdr)}",
                            active = "MDR" in cpuState.lastActiveBlocks,
                            modifier = Modifier.weight(1f)
                        )
                        DatapathBox(
                            title = "IR",
                            value = "0x${hex2(cpuState.ir)}",
                            active = "IR" in cpuState.lastActiveBlocks,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        cpuState.registers.forEachIndexed { idx, value ->
                            DatapathBox(
                                title = "R$idx",
                                value = "$value (0x${hex2(value)})",
                                active = "REG" in cpuState.lastActiveBlocks,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DatapathBox(
                            title = "Control Unit",
                            value = cpuState.decodedCurrent?.sourceLine ?: "IDLE",
                            active = "CU" in cpuState.lastActiveBlocks,
                            modifier = Modifier.weight(1.4f)
                        )
                        DatapathBox(
                            title = "ALU / ACC",
                            value = "${cpuState.acc} (0x${hex2(cpuState.acc)})",
                            active = "ALU" in cpuState.lastActiveBlocks || "ACC" in cpuState.lastActiveBlocks,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Status Flags
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SignalReadoutChip("Z (Zero)", if (cpuState.flagZ) "1" else "0", cpuState.flagZ)
                        SignalReadoutChip("C (Carry)", if (cpuState.flagC) "1" else "0", cpuState.flagC)
                        SignalReadoutChip("N (Neg)", if (cpuState.flagN) "1" else "0", cpuState.flagN)
                        SignalReadoutChip("Cycles", cpuState.cycleCount.toString(), true)
                    }
                }
            }

            item {
                StepExplanationCard(
                    title = "Micro-Operation Explanation",
                    steps = listOf(cpuState.stepExplanation)
                )
            }

            item {
                LabSectionCard(
                    title = "Tiny Assembly Editor",
                    subtitle = "Supported: LOAD Rd, #imm | LOAD Rd, [addr] | STORE Rs, [addr] | ADD/SUB/AND/OR/XOR Rd, Rs | INC/DEC Rd | JMP/JZ addr | HLT"
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Cpu8BitEngine.presets.forEach { preset ->
                            FilterChip(
                                selected = assemblyText.trim() == preset.code.trim(),
                                onClick = {
                                    assemblyText = preset.code
                                    Cpu8BitEngine.assemble(preset.code).onSuccess {
                                        cpuState = it
                                        compileError = null
                                    }
                                },
                                label = { Text(preset.name) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = assemblyText,
                        onValueChange = { assemblyText = it },
                        label = { Text("Assembly Source") },
                        textStyle = MonoCodeStyle,
                        minLines = 5,
                        maxLines = 10,
                        isError = compileError != null,
                        supportingText = compileError?.let { err -> { Text(err) } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cpu_assembly_editor")
                    )

                    Button(
                        onClick = {
                            Cpu8BitEngine.assemble(assemblyText)
                                .onSuccess {
                                    cpuState = it
                                    compileError = null
                                }
                                .onFailure { compileError = it.message }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("cpu_assemble_btn")
                    ) {
                        Text("Assemble & Load into RAM")
                    }

                    // Compact 32-Byte RAM Hex Dump
                    Text(
                        text = "32-BYTE MAIN MEMORY (RAM 0x00–0x1F)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (row in 0 until 4) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val baseAddr = row * 8
                                Text(
                                    text = "0x${hex2(baseAddr)}:",
                                    style = MonoCodeStyle,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .width(44.dp)
                                        .align(Alignment.CenterVertically)
                                )
                                for (col in 0 until 8) {
                                    val addr = baseAddr + col
                                    val value = cpuState.ram[addr]
                                    val isPc = cpuState.pc == addr && cpuState.phase != FdePhase.HALTED
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        color = if (isPc) {
                                            signals.clockPulse.copy(alpha = 0.25f)
                                        } else if (value != 0) {
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                        },
                                        border = if (isPc) BorderStroke(1.dp, signals.clockPulse) else null,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 5.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = hex2(value),
                                                style = MonoCodeStyle,
                                                color = if (isPc) signals.clockPulse else MaterialTheme.colorScheme.onSurface
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
}

@Composable
private fun DatapathBox(
    title: String,
    value: String,
    active: Boolean,
    modifier: Modifier = Modifier
) {
    val signals = LocalDecoSignalColors.current
    val bg by animateColorAsState(
        targetValue = if (active) signals.signalHigh.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        label = "dp_bg"
    )
    Surface(
        modifier = modifier,
        color = bg,
        border = BorderStroke(
            width = if (active) 1.5.dp else 1.dp,
            color = if (active) signals.signalHigh else MaterialTheme.colorScheme.outlineVariant
        ),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = if (active) signals.signalHigh else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                style = MonoCodeStyle.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

private fun hex2(v: Int): String = (v and 0xFF).toString(16).uppercase().padStart(2, '0')
