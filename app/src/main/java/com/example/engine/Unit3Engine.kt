package com.example.engine

enum class FlipFlopType(
    val title: String,
    val equation: String,
    val input1Label: String,
    val input2Label: String?
) {
    SR("SR Flip-Flop", "Q+ = S + R'·Q  (S=R=1 Invalid)", "S", "R"),
    JK("JK Flip-Flop", "Q+ = J·Q' + K'·Q  (J=K=1 Toggle)", "J", "K"),
    D("D Flip-Flop", "Q+ = D  (Delay / Data Latch)", "D", null),
    T("T Flip-Flop", "Q+ = T ⊕ Q  (Toggle when T=1)", "T", null)
}

data class FlipFlopCycleSample(
    val cycle: Int,
    val in1: Int,
    val in2: Int,
    val qNext: Int,
    val isInvalid: Boolean,
    val actionLabel: String
)

data class FlipFlopStepResult(
    val qNext: Int,
    val qBarNext: Int,
    val isInvalid: Boolean,
    val actionLabel: String,
    val stepExplanation: String
)

object FlipFlopEngine {

    fun step(type: FlipFlopType, currentQ: Int, in1: Int, in2: Int, cycle: Int): FlipFlopStepResult {
        return when (type) {
            FlipFlopType.SR -> when {
                in1 == 0 && in2 == 0 -> FlipFlopStepResult(
                    qNext = currentQ,
                    qBarNext = 1 - currentQ,
                    isInvalid = false,
                    actionLabel = "HOLD",
                    stepExplanation = "Cycle #$cycle (CLK ↑): S=0, R=0 -> Hold state. Output Q remains $currentQ."
                )
                in1 == 0 && in2 == 1 -> FlipFlopStepResult(
                    qNext = 0,
                    qBarNext = 1,
                    isInvalid = false,
                    actionLabel = "RESET",
                    stepExplanation = "Cycle #$cycle (CLK ↑): S=0, R=1 -> Reset state. Output Q becomes 0, Q' = 1."
                )
                in1 == 1 && in2 == 0 -> FlipFlopStepResult(
                    qNext = 1,
                    qBarNext = 0,
                    isInvalid = false,
                    actionLabel = "SET",
                    stepExplanation = "Cycle #$cycle (CLK ↑): S=1, R=0 -> Set state. Output Q becomes 1, Q' = 0."
                )
                else -> FlipFlopStepResult(
                    qNext = currentQ,
                    qBarNext = currentQ,
                    isInvalid = true,
                    actionLabel = "INVALID",
                    stepExplanation = "Cycle #$cycle (CLK ↑): S=1, R=1 -> Forbidden/Invalid! Both Q and Q' attempt to drive the same rail, causing a race condition."
                )
            }

            FlipFlopType.JK -> when {
                in1 == 0 && in2 == 0 -> FlipFlopStepResult(
                    qNext = currentQ,
                    qBarNext = 1 - currentQ,
                    isInvalid = false,
                    actionLabel = "HOLD",
                    stepExplanation = "Cycle #$cycle (CLK ↑): J=0, K=0 -> No change. Output Q stays $currentQ."
                )
                in1 == 0 && in2 == 1 -> FlipFlopStepResult(
                    qNext = 0,
                    qBarNext = 1,
                    isInvalid = false,
                    actionLabel = "RESET",
                    stepExplanation = "Cycle #$cycle (CLK ↑): J=0, K=1 -> Reset. Output Q clears to 0."
                )
                in1 == 1 && in2 == 0 -> FlipFlopStepResult(
                    qNext = 1,
                    qBarNext = 0,
                    isInvalid = false,
                    actionLabel = "SET",
                    stepExplanation = "Cycle #$cycle (CLK ↑): J=1, K=0 -> Set. Output Q latches to 1."
                )
                else -> {
                    val toggled = 1 - currentQ
                    FlipFlopStepResult(
                        qNext = toggled,
                        qBarNext = 1 - toggled,
                        isInvalid = false,
                        actionLabel = "TOGGLE",
                        stepExplanation = "Cycle #$cycle (CLK ↑): J=1, K=1 -> Toggle! Feedback inverts Q from $currentQ to $toggled."
                    )
                }
            }

            FlipFlopType.D -> FlipFlopStepResult(
                qNext = in1,
                qBarNext = 1 - in1,
                isInvalid = false,
                actionLabel = if (in1 == 1) "LOAD 1" else "LOAD 0",
                stepExplanation = "Cycle #$cycle (CLK ↑): D=$in1 -> Data sampled on rising edge. Q updates directly to $in1."
            )

            FlipFlopType.T -> {
                val next = if (in1 == 1) 1 - currentQ else currentQ
                FlipFlopStepResult(
                    qNext = next,
                    qBarNext = 1 - next,
                    isInvalid = false,
                    actionLabel = if (in1 == 1) "TOGGLE" else "HOLD",
                    stepExplanation = if (in1 == 1) {
                        "Cycle #$cycle (CLK ↑): T=1 -> Toggle! Q flips from $currentQ to $next."
                    } else {
                        "Cycle #$cycle (CLK ↑): T=0 -> Hold! Q remains $currentQ."
                    }
                )
            }
        }
    }
}

