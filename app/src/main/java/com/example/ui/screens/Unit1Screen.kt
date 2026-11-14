package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.DecoCurriculum
import com.example.engine.BooleanExpressionEvaluator
import com.example.engine.KMapCellState
import com.example.engine.KMapSolver
import com.example.engine.NumberBase
import com.example.engine.NumberSystemConverter
import com.example.ui.components.LabSectionCard
import com.example.ui.components.PresetChipRow
import com.example.ui.components.StepExplanationCard
import com.example.ui.components.TheorySummaryCard
import com.example.ui.theme.KMapGroupColors
import com.example.ui.theme.LocalDecoSignalColors
import com.example.ui.theme.MonoCodeStyle
import com.example.ui.theme.MonoValueStyle

@Composable
fun Unit1Screen(modifier: Modifier = Modifier) {
    val unit = DecoCurriculum.units[0]
    var subTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("Converter", "Truth Table", "K-Map (2–4)")

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
                            modifier = Modifier.testTag("unit1_tab_$idx")
                        )
                    }
                }
            }

            when (subTab) {
                0 -> item { NumberConverterSection() }
                1 -> item { TruthTableSection() }
                else -> item { KMapSimplifierSection() }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NumberConverterSection() {
    var selectedBase by rememberSaveable { mutableStateOf(NumberBase.DECIMAL) }
    var inputText by rememberSaveable { mutableStateOf("-25") }
    val result = remember(inputText, selectedBase) {
        NumberSystemConverter.convert(inputText, selectedBase)
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LabSectionCard(
            title = "Radix & 2's Complement Converter",
            subtitle = "Convert seamlessly across Decimal, Binary, Octal, Hex, and 8-bit Signed 2's Complement."
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                NumberBase.entries.forEach { base ->
                    FilterChip(
                        selected = selectedBase == base,
                        onClick = {
                            selectedBase = base
                            inputText = when (base) {
                                NumberBase.DECIMAL -> "-25"
                                NumberBase.BINARY -> "101010"
                                NumberBase.OCTAL -> "52"
                                NumberBase.HEX -> "2A"
                                NumberBase.TWOS_COMPLEMENT -> "11100111"
                            }
                        },
                        label = { Text(base.label) },
                        modifier = Modifier.testTag("base_chip_${base.name}")
                    )
                }
            }

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Input (${selectedBase.label})") },
                placeholder = { Text(selectedBase.hint) },
                textStyle = MonoValueStyle,
                singleLine = true,
                isError = !result.isValid,
                supportingText = if (!result.isValid && result.errorMessage != null) {
                    { Text(result.errorMessage) }
                } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("converter_input_field")
            )

            PresetChipRow(
                presets = listOf("-25 (Dec)", "42 (Dec)", "0xFF", "11110000 (2's)"),
                onSelect = { idx ->
                    when (idx) {
                        0 -> { selectedBase = NumberBase.DECIMAL; inputText = "-25" }
                        1 -> { selectedBase = NumberBase.DECIMAL; inputText = "42" }
                        2 -> { selectedBase = NumberBase.HEX; inputText = "FF" }
                        3 -> { selectedBase = NumberBase.TWOS_COMPLEMENT; inputText = "11110000" }
                    }
                }
            )

            if (result.isValid) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    RadixOutputRow("Decimal (Base 10)", result.signedDecimal.toString())
                    RadixOutputRow("Binary (Base 2)", result.binary)
                    RadixOutputRow("Octal (Base 8)", result.octal)
                    RadixOutputRow("Hexadecimal (Base 16)", "0x${result.hex}")
                    RadixOutputRow(
                        label = "8-Bit 2's Complement",
                        value = result.twosComplement8Bit,
                        supporting = result.twosComplementNote
                    )
                }
            }
        }

        StepExplanationCard(
            title = "Conversion Step-by-Step",
            steps = result.steps
        )
    }
}

