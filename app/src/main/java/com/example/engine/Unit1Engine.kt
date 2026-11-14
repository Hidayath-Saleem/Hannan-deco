package com.example.engine

import kotlin.math.abs

enum class NumberBase(val label: String, val radix: Int, val hint: String) {
    DECIMAL("Decimal (Base 10)", 10, "e.g. -25 or 42"),
    BINARY("Binary (Base 2)", 2, "e.g. 101010"),
    OCTAL("Octal (Base 8)", 8, "e.g. 52"),
    HEX("Hexadecimal (Base 16)", 16, "e.g. 2A"),
    TWOS_COMPLEMENT("8-Bit 2's Comp", 2, "8 bits e.g. 11100111")
}

data class ConversionOutput(
    val isValid: Boolean,
    val errorMessage: String? = null,
    val signedDecimal: Long = 0,
    val binary: String = "0",
    val octal: String = "0",
    val hex: String = "0",
    val twosComplement8Bit: String = "00000000",
    val twosComplementNote: String = "",
    val steps: List<String> = emptyList()
)

object NumberSystemConverter {

    fun convert(rawInput: String, sourceBase: NumberBase): ConversionOutput {
        val input = rawInput.trim().uppercase()
        if (input.isEmpty()) {
            return ConversionOutput(
                isValid = false,
                errorMessage = "Enter a value to convert."
            )
        }

        val steps = mutableListOf<String>()
        val decimalValue: Long

        try {
            when (sourceBase) {
                NumberBase.DECIMAL -> {
                    val parsed = input.toLongOrNull()
                        ?: return ConversionOutput(false, "Invalid decimal integer.")
                    if (abs(parsed) > 65535L) {
                        return ConversionOutput(false, "Keep decimal within -65535 to 65535.")
                    }
                    decimalValue = parsed
                    steps += "1. Input parsed directly as signed Decimal: $decimalValue."
                }

                NumberBase.BINARY -> {
                    if (!input.matches(Regex("^-?[01]{1,16}$"))) {
                        return ConversionOutput(false, "Enter up to 16 binary digits (0 or 1).")
                    }
                    decimalValue = input.toLong(2)
                    val unsigned = input.removePrefix("-")
                    val expansion = unsigned.reversed().mapIndexedNotNull { idx, ch ->
                        if (ch == '1') "1×2^$idx" else null
                    }.reversed().joinToString(" + ").ifEmpty { "0" }
                    steps += "1. Positional expansion of ($input)₂: $expansion = $decimalValue."
                }

                NumberBase.OCTAL -> {
                    if (!input.matches(Regex("^-?[0-7]{1,6}$"))) {
                        return ConversionOutput(false, "Enter up to 6 octal digits (0–7).")
                    }
                    decimalValue = input.toLong(8)
                    val unsigned = input.removePrefix("-")
                    val expansion = unsigned.reversed().mapIndexed { idx, ch ->
                        "$ch×8^$idx"
                    }.reversed().joinToString(" + ")
                    steps += "1. Positional expansion of ($input)₈: $expansion = $decimalValue."
                }

                NumberBase.HEX -> {
                    val cleaned = input.removePrefix("0X")
                    if (!cleaned.matches(Regex("^-?[0-9A-F]{1,4}$"))) {
                        return ConversionOutput(false, "Enter up to 4 hex digits (0–9, A–F).")
                    }
                    decimalValue = cleaned.toLong(16)
                    val unsigned = cleaned.removePrefix("-")
                    val expansion = unsigned.reversed().mapIndexed { idx, ch ->
                        val digit = ch.digitToInt(16)
                        "$digit×16^$idx"
                    }.reversed().joinToString(" + ")
                    steps += "1. Positional expansion of (0x$cleaned)₁₆: $expansion = $decimalValue."
                }

                NumberBase.TWOS_COMPLEMENT -> {
                    if (!input.matches(Regex("^[01]{8}$"))) {
                        return ConversionOutput(false, "8-bit 2's complement requires exactly 8 bits (0/1).")
                    }
                    val unsignedVal = input.toInt(2)
                    if (input[0] == '1') {
                        decimalValue = (unsignedVal - 256).toLong()
                        val inverted = input.map { if (it == '0') '1' else '0' }.joinToString("")
                        val mag = inverted.toInt(2) + 1
                        steps += "1. MSB is 1 -> negative number in 8-bit 2's complement."
                        steps += "2. Invert bits ($input -> $inverted), then add 1 -> ${mag.toString(2).padStart(8, '0')} ($mag), so value = -$mag."
                    } else {
                        decimalValue = unsignedVal.toLong()
                        steps += "1. MSB is 0 -> positive number in 8-bit 2's complement = +$decimalValue."
                    }
                }
            }
        } catch (_: NumberFormatException) {
            return ConversionOutput(false, "Value out of supported range.")
        }

        val absVal = abs(decimalValue)
        val absBin = absVal.toString(2)
        val signPrefix = if (decimalValue < 0) "-" else ""

        if (sourceBase != NumberBase.BINARY && sourceBase != NumberBase.TWOS_COMPLEMENT) {
            if (absVal == 0L) {
                steps += "2. Decimal 0 in Binary is 0."
            } else {
                val divSteps = mutableListOf<String>()
                var q = absVal
                while (q > 0 && divSteps.size < 6) {
                    divSteps += "$q÷2=${q / 2} r${q % 2}"
                    q /= 2
                }
                val suffix = if (q > 0) " …" else ""
                steps += "2. Repeated division by 2: ${divSteps.joinToString(", ")}$suffix -> ${signPrefix}$absBin₂."
            }
        }

        val octStr = signPrefix + absVal.toString(8)
        val hexStr = signPrefix + absVal.toString(16).uppercase()
        steps += "3. Group binary bits by 3 for Octal ($octStr₈) and by 4 for Hex ($hexStr₁₆)."

        val twos8: String
        val twosNote: String
        if (decimalValue in -128L..127L) {
            val byteBits = (decimalValue.toInt() and 0xFF).toString(2).padStart(8, '0')
            twos8 = byteBits
            if (decimalValue < 0) {
                val posBits = absVal.toInt().toString(2).padStart(8, '0')
                val invBits = posBits.map { if (it == '0') '1' else '0' }.joinToString("")
                twosNote = "+$absVal = $posBits -> invert = $invBits -> +1 = $byteBits"
                steps += "4. 8-bit 2's complement of $decimalValue: $twosNote."
            } else {
                twosNote = "Positive value padded to 8 bits (MSB=0)"
                steps += "4. 8-bit 2's complement of +$decimalValue: $byteBits (MSB = 0)."
            }
        } else {
            twos8 = "Out of 8-bit range (-128..127)"
            twosNote = "Requires > 8 bits for signed 2's complement"
            steps += "4. $decimalValue exceeds signed 8-bit 2's complement range (-128 to +127)."
        }

        return ConversionOutput(
            isValid = true,
            signedDecimal = decimalValue,
            binary = signPrefix + absBin,
            octal = octStr,
            hex = hexStr,
            twosComplement8Bit = twos8,
            twosComplementNote = twosNote,
            steps = steps
        )
    }
}

