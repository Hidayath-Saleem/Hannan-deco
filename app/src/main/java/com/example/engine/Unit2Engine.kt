package com.example.engine

enum class GateType(val label: String, val inputCount: Int, val outputCount: Int) {
    INPUT("IN", 0, 1),
    OUTPUT("LED", 1, 0),
    NOT("NOT", 1, 1),
    AND("AND", 2, 1),
    OR("OR", 2, 1),
    NAND("NAND", 2, 1),
    NOR("NOR", 2, 1),
    XOR("XOR", 2, 1),
    XNOR("XNOR", 2, 1)
}

data class CircuitNode(
    val id: String,
    val type: GateType,
    val label: String,
    val xFraction: Float, // 0f..1f normalized canvas coordinate for crisp responsiveness
    val yFraction: Float,
    val inputValue: Boolean = false
)

data class CircuitWire(
    val id: String,
    val fromNodeId: String,
    val toNodeId: String,
    val toInputPin: Int // 0 or 1
)

data class CircuitEvalResult(
    val nodeOutputs: Map<String, Boolean>,
    val steps: List<String>
)

object LogicCircuitEvaluator {

    fun presetHalfAdder(): Pair<List<CircuitNode>, List<CircuitWire>> {
        val nodes = listOf(
            CircuitNode("inA", GateType.INPUT, "A", 0.12f, 0.28f, true),
            CircuitNode("inB", GateType.INPUT, "B", 0.12f, 0.72f, true),
            CircuitNode("xor1", GateType.XOR, "XOR", 0.48f, 0.28f),
            CircuitNode("and1", GateType.AND, "AND", 0.48f, 0.72f),
            CircuitNode("outS", GateType.OUTPUT, "SUM", 0.85f, 0.28f),
            CircuitNode("outC", GateType.OUTPUT, "CARRY", 0.85f, 0.72f)
        )
        val wires = listOf(
            CircuitWire("w1", "inA", "xor1", 0),
            CircuitWire("w2", "inB", "xor1", 1),
            CircuitWire("w3", "inA", "and1", 0),
            CircuitWire("w4", "inB", "and1", 1),
            CircuitWire("w5", "xor1", "outS", 0),
            CircuitWire("w6", "and1", "outC", 0)
        )
        return nodes to wires
    }

    fun preset2To1Mux(): Pair<List<CircuitNode>, List<CircuitWire>> {
        val nodes = listOf(
            CircuitNode("d0", GateType.INPUT, "D0", 0.10f, 0.20f, false),
            CircuitNode("sel", GateType.INPUT, "SEL", 0.10f, 0.50f, true),
            CircuitNode("d1", GateType.INPUT, "D1", 0.10f, 0.80f, true),
            CircuitNode("notS", GateType.NOT, "NOT", 0.32f, 0.35f),
            CircuitNode("and0", GateType.AND, "AND0", 0.54f, 0.25f),
            CircuitNode("and1", GateType.AND, "AND1", 0.54f, 0.75f),
            CircuitNode("orY", GateType.OR, "OR", 0.73f, 0.50f),
            CircuitNode("outY", GateType.OUTPUT, "Y", 0.90f, 0.50f)
        )
        val wires = listOf(
            CircuitWire("m1", "sel", "notS", 0),
            CircuitWire("m2", "d0", "and0", 0),
            CircuitWire("m3", "notS", "and0", 1),
            CircuitWire("m4", "sel", "and1", 0),
            CircuitWire("m5", "d1", "and1", 1),
            CircuitWire("m6", "and0", "orY", 0),
            CircuitWire("m7", "and1", "orY", 1),
            CircuitWire("m8", "orY", "outY", 0)
        )
        return nodes to wires
    }

