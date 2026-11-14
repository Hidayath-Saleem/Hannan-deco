package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Refresh
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.DecoCurriculum
import com.example.engine.CounterShiftEngine
import com.example.engine.FlipFlopCycleSample
import com.example.engine.FlipFlopEngine
import com.example.engine.FlipFlopType
import com.example.engine.ShiftRegisterMode
import com.example.ui.components.BitToggleButton
import com.example.ui.components.LabSectionCard
import com.example.ui.components.SignalReadoutChip
import com.example.ui.components.StepExplanationCard
import com.example.ui.components.TheorySummaryCard
import com.example.ui.theme.LocalDecoSignalColors
import com.example.ui.theme.MonoCodeStyle
import com.example.ui.theme.MonoValueStyle

@Composable
fun Unit3Screen(modifier: Modifier = Modifier) {
    val unit = DecoCurriculum.units[2]
    var subTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("Flip-Flops & Timing", "Counters & Shift Reg")

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
                            modifier = Modifier.testTag("unit3_tab_$idx")
                        )
                    }
                }
            }
            when (subTab) {
                0 -> item { FlipFlopTimingSection() }
                else -> item { CounterShiftSection() }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlipFlopTimingSection() {
    var ffType by rememberSaveable { mutableStateOf(FlipFlopType.JK) }
    var in1 by rememberSaveable { mutableIntStateOf(1) }
    var in2 by rememberSaveable { mutableIntStateOf(1) }
    var q by rememberSaveable { mutableIntStateOf(0) }
    var qBar by rememberSaveable { mutableIntStateOf(1) }
    var cycle by rememberSaveable { mutableIntStateOf(0) }
    var isInvalid by rememberSaveable { mutableStateOf(false) }
    var actionLabel by rememberSaveable { mutableStateOf("READY") }
    var traceHistory by remember {
        mutableStateOf(
            listOf(
                "Initial state: Q = 0, Q' = 1. Set inputs and tap 'CLK ↑' to trigger rising-edge evaluation."
            )
        )
    }
    var waveformSamples by remember {
        mutableStateOf(
            listOf(FlipFlopCycleSample(0, 1, 1, 0, false, "INIT"))
        )
    }

    val signals = LocalDecoSignalColors.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LabSectionCard(
            title = "Flip-Flop Simulator & Timing Diagram",
            subtitle = ffType.equation
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FlipFlopType.entries.forEach { type ->
                    FilterChip(
                        selected = ffType == type,
                        onClick = {
                            ffType = type
                            isInvalid = false
                            actionLabel = "SWITCHED"
                        },
                        label = { Text(type.title) },
                        modifier = Modifier.testTag("ff_chip_${type.name}")
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BitToggleButton(
                    label = ffType.input1Label,
                    value = in1,
                    onToggle = { in1 = 1 - in1 },
                    testTag = "ff_in1"
                )
                if (ffType.input2Label != null) {
                    BitToggleButton(
                        label = ffType.input2Label!!,
                        value = in2,
                        onToggle = { in2 = 1 - in2 },
                        testTag = "ff_in2"
                    )
                }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = {
                        cycle += 1
                        val res = FlipFlopEngine.step(ffType, q, in1, in2, cycle)
                        q = res.qNext
                        qBar = res.qBarNext
                        isInvalid = res.isInvalid
                        actionLabel = res.actionLabel
                        waveformSamples = (waveformSamples + FlipFlopCycleSample(
                            cycle = cycle,
                            in1 = in1,
                            in2 = in2,
                            qNext = q,
                            isInvalid = isInvalid,
                            actionLabel = actionLabel
                        )).takeLast(8)
                        traceHistory = (listOf(res.stepExplanation) + traceHistory).take(4)
                    },
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("ff_clock_pulse_btn")
                ) {
                    Icon(Icons.Outlined.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("CLK ↑")
                }
                OutlinedButton(
                    onClick = {
                        q = 0; qBar = 1; cycle = 0; isInvalid = false; actionLabel = "RESET"
                        waveformSamples = listOf(FlipFlopCycleSample(0, in1, in2, 0, false, "INIT"))
                        traceHistory = listOf("Flip-Flop asynchronously cleared: Q = 0, Q' = 1.")
                    },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Icon(Icons.Outlined.Refresh, contentDescription = "Reset flip-flop")
                }
            }

            // Live State Badges
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SignalReadoutChip(label = "Q", value = q.toString(), isHigh = q == 1)
                SignalReadoutChip(label = "Q'", value = qBar.toString(), isHigh = qBar == 1)
                SignalReadoutChip(
                    label = "Mode",
                    value = actionLabel,
                    isHigh = !isInvalid
                )
            }

            // Oscilloscope Digital Timing Diagram
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "DIGITAL TIMING DIAGRAM (LAST ${waveformSamples.size} CYCLES)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))

                    WaveformRow(
                        label = "CLK",
                        values = waveformSamples.map { 1 },
                        isClock = true,
                        color = signals.clockPulse
                    )
                    Spacer(Modifier.height(6.dp))
                    WaveformRow(
                        label = ffType.input1Label,
                        values = waveformSamples.map { it.in1 },
                        isClock = false,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    if (ffType.input2Label != null) {
                        Spacer(Modifier.height(6.dp))
                        WaveformRow(
                            label = ffType.input2Label!!,
                            values = waveformSamples.map { it.in2 },
                            isClock = false,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    WaveformRow(
                        label = "Q",
                        values = waveformSamples.map { it.qNext },
                        isClock = false,
                        color = if (isInvalid) signals.miss else signals.signalHigh
                    )
                }
            }
        }

        StepExplanationCard(
            title = "Clock Edge History",
            steps = traceHistory,
            accentColor = if (isInvalid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun WaveformRow(
    label: String,
    values: List<Int>,
    isClock: Boolean,
    color: Color
) {
    val signals = LocalDecoSignalColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MonoCodeStyle.copy(fontWeight = FontWeight.Bold),
            color = color,
            modifier = Modifier.width(36.dp)
        )
        Canvas(
            modifier = Modifier
                .weight(1f)
                .height(28.dp)
        ) {
            val count = values.size.coerceAtLeast(1)
            val slotW = size.width / count.toFloat()
            val yHigh = 4.dp.toPx()
            val yLow = size.height - 4.dp.toPx()

            // Vertical cycle guides
            for (i in 0..count) {
                val x = i * slotW
                drawLine(
                    color = signals.gridLine.copy(alpha = 0.35f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
            }

            val path = Path()
            if (isClock) {
                for (i in 0 until count) {
                    val x0 = i * slotW
                    val xMid = x0 + slotW * 0.5f
                    val x1 = (i + 1) * slotW
                    if (i == 0) path.moveTo(x0, yLow) else path.lineTo(x0, yLow)
                    path.lineTo(xMid, yLow)
                    path.lineTo(xMid, yHigh)
                    path.lineTo(x1, yHigh)
                }
            } else {
                for (i in values.indices) {
                    val x0 = i * slotW
                    val x1 = (i + 1) * slotW
                    val y = if (values[i] == 1) yHigh else yLow
                    if (i == 0) {
                        path.moveTo(x0, y)
                    } else {
                        path.lineTo(x0, y)
                    }
                    path.lineTo(x1, y)
                }
            }
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Square)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CounterShiftSection() {
    var counterVal by rememberSaveable { mutableIntStateOf(0) }
    var countUp by rememberSaveable { mutableStateOf(true) }
    var mod10 by rememberSaveable { mutableStateOf(false) }
    var counterStep by rememberSaveable {
        mutableStateOf("4-bit Synchronous Counter ready at 0000 (0). Tap 'Pulse Counter CLK' to advance.")
    }

    var shiftMode by rememberSaveable { mutableStateOf(ShiftRegisterMode.SISO) }
    var shiftBits by remember { mutableStateOf(listOf(1, 0, 0, 0)) }
    var serialIn by rememberSaveable { mutableIntStateOf(1) }
    var serialOut by rememberSaveable { mutableIntStateOf(0) }
    var shiftStep by rememberSaveable {
        mutableStateOf("4-bit Shift Register initialized to 1000. Select mode and tap 'Shift CLK'.")
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LabSectionCard(
            title = "4-Bit Binary / BCD Decade Counter",
            subtitle = "Observe flip-flop bit transitions across Q3 Q2 Q1 Q0."
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = countUp,
                    onClick = { countUp = true },
                    label = { Text("Up Counter") }
                )
                FilterChip(
                    selected = !countUp,
                    onClick = { countUp = false },
                    label = { Text("Down Counter") }
                )
                FilterChip(
                    selected = mod10,
                    onClick = { mod10 = !mod10; counterVal %= 10 },
                    label = { Text(if (mod10) "Mod-10 (BCD)" else "Mod-16") }
                )
            }

            BitLedStrip(
                labels = listOf("Q3", "Q2", "Q1", "Q0"),
                bits = (3 downTo 0).map { (counterVal shr it) and 1 }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SignalReadoutChip(
                    label = "Decimal",
                    value = counterVal.toString(),
                    isHigh = true
                )
                SignalReadoutChip(
                    label = "Hex",
                    value = "0x${counterVal.toString(16).uppercase()}",
                    isHigh = true
                )
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = {
                        val (next, msg) = CounterShiftEngine.stepCounter(
                            counterVal,
                            countUp,
                            if (mod10) 10 else 16
                        )
                        counterVal = next
                        counterStep = msg
                    },
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("counter_clk_btn")
                ) {
                    Text("Pulse CLK ↑")
                }
            }
        }

        LabSectionCard(
            title = "4-Bit Shift Register",
            subtitle = shiftMode.desc
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ShiftRegisterMode.entries.forEach { m ->
                    FilterChip(
                        selected = shiftMode == m,
                        onClick = { shiftMode = m },
                        label = { Text(m.title) }
                    )
                }
            }

            BitLedStrip(
                labels = listOf("Q3", "Q2", "Q1", "Q0"),
                bits = shiftBits
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (shiftMode == ShiftRegisterMode.SISO || shiftMode == ShiftRegisterMode.SIPO) {
                    BitToggleButton(
                        label = "Ser In",
                        value = serialIn,
                        onToggle = { serialIn = 1 - serialIn },
                        testTag = "shift_serial_in"
                    )
                }
                SignalReadoutChip(
                    label = "Ser Out",
                    value = serialOut.toString(),
                    isHigh = serialOut == 1
                )
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = {
                        val (next, outBit, msg) = CounterShiftEngine.stepShiftRegister(
                            shiftBits,
                            serialIn,
                            shiftMode
                        )
                        shiftBits = next
                        serialOut = outBit
                        shiftStep = msg
                    },
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("shift_clk_btn")
                ) {
                    Text("Shift CLK ↑")
                }
                OutlinedButton(
                    onClick = {
                        shiftBits = listOf(1, 0, 0, 0)
                        serialOut = 0
                        shiftStep = "Reset shift register to 1000."
                    },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("1000")
                }
            }
        }

        StepExplanationCard(
            title = "Counter & Shift Register Trace",
            steps = listOf(counterStep, shiftStep)
        )
    }
}

@Composable
private fun BitLedStrip(labels: List<String>, bits: List<Int>) {
    val signals = LocalDecoSignalColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        labels.zip(bits).forEach { (lbl, bit) ->
            val high = bit == 1
            Surface(
                modifier = Modifier.weight(1f),
                color = if (high) signals.signalHigh.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.5.dp, if (high) signals.signalHigh else MaterialTheme.colorScheme.outline),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (high) signals.signalHigh else signals.signalLow)
                    )
                    Text(lbl, style = MaterialTheme.typography.labelSmall)
                    Text(
                        bit.toString(),
                        style = MonoValueStyle.copy(fontWeight = FontWeight.Bold),
                        color = if (high) signals.signalHigh else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