data class TruthTableRow(
    val mintermIndex: Int,
    val inputs: List<Int>,
    val output: Int
)

data class TruthTableResult(
    val isValid: Boolean,
    val errorMessage: String? = null,
    val variables: List<String> = emptyList(),
    val rows: List<TruthTableRow> = emptyList(),
    val canonicalSop: String = "",
    val canonicalPos: String = "",
    val steps: List<String> = emptyList()
)

object BooleanExpressionEvaluator {

    private sealed class Expr {
        data class Const(val value: Boolean) : Expr()
        data class Var(val index: Int) : Expr()
        data class Not(val child: Expr) : Expr()
        data class BinOp(val op: String, val left: Expr, val right: Expr) : Expr()
    }

    private sealed class Token {
        data class VarToken(val name: String) : Token()
        data class ConstToken(val value: Boolean) : Token()
        data class OpToken(val op: String) : Token()
        data object LParen : Token()
        data object RParen : Token()
        data object PostNot : Token()
    }

    fun generateTruthTable(expression: String, forcedVarCount: Int? = null): TruthTableResult {
        val raw = expression.trim()
        if (raw.isEmpty()) {
            return TruthTableResult(false, "Enter a Boolean expression using A, B, C, D.")
        }

        val tokens = try {
            tokenize(raw)
        } catch (e: IllegalArgumentException) {
            return TruthTableResult(false, e.message ?: "Syntax error.")
        }

        if (tokens.isEmpty()) {
            return TruthTableResult(false, "Expression has no valid tokens.")
        }

        val usedIndices = tokens.filterIsInstance<Token.VarToken>().map {
            when (it.name) {
                "A" -> 0
                "B" -> 1
                "C" -> 2
                else -> 3
            }
        }
        val maxIdx = usedIndices.maxOrNull() ?: 1
        val varCount = (forcedVarCount ?: (maxIdx + 1)).coerceIn(2, 4)
        val vars = listOf("A", "B", "C", "D").take(varCount)

        val ast = try {
            Parser(tokens).parse()
        } catch (e: IllegalArgumentException) {
            return TruthTableResult(false, e.message ?: "Invalid Boolean syntax.")
        }

        val totalRows = 1 shl varCount
        val rows = ArrayList<TruthTableRow>(totalRows)
        val ones = mutableListOf<Int>()
        val zeros = mutableListOf<Int>()

        for (m in 0 until totalRows) {
            val bits = (0 until varCount).map { bit ->
                (m shr (varCount - 1 - bit)) and 1
            }
            val out = if (evalAst(ast, bits)) 1 else 0
            if (out == 1) ones += m else zeros += m
            rows += TruthTableRow(m, bits, out)
        }

        val sop = if (ones.isEmpty()) "0 (Contradiction)" else "Σm(${ones.joinToString(", ")})"
        val pos = if (zeros.isEmpty()) "1 (Tautology)" else "ΠM(${zeros.joinToString(", ")})"

        val sampleRow = rows.firstOrNull { it.output == 1 } ?: rows.first()
        val assignmentDesc = vars.zip(sampleRow.inputs).joinToString(", ") { (v, b) -> "$v=$b" }
        val steps = listOf(
            "1. Parsed expression \"$raw\" over ${varCount} variables (${vars.joinToString(", ")}) = ${totalRows} input combinations.",
            "2. Operator precedence evaluated: Parentheses -> NOT (!, ') -> AND (·, &) -> XOR (^) -> OR (+, |).",
            "3. Sample check for m${sampleRow.mintermIndex} ($assignmentDesc): evaluates to F = ${sampleRow.output}.",
            "4. Canonical forms: SOP = $sop | POS = $pos."
        )

        return TruthTableResult(
            isValid = true,
            variables = vars,
            rows = rows,
            canonicalSop = sop,
            canonicalPos = pos,
            steps = steps
        )
    }