    fun presetNandXor(): Pair<List<CircuitNode>, List<CircuitWire>> {
        val nodes = listOf(
            CircuitNode("a", GateType.INPUT, "A", 0.10f, 0.30f, true),
            CircuitNode("b", GateType.INPUT, "B", 0.10f, 0.70f, false),
            CircuitNode("n1", GateType.NAND, "N1", 0.33f, 0.50f),
            CircuitNode("n2", GateType.NAND, "N2", 0.55f, 0.25f),
            CircuitNode("n3", GateType.NAND, "N3", 0.55f, 0.75f),
            CircuitNode("n4", GateType.NAND, "N4", 0.75f, 0.50f),
            CircuitNode("out", GateType.OUTPUT, "A⊕B", 0.91f, 0.50f)
        )
        val wires = listOf(
            CircuitWire("x1", "a", "n1", 0),
            CircuitWire("x2", "b", "n1", 1),
            CircuitWire("x3", "a", "n2", 0),
            CircuitWire("x4", "n1", "n2", 1),
            CircuitWire("x5", "n1", "n3", 0),
            CircuitWire("x6", "b", "n3", 1),
            CircuitWire("x7", "n2", "n4", 0),
            CircuitWire("x8", "n3", "n4", 1),
            CircuitWire("x9", "n4", "out", 0)
        )
        return nodes to wires
    }

    fun evaluate(nodes: List<CircuitNode>, wires: List<CircuitWire>): CircuitEvalResult {
        val values = mutableMapOf<String, Boolean>()
        nodes.forEach { node ->
            values[node.id] = if (node.type == GateType.INPUT) node.inputValue else false
        }

        // Fixed-point relaxation up to nodes.size + 1 passes (handles arbitrary DAGs safely without infinite loops)
        val maxPasses = (nodes.size + 2).coerceAtLeast(3)
        repeat(maxPasses) {
            var changed = false
            for (node in nodes) {
                if (node.type == GateType.INPUT) continue
                val pin0 = wires.firstOrNull { it.toNodeId == node.id && it.toInputPin == 0 }
                    ?.let { values[it.fromNodeId] ?: false } ?: false
                val pin1 = wires.firstOrNull { it.toNodeId == node.id && it.toInputPin == 1 }
                    ?.let { values[it.fromNodeId] ?: false } ?: false

                val nextVal = when (node.type) {
                    GateType.INPUT -> node.inputValue
                    GateType.OUTPUT -> pin0
                    GateType.NOT -> !pin0
                    GateType.AND -> pin0 && pin1
                    GateType.OR -> pin0 || pin1
                    GateType.NAND -> !(pin0 && pin1)
                    GateType.NOR -> !(pin0 || pin1)
                    GateType.XOR -> pin0 xor pin1
                    GateType.XNOR -> !(pin0 xor pin1)
                }
                if (values[node.id] != nextVal) {
                    values[node.id] = nextVal
                    changed = true
                }
            }
            if (!changed) return@repeat
        }

        val steps = mutableListOf<String>()
        val inputs = nodes.filter { it.type == GateType.INPUT }
        if (inputs.isNotEmpty()) {
            steps += "Inputs: " + inputs.joinToString(", ") { "${it.label}=${if (values[it.id] == true) 1 else 0}" }
        }
        nodes.filter { it.type != GateType.INPUT && it.type != GateType.OUTPUT }.forEach { gate ->
            val p0 = wires.firstOrNull { it.toNodeId == gate.id && it.toInputPin == 0 }
                ?.let { if (values[it.fromNodeId] == true) 1 else 0 } ?: 0
            val p1 = wires.firstOrNull { it.toNodeId == gate.id && it.toInputPin == 1 }
                ?.let { if (values[it.fromNodeId] == true) 1 else 0 } ?: 0
            val out = if (values[gate.id] == true) 1 else 0
            if (gate.type == GateType.NOT) {
                steps += "${gate.label} (NOT): !($p0) -> $out"
            } else {
                steps += "${gate.label} (${gate.type.label}): ($p0, $p1) -> $out"
            }
        }
        val outputs = nodes.filter { it.type == GateType.OUTPUT }
        if (outputs.isNotEmpty()) {
            steps += "Outputs: " + outputs.joinToString(", ") { "${it.label}=${if (values[it.id] == true) 1 else 0}" }
        }

        return CircuitEvalResult(values, steps)
    }
}

enum class CombinationalIcType(val title: String, val subtitle: String) {
    HALF_ADDER("Half Adder", "Adds 2 bits: Sum = A ⊕ B, Carry = A · B"),
    FULL_ADDER("Full Adder", "Adds 3 bits (A, B, Cin) -> Sum & Cout"),
    MUX_4_1("4:1 Multiplexer", "Select lines S1S0 route one of D0..D3 to Y"),
    DECODER_2_4("2:4 Decoder", "Binary A1A0 activates one of Y0..Y3 when EN=1"),
    ENCODER_4_2("4:2 Priority Encoder", "Outputs index of highest active D3..D0")
}