enum class ShiftRegisterMode(val title: String, val desc: String) {
    SISO("SISO (Shift Right)", "Serial In at MSB (Q3) -> shifts right -> Serial Out at Q0"),
    SIPO("SIPO (Serial-to-Parallel)", "Serial bits enter Q3 and appear simultaneously on Q3..Q0"),
    RING("4-Bit Ring Counter", "Q0 feeds back into Q3, circulating a single pattern"),
    JOHNSON("Johnson (Twisted Ring)", "Q0' feeds back into Q3, creating 8 unique states")
}

object CounterShiftEngine {

    fun stepCounter(currentValue: Int, countUp: Boolean, modValue: Int = 16): Pair<Int, String> {
        val next = if (countUp) {
            (currentValue + 1) % modValue
        } else {
            (currentValue - 1 + modValue) % modValue
        }
        val currBin = currentValue.toString(2).padStart(4, '0')
        val nextBin = next.toString(2).padStart(4, '0')
        val dir = if (countUp) "UP (+1)" else "DOWN (-1)"
        val explanation = "Counter CLK ↑ [$dir, Mod-$modValue]: $currBin ($currentValue) -> $nextBin ($next)."
        return next to explanation
    }

    fun stepShiftRegister(
        bits: List<Int>, // [Q3, Q2, Q1, Q0]
        serialIn: Int,
        mode: ShiftRegisterMode
    ): Triple<List<Int>, Int, String> {
        val q3 = bits.getOrElse(0) { 0 }
        val q2 = bits.getOrElse(1) { 0 }
        val q1 = bits.getOrElse(2) { 0 }
        val q0 = bits.getOrElse(3) { 0 }

        val incoming = when (mode) {
            ShiftRegisterMode.SISO, ShiftRegisterMode.SIPO -> serialIn
            ShiftRegisterMode.RING -> q0
            ShiftRegisterMode.JOHNSON -> 1 - q0
        }

        val nextBits = listOf(incoming, q3, q2, q1)
        val serialOut = q0
        val oldStr = bits.joinToString("")
        val newStr = nextBits.joinToString("")
        val reason = when (mode) {
            ShiftRegisterMode.SISO -> "Serial In ($serialIn) -> Q3; Q3→Q2→Q1→Q0; Serial Out = $serialOut."
            ShiftRegisterMode.SIPO -> "Serial In ($serialIn) shifts in at Q3; parallel outputs Q3..Q0 = $newStr."
            ShiftRegisterMode.RING -> "Ring feedback Q0 ($q0) -> Q3; pattern rotates right: $oldStr -> $newStr."
            ShiftRegisterMode.JOHNSON -> "Twisted feedback Q0' (${1 - q0}) -> Q3; state advances $oldStr -> $newStr."
        }
        return Triple(nextBits, serialOut, reason)
    }
}