    private fun evalAst(expr: Expr, bits: List<Int>): Boolean = when (expr) {
        is Expr.Const -> expr.value
        is Expr.Var -> bits.getOrElse(expr.index) { 0 } == 1
        is Expr.Not -> !evalAst(expr.child, bits)
        is Expr.BinOp -> {
            val l = evalAst(expr.left, bits)
            val r = evalAst(expr.right, bits)
            when (expr.op) {
                "AND" -> l && r
                "NAND" -> !(l && r)
                "OR" -> l || r
                "NOR" -> !(l || r)
                "XOR" -> l xor r
                "XNOR" -> !(l xor r)
                else -> l && r
            }
        }
    }

    private fun tokenize(input: String): List<Token> {
        val rawTokens = mutableListOf<Token>()
        var i = 0
        while (i < input.length) {
            val c = input[i]
            when {
                c.isWhitespace() -> i++
                c == '(' -> { rawTokens += Token.LParen; i++ }
                c == ')' -> { rawTokens += Token.RParen; i++ }
                c == '\'' -> { rawTokens += Token.PostNot; i++ }
                c == '!' || c == '~' -> { rawTokens += Token.OpToken("NOT"); i++ }
                c == '&' || c == '*' || c == '·' || c == '.' -> {
                    if (c == '&' && i + 1 < input.length && input[i + 1] == '&') i++
                    rawTokens += Token.OpToken("AND"); i++
                }
                c == '|' || c == '+' -> {
                    if (c == '|' && i + 1 < input.length && input[i + 1] == '|') i++
                    rawTokens += Token.OpToken("OR"); i++
                }
                c == '^' || c == '⊕' -> { rawTokens += Token.OpToken("XOR"); i++ }
                c == '0' -> { rawTokens += Token.ConstToken(false); i++ }
                c == '1' -> { rawTokens += Token.ConstToken(true); i++ }
                c.isLetter() -> {
                    val start = i
                    while (i < input.length && input[i].isLetter()) i++
                    val word = input.substring(start, i).uppercase()
                    when (word) {
                        "AND", "OR", "XOR", "NAND", "NOR", "XNOR", "NOT" -> {
                            rawTokens += Token.OpToken(word)
                        }
                        else -> {
                            for (ch in word) {
                                if (ch in 'A'..'D') {
                                    rawTokens += Token.VarToken(ch.toString())
                                } else {
                                    throw IllegalArgumentException("Unknown symbol '$word'. Use variables A, B, C, D.")
                                }
                            }
                        }
                    }
                }
                else -> throw IllegalArgumentException("Unsupported character '$c'.")
            }
        }

        // Insert implicit AND for patterns like AB, A'B, A(B+C), )A, )(
        val withImplicit = mutableListOf<Token>()
        for (idx in rawTokens.indices) {
            val curr = rawTokens[idx]
            if (idx > 0) {
                val prev = rawTokens[idx - 1]
                val prevEndsOperand = prev is Token.VarToken || prev is Token.ConstToken ||
                    prev is Token.RParen || prev is Token.PostNot
                val currStartsOperand = curr is Token.VarToken || curr is Token.ConstToken ||
                    curr is Token.LParen || (curr is Token.OpToken && curr.op == "NOT")
                if (prevEndsOperand && currStartsOperand) {
                    withImplicit += Token.OpToken("AND")
                }
            }
            withImplicit += curr
        }
        return withImplicit
    }