data class CombinationalIcResult(
    val outputs: List<Pair<String, Int>>,
    val activeRowIndex: Int,
    val steps: List<String>
)

object CombinationalIcEngine {

    fun evaluateHalfAdder(a: Int, b: Int): CombinationalIcResult {
        val sum = a xor b
        val carry = a and b
        return CombinationalIcResult(
            outputs = listOf("Sum (S)" to sum, "Carry (C)" to carry),
            activeRowIndex = (a shl 1) or b,
            steps = listOf(
                "1. Inputs A = $a, B = $b.",
                "2. XOR gate computes Sum = $a ⊕ $b = $sum.",
                "3. AND gate computes Carry = $a · $b = $carry."
            )
        )
    }

    fun evaluateFullAdder(a: Int, b: Int, cin: Int): CombinationalIcResult {
        val axb = a xor b
        val sum = axb xor cin
        val cout = (a and b) or (cin and axb)
        return CombinationalIcResult(
            outputs = listOf("Sum (S)" to sum, "Carry Out (Cout)" to cout),
            activeRowIndex = (a shl 2) or (b shl 1) or cin,
            steps = listOf(
                "1. First Half-Adder stage: A ⊕ B = $a ⊕ $b = $axb, A·B = ${a and b}.",
                "2. Second stage adds Cin = $cin: Sum = $axb ⊕ $cin = $sum.",
                "3. Carry Out: Cout = (A·B) + Cin·(A⊕B) = ${a and b} + ($cin·$axb) = $cout."
            )
        )
    }

    fun evaluateMux4To1(s1: Int, s0: Int, d: List<Int>): CombinationalIcResult {
        val sel = (s1 shl 1) or s0
        val y = d.getOrElse(sel) { 0 }
        return CombinationalIcResult(
            outputs = listOf("Selected Line" to sel, "Output Y" to y),
            activeRowIndex = sel,
            steps = listOf(
                "1. Select lines S1S0 = $s1$s0₂ = channel D$sel.",
                "2. Data channels D0..D3 = [${d.joinToString(", ")}].",
                "3. Internal AND-OR tree masks out other channels and routes D$sel ($y) to output Y = $y."
            )
        )
    }

    fun evaluateDecoder2To4(en: Int, a1: Int, a0: Int): CombinationalIcResult {
        val code = (a1 shl 1) or a0
        val ys = (0..3).map { idx -> if (en == 1 && idx == code) 1 else 0 }
        return CombinationalIcResult(
            outputs = ys.mapIndexed { i, v -> "Y$i" to v },
            activeRowIndex = if (en == 1) code else -1,
            steps = if (en == 0) {
                listOf(
                    "1. Enable EN = 0 -> Decoder disabled.",
                    "2. All outputs Y0..Y3 are forced to 0 regardless of A1A0."
                )
            } else {
                listOf(
                    "1. Enable EN = 1 -> Decoder active.",
                    "2. Address A1A0 = $a1$a0₂ = decimal $code.",
                    "3. Only minterm output Y$code is asserted HIGH (1); all others remain 0."
                )
            }
        )
    }

    fun evaluatePriorityEncoder4To2(d: List<Int>): CombinationalIcResult {
        val highest = (3 downTo 0).firstOrNull { d.getOrElse(it) { 0 } == 1 }
        val valid = if (highest != null) 1 else 0
        val y1 = if (highest != null) (highest shr 1) and 1 else 0
        val y0 = if (highest != null) highest and 1 else 0
        return CombinationalIcResult(
            outputs = listOf("Y1" to y1, "Y0" to y0, "Valid (V)" to valid),
            activeRowIndex = highest ?: -1,
            steps = if (highest == null) {
                listOf(
                    "1. All inputs D3..D0 are 0.",
                    "2. Valid flag V = 0, indicating no active request; Y1Y0 = 00."
                )
            } else {
                listOf(
                    "1. Inputs D3..D0 = [${d[3]}, ${d[2]}, ${d[1]}, ${d[0]}].",
                    "2. Highest-priority active input is D$highest.",
                    "3. Encoder outputs binary index Y1Y0 = $y1$y0₂ ($highest) with Valid V = 1."
                )
            }
        )
    }
}
