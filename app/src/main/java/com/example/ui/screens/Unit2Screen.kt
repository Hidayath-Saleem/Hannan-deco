package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cable
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.data.DecoCurriculum
import com.example.engine.CircuitNode
import com.example.engine.CircuitWire
import com.example.engine.CombinationalIcEngine
import com.example.engine.CombinationalIcType
import com.example.engine.GateType
import com.example.engine.LogicCircuitEvaluator
import com.example.ui.components.BitToggleButton
import com.example.ui.components.LabSectionCard
import com.example.ui.components.PresetChipRow
import com.example.ui.components.SignalReadoutChip
import com.example.ui.components.StepExplanationCard
import com.example.ui.components.TheorySummaryCard
import com.example.ui.theme.LocalDecoSignalColors
import com.example.ui.theme.MonoCodeStyle
import com.example.ui.theme.MonoValueStyle
import kotlin.math.roundToInt

@Composable
fun Unit2Screen(modifier: Modifier = Modifier) {
    val unit = DecoCurriculum.units[1]
    var subTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("Gate Sandbox", "Combinational ICs")

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
                            text = { Text(label) },
                            modifier = Modifier.testTag("unit2_tab_$idx")
                        )
                    }
                }
            }
            when (subTab) {
                0 -> item { DragDropGateSandbox() }
                else -> item { CombinationalIcSection() }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DragDropGateSandbox() {
    var nodes by remember { mutableStateOf(LogicCircuitEvaluator.presetHalfAdder().first) }
    var wires by remember { mutableStateOf(LogicCircuitEvaluator.presetHalfAdder().second) }
    var wireSourceNodeId by remember { mutableStateOf<String?>(null) }
    var selectedNodeId by remember { mutableStateOf<String?>(null) }
    var nextNodeNum by remember { mutableIntStateOf(1) }

    val eval = remember(nodes, wires) {
        LogicCircuitEvaluator.evaluate(nodes, wires)
    }
    val signals = LocalDecoSignalColors.current
    val density = LocalDensity.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LabSectionCard(
            title = "Drag-and-Drop Logic Gate Simulator",
            subtitle = "Drag gates anywhere on the blueprint grid. Tap IN switches to toggle 0/1. Tap a node's 'OUT→' or Wire mode to connect pins."
        ) {
            PresetChipRow(
                presets = listOf("Half Adder", "2:1 MUX", "NAND XOR", "Empty"),
                onSelect = { idx ->
                    wireSourceNodeId = null
                    selectedNodeId = null
                    when (idx) {
                        0 -> LogicCircuitEvaluator.presetHalfAdder().let { nodes = it.first; wires = it.second }
                        1 -> LogicCircuitEvaluator.preset2To1Mux().let { nodes = it.first; wires = it.second }
                        2 -> LogicCircuitEvaluator.presetNandXor().let { nodes = it.first; wires = it.second }
                        else -> { nodes = emptyList(); wires = emptyList() }
                    }
                }
            )

            // Add Gate Palette
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GateType.entries.forEach { gateType ->
                    OutlinedButton(
                        onClick = {
                            val id = "g_${nextNodeNum++}"
                            val countLabel = nodes.count { it.type == gateType } + 1
                            val lbl = when (gateType) {
                                GateType.INPUT -> "IN$countLabel"
                                GateType.OUTPUT -> "LED$countLabel"
                                else -> "${gateType.label}$countLabel"
                            }
                            val x = when (gateType) {
                                GateType.INPUT -> 0.14f
                                GateType.OUTPUT -> 0.86f
                                else -> 0.45f + ((nextNodeNum % 3) * 0.06f)
                            }
                            val y = 0.2f + ((nodes.size % 4) * 0.18f)
                            nodes = nodes + CircuitNode(id, gateType, lbl, x, y)
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("add_gate_${gateType.name}")
                    ) {
                        Text("+ ${gateType.label}", style = MonoCodeStyle)
                    }
                }
            }

            // Wiring / Selection Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (wireSourceNodeId != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Cable,
                            contentDescription = "Wire mode",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (wireSourceNodeId == null) {
                                "Tap 'OUT' on any block to start a wire"
                            } else {
                                "Wiring from ${nodes.find { it.id == wireSourceNodeId }?.label ?: ""} -> Tap target Pin 1 or Pin 2"
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                if (selectedNodeId != null) {
                    OutlinedButton(
                        onClick = {
                            val delId = selectedNodeId
                            nodes = nodes.filterNot { it.id == delId }
                            wires = wires.filterNot { it.fromNodeId == delId || it.toNodeId == delId }
                            if (wireSourceNodeId == delId) wireSourceNodeId = null
                            selectedNodeId = null
                        },
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    ) {
                        Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete selected gate")
                    }
                }
            }

            // Interactive Drag & Wire Blueprint Canvas
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.large)
                    .testTag("logic_gate_canvas")
            ) {
                val widthPx = with(density) { maxWidth.toPx() }
                val heightPx = with(density) { maxHeight.toPx() }
                val nodeW = with(density) { 84.dp.toPx() }
                val nodeH = with(density) { 58.dp.toPx() }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Subtle engineering blueprint grid
                    val step = 28.dp.toPx()
                    var gx = step
                    while (gx < size.width) {
                        drawLine(
                            color = signals.gridLine.copy(alpha = 0.25f),
                            start = Offset(gx, 0f),
                            end = Offset(gx, size.height),
                            strokeWidth = 1f
                        )
                        gx += step
                    }
                    var gy = step
                    while (gy < size.height) {
                        drawLine(
                            color = signals.gridLine.copy(alpha = 0.25f),
                            start = Offset(0f, gy),
                            end = Offset(size.width, gy),
                            strokeWidth = 1f
                        )
                        gy += step
                    }

                    // Draw animated Bezier signal wires
                    wires.forEach { wire ->
                        val from = nodes.find { it.id == wire.fromNodeId }
                        val to = nodes.find { it.id == wire.toNodeId }
                        if (from != null && to != null) {
                            val startX = from.xFraction * size.width + nodeW / 2f
                            val startY = from.yFraction * size.height
                            val endX = to.xFraction * size.width - nodeW / 2f
                            val pinOffset = if (to.type.inputCount <= 1) 0f else if (wire.toInputPin == 0) -12.dp.toPx() else 12.dp.toPx()
                            val endY = to.yFraction * size.height + pinOffset
                            val ctrl = ((endX - startX) * 0.45f).coerceAtLeast(36f)

                            val path = Path().apply {
                                moveTo(startX, startY)
                                cubicTo(startX + ctrl, startY, endX - ctrl, endY, endX, endY)
                            }
                            val active = eval.nodeOutputs[from.id] == true
                            drawPath(
                                path = path,
                                color = if (active) signals.signalHigh else signals.signalLow,
                                style = Stroke(width = if (active) 4.dp.toPx() else 2.5.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }
                }

                // Draggable Gate Nodes
                nodes.forEach { node ->
                    val isHigh = eval.nodeOutputs[node.id] == true
                    val isSelected = selectedNodeId == node.id
                    val isWireSource = wireSourceNodeId == node.id
                    val leftPx = (node.xFraction * widthPx - nodeW / 2f).roundToInt()
                    val topPx = (node.yFraction * heightPx - nodeH / 2f).roundToInt()

                    Surface(
                        modifier = Modifier
                            .offset { IntOffset(leftPx, topPx) }
                            .size(width = 84.dp, height = 58.dp)
                            .pointerInput(node.id, widthPx, heightPx) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    nodes = nodes.map { curr ->
                                        if (curr.id == node.id) {
                                            val nx = (curr.xFraction + dragAmount.x / widthPx).coerceIn(0.08f, 0.92f)
                                            val ny = (curr.yFraction + dragAmount.y / heightPx).coerceIn(0.10f, 0.90f)
                                            curr.copy(xFraction = nx, yFraction = ny)
                                        } else curr
                                    }
                                }
                            }
                            .clickable {
                                selectedNodeId = node.id
                                if (node.type == GateType.INPUT) {
                                    nodes = nodes.map {
                                        if (it.id == node.id) it.copy(inputValue = !it.inputValue) else it
                                    }
                                }
                            }
                            .testTag("canvas_node_${node.id}"),
                        color = if (isHigh) signals.signalHigh.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            width = if (isWireSource || isSelected) 2.dp else 1.2.dp,
                            color = when {
                                isWireSource -> MaterialTheme.colorScheme.tertiary
                                isHigh -> signals.signalHigh
                                isSelected -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline
                            }
                        ),
                        shape = MaterialTheme.shapes.small,
                        shadowElevation = 3.dp
                    ) {
                        Box(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                            // Input Pin buttons on left edge when wiring
                            if (node.type.inputCount > 0 && wireSourceNodeId != null && wireSourceNodeId != node.id) {
                                Column(
                                    modifier = Modifier.align(Alignment.CenterStart),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    for (pinIdx in 0 until node.type.inputCount) {
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.tertiary)
                                                .clickable {
                                                    val src = wireSourceNodeId ?: return@clickable
                                                    wires = wires.filterNot {
                                                        it.toNodeId == node.id && it.toInputPin == pinIdx
                                                    } + CircuitWire(
                                                        id = "w_${src}_${node.id}_$pinIdx",
                                                        fromNodeId = src,
                                                        toNodeId = node.id,
                                                        toInputPin = pinIdx
                                                    )
                                                    wireSourceNodeId = null
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                "${pinIdx + 1}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onTertiary
                                            )
                                        }
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = node.label,
                                    style = MonoCodeStyle.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                Text(
                                    text = if (isHigh) "1 (HIGH)" else "0 (LOW)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isHigh) signals.signalHigh else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Output pin tap to start wiring
                            if (node.type.outputCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isWireSource) MaterialTheme.colorScheme.tertiary
                                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                        )
                                        .clickable {
                                            wireSourceNodeId = if (wireSourceNodeId == node.id) null else node.id
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("→", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }

        StepExplanationCard(
            title = "Signal Propagation Trace",
            steps = eval.steps
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CombinationalIcSection() {
    var selectedIc by rememberSaveable { mutableStateOf(CombinationalIcType.FULL_ADDER) }
    var a by rememberSaveable { mutableIntStateOf(1) }
    var b by rememberSaveable { mutableIntStateOf(1) }
    var cin by rememberSaveable { mutableIntStateOf(0) }
    var s1 by rememberSaveable { mutableIntStateOf(1) }
    var s0 by rememberSaveable { mutableIntStateOf(0) }
    var en by rememberSaveable { mutableIntStateOf(1) }
    var d0 by rememberSaveable { mutableIntStateOf(0) }
    var d1 by rememberSaveable { mutableIntStateOf(1) }
    var d2 by rememberSaveable { mutableIntStateOf(1) }
    var d3 by rememberSaveable { mutableIntStateOf(0) }

    val result = remember(selectedIc, a, b, cin, s1, s0, en, d0, d1, d2, d3) {
        when (selectedIc) {
            CombinationalIcType.HALF_ADDER -> CombinationalIcEngine.evaluateHalfAdder(a, b)
            CombinationalIcType.FULL_ADDER -> CombinationalIcEngine.evaluateFullAdder(a, b, cin)
            CombinationalIcType.MUX_4_1 -> CombinationalIcEngine.evaluateMux4To1(s1, s0, listOf(d0, d1, d2, d3))
            CombinationalIcType.DECODER_2_4 -> CombinationalIcEngine.evaluateDecoder2To4(en, s1, s0)
            CombinationalIcType.ENCODER_4_2 -> CombinationalIcEngine.evaluatePriorityEncoder4To2(listOf(d0, d1, d2, d3))
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LabSectionCard(
            title = "Ready-Made Combinational ICs",
            subtitle = selectedIc.subtitle
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CombinationalIcType.entries.forEach { ic ->
                    FilterChip(
                        selected = selectedIc == ic,
                        onClick = { selectedIc = ic },
                        label = { Text(ic.title) },
                        modifier = Modifier.testTag("ic_chip_${ic.name}")
                    )
                }
            }

            // Inputs
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (selectedIc) {
                    CombinationalIcType.HALF_ADDER -> {
                        BitToggleButton("A", a, { a = 1 - a }, "ic_in_a")
                        BitToggleButton("B", b, { b = 1 - b }, "ic_in_b")
                    }
                    CombinationalIcType.FULL_ADDER -> {
                        BitToggleButton("A", a, { a = 1 - a }, "ic_in_a")
                        BitToggleButton("B", b, { b = 1 - b }, "ic_in_b")
                        BitToggleButton("Cin", cin, { cin = 1 - cin }, "ic_in_cin")
                    }
                    CombinationalIcType.MUX_4_1 -> {
                        BitToggleButton("S1", s1, { s1 = 1 - s1 }, "ic_in_s1")
                        BitToggleButton("S0", s0, { s0 = 1 - s0 }, "ic_in_s0")
                        BitToggleButton("D0", d0, { d0 = 1 - d0 }, "ic_in_d0")
                        BitToggleButton("D1", d1, { d1 = 1 - d1 }, "ic_in_d1")
                        BitToggleButton("D2", d2, { d2 = 1 - d2 }, "ic_in_d2")
                        BitToggleButton("D3", d3, { d3 = 1 - d3 }, "ic_in_d3")
                    }
                    CombinationalIcType.DECODER_2_4 -> {
                        BitToggleButton("EN", en, { en = 1 - en }, "ic_in_en")
                        BitToggleButton("A1", s1, { s1 = 1 - s1 }, "ic_in_a1")
                        BitToggleButton("A0", s0, { s0 = 1 - s0 }, "ic_in_a0")
                    }
                    CombinationalIcType.ENCODER_4_2 -> {
                        BitToggleButton("D3 (Hi)", d3, { d3 = 1 - d3 }, "ic_in_d3")
                        BitToggleButton("D2", d2, { d2 = 1 - d2 }, "ic_in_d2")
                        BitToggleButton("D1", d1, { d1 = 1 - d1 }, "ic_in_d1")
                        BitToggleButton("D0 (Lo)", d0, { d0 = 1 - d0 }, "ic_in_d0")
                    }
                }
            }

            // Outputs Chip Strip
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "${selectedIc.title.uppercase()} OUTPUTS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        result.outputs.forEach { (lbl, valBit) ->
                            SignalReadoutChip(
                                label = lbl,
                                value = valBit.toString(),
                                isHigh = valBit == 1 || (lbl == "Selected Line")
                            )
                        }
                    }
                }
            }
        }

        StepExplanationCard(
            title = "${selectedIc.title} Logic Walkthrough",
            steps = result.steps
        )
    }
}