@Composable
private fun RadixOutputRow(
    label: String,
    value: String,
    supporting: String? = null
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MonoValueStyle.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (supporting != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TruthTableSection() {
    var expr by rememberSaveable { mutableStateOf("A & B + !C") }
    var forcedVars by rememberSaveable { mutableIntStateOf(0) } // 0 = auto
    val result = remember(expr, forcedVars) {
        BooleanExpressionEvaluator.generateTruthTable(
            expr,
            if (forcedVars == 0) null else forcedVars
        )
    }
    val signals = LocalDecoSignalColors.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LabSectionCard(
            title = "Boolean Truth Table Generator",
            subtitle = "Use A, B, C, D with &, *, AND, +, |, OR, !, ', ^, XOR, NAND, NOR."
        ) {
            OutlinedTextField(
                value = expr,
                onValueChange = { expr = it },
                label = { Text("Boolean Expression F(A, B, C, D)") },
                textStyle = MonoValueStyle,
                singleLine = true,
                isError = !result.isValid,
                supportingText = if (!result.isValid && result.errorMessage != null) {
                    { Text(result.errorMessage) }
                } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("truth_table_expr_input")
            )

            PresetChipRow(
                presets = listOf("A·B + C'", "(A⊕B)·C", "Majority 3", "4-Var Parity"),
                onSelect = { idx ->
                    expr = when (idx) {
                        0 -> "A & B + !C"
                        1 -> "(A ^ B) & C"
                        2 -> "A&B + B&C + A&C"
                        else -> "A ^ B ^ C ^ D"
                    }
                }
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Vars:", style = MaterialTheme.typography.labelMedium)
                listOf(0 to "Auto", 2 to "2", 3 to "3", 4 to "4").forEach { (v, label) ->
                    FilterChip(
                        selected = forcedVars == v,
                        onClick = { forcedVars = v },
                        label = { Text(label) }
                    )
                }
            }

            if (result.isValid) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Column(Modifier.padding(10.dp)) {
                            Text("Canonical SOP", style = MaterialTheme.typography.labelSmall)
                            Text(
                                result.canonicalSop,
                                style = MonoCodeStyle,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Column(Modifier.padding(10.dp)) {
                            Text("Canonical POS", style = MaterialTheme.typography.labelSmall)
                            Text(
                                result.canonicalPos,
                                style = MonoCodeStyle,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                // Truth Table Matrix
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "m",
                                style = MonoValueStyle,
                                modifier = Modifier.width(32.dp)
                            )
                            result.variables.forEach { v ->
                                Text(
                                    v,
                                    style = MonoValueStyle,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Text(
                                "F",
                                style = MonoValueStyle.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(40.dp)
                            )
                        }
                        result.rows.forEach { row ->
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            val isHigh = row.output == 1
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isHigh) signals.signalHigh.copy(alpha = 0.08f)
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "m${row.mintermIndex}",
                                    style = MonoCodeStyle,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.width(32.dp)
                                )
                                row.inputs.forEach { bit ->
                                    Text(
                                        bit.toString(),
                                        style = MonoValueStyle,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Text(
                                    row.output.toString(),
                                    style = MonoValueStyle.copy(fontWeight = FontWeight.Bold),
                                    color = if (isHigh) signals.signalHigh else signals.signalLow,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.width(40.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        StepExplanationCard(
            title = "Truth Table Evaluation Trace",
            steps = result.steps
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KMapSimplifierSection() {
    var varCount by rememberSaveable { mutableIntStateOf(4) }
    var cellStates by remember {
        mutableStateOf(
            List(16) { idx ->
                // Preset showing wrap-around quad + pair: m(0, 2, 8, 10, 5, 7)
                if (idx in listOf(0, 2, 8, 10, 5, 7)) KMapCellState.ONE else KMapCellState.ZERO
            }
        )
    }

    val activeCells = remember(varCount, cellStates) {
        cellStates.take(1 shl varCount)
    }
    val solution = remember(varCount, activeCells) {
        KMapSolver.solve(varCount, activeCells)
    }

    val rows = if (varCount == 2) 2 else if (varCount == 3) 2 else 4
    val cols = if (varCount == 2) 2 else 4
    val rowLabels = when (varCount) {
        2 -> listOf("0", "1")
        3 -> listOf("0", "1")
        else -> listOf("00", "01", "11", "10")
    }
    val colLabels = when (varCount) {
        2 -> listOf("0", "1")
        else -> listOf("00", "01", "11", "10")
    }
    val axisLabel = when (varCount) {
        2 -> "A \\ B"
        3 -> "A \\ BC"
        else -> "AB \\ CD"
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LabSectionCard(
            title = "Karnaugh Map Simplifier (Up to 4 Variables)",
            subtitle = "Tap any K-Map cell to cycle 0 -> 1 -> X (Don't Care). Prime implicants are grouped automatically."
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(2, 3, 4).forEach { count ->
                    FilterChip(
                        selected = varCount == count,
                        onClick = { varCount = count },
                        label = { Text("$count-Var") },
                        modifier = Modifier.testTag("kmap_var_$count")
                    )
                }
                Spacer(Modifier.weight(1f))
                OutlinedButton(
                    onClick = { cellStates = List(16) { KMapCellState.ZERO } },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Clear")
                }
            }

            PresetChipRow(
                presets = listOf("Wrap Quad", "Octet", "With Don't-Care"),
                onSelect = { idx ->
                    varCount = 4
                    cellStates = when (idx) {
                        0 -> List(16) { if (it in listOf(0, 2, 8, 10, 5, 7)) KMapCellState.ONE else KMapCellState.ZERO }
                        1 -> List(16) { if (it in listOf(0, 1, 2, 3, 8, 9, 10, 11)) KMapCellState.ONE else KMapCellState.ZERO }
                        else -> List(16) {
                            when (it) {
                                1, 3, 5, 7, 11 -> KMapCellState.ONE
                                9, 15 -> KMapCellState.DONT_CARE
                                else -> KMapCellState.ZERO
                            }
                        }
                    }
                }
            )

            // Minimized SOP Display
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Text(
                        text = "MINIMIZED SOP OUTPUT",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = solution.minimizedSop,
                        style = MonoValueStyle.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("kmap_minimized_output")
                    )
                }
            }

            // K-Map Gray-Code Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = axisLabel,
                        style = MonoCodeStyle.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(64.dp)
                    )
                    colLabels.forEach { colLbl ->
                        Text(
                            text = colLbl,
                            style = MonoValueStyle,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(66.dp)
                        )
                    }
                }

                for (r in 0 until rows) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = rowLabels[r],
                            style = MonoValueStyle,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(64.dp)
                        )
                        for (c in 0 until cols) {
                            val minterm = KMapSolver.mintermAtGrid(varCount, r, c)
                            val state = cellStates[minterm]
                            val coveringGroups = solution.groups.filter { minterm in it.coveredMinterms }
                            val primaryGroup = coveringGroups.firstOrNull()
                            val groupColor = primaryGroup?.let {
                                KMapGroupColors[it.colorIndex % KMapGroupColors.size]
                            }

                            Box(
                                modifier = Modifier
                                    .size(width = 66.dp, height = 56.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(
                                        when {
                                            groupColor != null -> groupColor.copy(alpha = 0.16f)
                                            state == KMapCellState.DONT_CARE -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        }
                                    )
                                    .border(
                                        width = if (groupColor != null) 2.dp else 1.dp,
                                        color = groupColor ?: MaterialTheme.colorScheme.outlineVariant,
                                        shape = MaterialTheme.shapes.small
                                    )
                                    .clickable {
                                        val mutable = cellStates.toMutableList()
                                        mutable[minterm] = mutable[minterm].next()
                                        cellStates = mutable
                                    }
                                    .testTag("kmap_cell_$minterm"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "m$minterm",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(4.dp)
                                )
                                Text(
                                    text = state.symbol,
                                    style = MonoValueStyle.copy(fontWeight = FontWeight.Bold),
                                    color = when (state) {
                                        KMapCellState.ONE -> groupColor ?: MaterialTheme.colorScheme.primary
                                        KMapCellState.DONT_CARE -> MaterialTheme.colorScheme.tertiary
                                        KMapCellState.ZERO -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                                if (coveringGroups.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        coveringGroups.take(3).forEach { grp ->
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(KMapGroupColors[grp.colorIndex % KMapGroupColors.size])
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (solution.groups.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    solution.groups.forEach { grp ->
                        val color = KMapGroupColors[grp.colorIndex % KMapGroupColors.size]
                        Surface(
                            color = color.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, color),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "${grp.term} (${grp.size})",
                                style = MonoCodeStyle,
                                color = color,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        StepExplanationCard(
            title = "K-Map Grouping & Simplification Steps",
            steps = solution.steps
        )
    }
}