    private class Parser(private val tokens: List<Token>) {
        private var pos = 0

        fun parse(): Expr {
            val expr = parseOr()
            if (pos < tokens.size) {
                throw IllegalArgumentException("Unexpected token near position ${pos + 1}.")
            }
            return expr
        }

        private fun parseOr(): Expr {
            var left = parseXor()
            while (matchOp("OR", "NOR")) {
                val op = (tokens[pos - 1] as Token.OpToken).op
                left = Expr.BinOp(op, left, parseXor())
            }
            return left
        }

        private fun parseXor(): Expr {
            var left = parseAnd()
            while (matchOp("XOR", "XNOR")) {
                val op = (tokens[pos - 1] as Token.OpToken).op
                left = Expr.BinOp(op, left, parseAnd())
            }
            return left
        }

        private fun parseAnd(): Expr {
            var left = parseUnary()
            while (matchOp("AND", "NAND")) {
                val op = (tokens[pos - 1] as Token.OpToken).op
                left = Expr.BinOp(op, left, parseUnary())
            }
            return left
        }

        private fun parseUnary(): Expr {
            if (matchOp("NOT")) {
                return Expr.Not(parseUnary())
            }
            var node = parsePrimary()
            while (pos < tokens.size && tokens[pos] is Token.PostNot) {
                pos++
                node = Expr.Not(node)
            }
            return node
        }

        private fun parsePrimary(): Expr {
            if (pos >= tokens.size) throw IllegalArgumentException("Unexpected end of expression.")
            return when (val t = tokens[pos++]) {
                is Token.VarToken -> {
                    val idx = when (t.name) {
                        "A" -> 0; "B" -> 1; "C" -> 2; else -> 3
                    }
                    Expr.Var(idx)
                }
                is Token.ConstToken -> Expr.Const(t.value)
                is Token.LParen -> {
                    val inner = parseOr()
                    if (pos >= tokens.size || tokens[pos] !is Token.RParen) {
                        throw IllegalArgumentException("Missing closing parenthesis ')'.")
                    }
                    pos++
                    inner
                }
                else -> throw IllegalArgumentException("Expected variable or '('.")
            }
        }

        private fun matchOp(vararg ops: String): Boolean {
            if (pos < tokens.size) {
                val t = tokens[pos]
                if (t is Token.OpToken && t.op in ops) {
                    pos++
                    return true
                }
            }
            return false
        }
    }
}

enum class KMapCellState(val symbol: String) {
    ZERO("0"), ONE("1"), DONT_CARE("X");

    fun next(): KMapCellState = when (this) {
        ZERO -> ONE
        ONE -> DONT_CARE
        DONT_CARE -> ZERO
    }
}

data class KMapImplicantGroup(
    val term: String,
    val coveredMinterms: Set<Int>,
    val size: Int,
    val colorIndex: Int,
    val explanation: String
)

data class KMapSolution(
    val varCount: Int,
    val minimizedSop: String,
    val groups: List<KMapImplicantGroup>,
    val steps: List<String>
)

object KMapSolver {

    val grayCode2 = listOf(0, 1)
    val grayCode4 = listOf(0, 1, 3, 2)

    fun mintermAtGrid(varCount: Int, row: Int, col: Int): Int = when (varCount) {
        2 -> (row shl 1) or col
        3 -> (row shl 2) or grayCode4[col]
        else -> (grayCode4[row] shl 2) or grayCode4[col]
    }

    private data class Implicant(val bits: Int, val mask: Int, val minterms: Set<Int>)

    fun solve(varCount: Int, cells: List<KMapCellState>): KMapSolution {
        val n = varCount.coerceIn(2, 4)
        val total = 1 shl n
        val safeCells = if (cells.size >= total) cells.take(total) else List(total) { cells.getOrElse(it) { KMapCellState.ZERO } }

        val minterms = (0 until total).filter { safeCells[it] == KMapCellState.ONE }
        val dontCares = (0 until total).filter { safeCells[it] == KMapCellState.DONT_CARE }

        if (minterms.isEmpty()) {
            return KMapSolution(
                varCount = n,
                minimizedSop = "F = 0",
                groups = emptyList(),
                steps = listOf("1. No 1-cells are present in the K-Map, so the minimized SOP function is constant 0.")
            )
        }

        val allOnesOrX = (minterms + dontCares).toSet()
        if (allOnesOrX.size == total) {
            return KMapSolution(
                varCount = n,
                minimizedSop = "F = 1",
                groups = listOf(
                    KMapImplicantGroup(
                        term = "1",
                        coveredMinterms = (0 until total).toSet(),
                        size = total,
                        colorIndex = 0,
                        explanation = "All $total cells are 1 or X, eliminating every variable -> F = 1."
                    )
                ),
                steps = listOf("1. Every cell in the $n-variable K-Map is 1 (or X), forming a single group of $total -> F = 1.")
            )
        }

        val varNames = listOf("A", "B", "C", "D").take(n)

        var current = allOnesOrX.map { m -> Implicant(m, 0, setOf(m)) }.distinctBy { it.bits to it.mask }
        val primeImplicants = mutableListOf<Implicant>()

        while (current.isNotEmpty()) {
            val used = BooleanArray(current.size)
            val nextGen = mutableListOf<Implicant>()
            for (i in current.indices) {
                for (j in i + 1 until current.size) {
                    val a = current[i]
                    val b = current[j]
                    if (a.mask == b.mask) {
                        val diff = (a.bits xor b.bits) and a.mask.inv()
                        if (diff != 0 && (diff and (diff - 1)) == 0) {
                            used[i] = true
                            used[j] = true
                            val newMask = a.mask or diff
                            val newBits = a.bits and newMask.inv()
                            nextGen += Implicant(newBits, newMask, a.minterms + b.minterms)
                        }
                    }
                }
            }
            for (i in current.indices) {
                if (!used[i]) primeImplicants += current[i]
            }
            current = nextGen.distinctBy { it.bits to it.mask }
        }

        val uniquePrimes = primeImplicants.distinctBy { it.bits to it.mask }
        val mintermSet = minterms.toSet()

        val selected = mutableListOf<Implicant>()
        val uncovered = mintermSet.toMutableSet()

        for (m in minterms) {
            val covering = uniquePrimes.filter { m in it.minterms }
            if (covering.size == 1) {
                val epi = covering.first()
                if (epi !in selected) {
                    selected += epi
                    uncovered.removeAll(epi.minterms)
                }
            }
        }

        if (uncovered.isNotEmpty()) {
            val candidates = uniquePrimes
                .filter { pi -> pi !in selected && pi.minterms.any { it in uncovered } }
                .sortedByDescending { it.minterms.count { m -> m in uncovered } }
            selected += findMinimalCover(uncovered, candidates)
        }

        val groups = selected.mapIndexed { idx, imp ->
            val term = implicantToTerm(imp.bits, imp.mask, varNames)
            val eliminated = (0 until n).filter { bitPos ->
                val bitFlag = 1 shl (n - 1 - bitPos)
                (imp.mask and bitFlag) != 0
            }.map { varNames[it] }
            val sizeName = when (imp.minterms.size) {
                16 -> "16-cell group"
                8 -> "Octet (8-cell)"
                4 -> "Quad (4-cell)"
                2 -> "Pair (2-cell)"
                else -> "Single (1-cell)"
            }
            val elimMsg = if (eliminated.isEmpty()) "no literals eliminated" else "eliminates ${eliminated.joinToString(", ")}"
            val ms = imp.minterms.sorted().joinToString(", ")
            KMapImplicantGroup(
                term = term,
                coveredMinterms = imp.minterms,
                size = imp.minterms.size,
                colorIndex = idx % 6,
                explanation = "$sizeName m($ms): $elimMsg -> $term"
            )
        }

        val sopExpr = "F = " + groups.joinToString(" + ") { it.term }
        val steps = buildList {
            add("1. Active minterms Σm(${minterms.joinToString(", ")})" + if (dontCares.isNotEmpty()) " with Don't-Cares d(${dontCares.joinToString(", ")})." else ".")
            add("2. Identified ${uniquePrimes.size} prime implicant(s) and selected ${groups.size} minimal cover group(s).")
            groups.forEachIndexed { idx, g ->
                add("${idx + 3}. Group ${idx + 1}: ${g.explanation}")
            }
        }

        return KMapSolution(
            varCount = n,
            minimizedSop = sopExpr,
            groups = groups,
            steps = steps
        )
    }

    private fun implicantToTerm(bits: Int, mask: Int, varNames: List<String>): String {
        val n = varNames.size
        val literals = mutableListOf<String>()
        for (i in 0 until n) {
            val flag = 1 shl (n - 1 - i)
            if ((mask and flag) == 0) {
                val isOne = (bits and flag) != 0
                literals += if (isOne) varNames[i] else "${varNames[i]}'"
            }
        }
        return if (literals.isEmpty()) "1" else literals.joinToString("·")
    }

    private fun findMinimalCover(
        uncovered: Set<Int>,
        candidates: List<Implicant>
    ): List<Implicant> {
        var best: List<Implicant>? = null

        fun search(remaining: Set<Int>, current: List<Implicant>) {
            val currentBest = best
            if (currentBest != null && current.size >= currentBest.size) return
            if (remaining.isEmpty()) {
                best = current
                return
            }
            // Pick uncovered minterm covered by fewest candidates (MRV heuristic)
            val target = remaining.minByOrNull { m -> candidates.count { m in it.minterms } } ?: return
            val options = candidates
                .filter { target in it.minterms }
                .sortedByDescending { it.minterms.count { m -> m in remaining } }
            for (opt in options) {
                search(remaining - opt.minterms, current + opt)
            }
        }

        search(uncovered, emptyList())
        return best ?: emptyList()
    }
}
