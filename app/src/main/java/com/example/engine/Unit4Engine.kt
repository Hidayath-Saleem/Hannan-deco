package com.example.engine

enum class FdePhase(val label: String) {
    FETCH("FETCH"),
    DECODE("DECODE"),
    EXECUTE("EXECUTE"),
    HALTED("HALTED")
}

data class DecodedInstruction(
    val opcode: String,
    val op1: String = "",
    val op2: String = "",
    val sourceLine: String = ""
)

data class CpuState(
    val pc: Int = 0,
    val ir: Int = 0,
    val mar: Int = 0,
    val mdr: Int = 0,
    val acc: Int = 0,
    val registers: List<Int> = listOf(0, 0, 0, 0), // R0..R3 (8-bit 0..255)
    val flagZ: Boolean = false,
    val flagC: Boolean = false,
    val flagN: Boolean = false,
    val phase: FdePhase = FdePhase.FETCH,
    val ram: List<Int> = List(32) { 0 },
    val programByAddress: Map<Int, DecodedInstruction> = emptyMap(),
    val decodedCurrent: DecodedInstruction? = null,
    val cycleCount: Int = 0,
    val lastActiveBlocks: Set<String> = emptySet(),
    val stepExplanation: String = "CPU Reset. Tap 'Step Phase' to start FETCH from PC = 0x00."
)

data class AssemblyPreset(
    val name: String,
    val description: String,
    val code: String
)

object Cpu8BitEngine {

    val presets = listOf(
        AssemblyPreset(
            name = "Add Two Numbers",
            description = "Loads 12 and 25 into R0 & R1, adds them, stores sum (37) at RAM[20].",
            code = """
                LOAD R0, #12
                LOAD R1, #25
                ADD R0, R1
                STORE R0, [20]
                HLT
            """.trimIndent()
        ),
        AssemblyPreset(
            name = "Countdown Loop",
            description = "Decrements R0 from 3 down to 0 using JZ conditional branch.",
            code = """
                LOAD R0, #3
                LOAD R1, #1
                DEC R0
                JZ 5
                JMP 2
                STORE R0, [24]
                HLT
            """.trimIndent()
        ),
        AssemblyPreset(
            name = "Bitwise ALU Lab",
            description = "Demonstrates AND, OR, XOR bit masking on 8-bit values.",
            code = """
                LOAD R0, #0xAC
                LOAD R1, #0x0F
                AND R0, R1
                LOAD R2, #0x50
                OR R0, R2
                XOR R0, R1
                STORE R0, [28]
                HLT
            """.trimIndent()
        )
    )

    fun assemble(source: String): Result<CpuState> {
        val lines = source.lines()
            .map { it.substringBefore(";").substringBefore("//").trim() }
            .filter { it.isNotEmpty() }

        if (lines.isEmpty()) {
            return Result.failure(IllegalArgumentException("Assembly program is empty."))
        }
        if (lines.size > 20) {
            return Result.failure(IllegalArgumentException("Keep program within 20 instructions (RAM 0x00..0x13)."))
        }

        val ram = MutableList(32) { 0 }
        val progMap = mutableMapOf<Int, DecodedInstruction>()

        for ((idx, line) in lines.withIndex()) {
            val parts = line.replace(",", " ").split(Regex("\\s+")).filter { it.isNotEmpty() }
            val op = parts[0].uppercase()
            val decoded = when (op) {
                "HLT", "NOP" -> DecodedInstruction(op, sourceLine = line)
                "INC", "DEC" -> {
                    val reg = parseReg(parts.getOrNull(1))
                        ?: return Result.failure(IllegalArgumentException("Line ${idx + 1}: Invalid register in '$line'."))
                    DecodedInstruction(op, "R$reg", sourceLine = line)
                }
                "JMP", "JZ" -> {
                    val addr = parseNumber(parts.getOrNull(1))
                        ?: return Result.failure(IllegalArgumentException("Line ${idx + 1}: Invalid target address in '$line'."))
                    if (addr !in 0..31) {
                        return Result.failure(IllegalArgumentException("Line ${idx + 1}: Address $addr out of RAM range (0..31)."))
                    }
                    DecodedInstruction(op, addr.toString(), sourceLine = line)
                }
                "LOAD" -> {
                    val rd = parseReg(parts.getOrNull(1))
                        ?: return Result.failure(IllegalArgumentException("Line ${idx + 1}: Expected destination R0..R3."))
                    val src = parts.getOrNull(2)
                        ?: return Result.failure(IllegalArgumentException("Line ${idx + 1}: Missing source operand."))
                    if (src.startsWith("#")) {
                        val imm = parseNumber(src.removePrefix("#"))
                            ?: return Result.failure(IllegalArgumentException("Line ${idx + 1}: Invalid immediate '$src'."))
                        DecodedInstruction("LOAD_IMM", "R$rd", (imm and 0xFF).toString(), line)
                    } else if (src.startsWith("[") && src.endsWith("]")) {
                        val addr = parseNumber(src.removeSurrounding("[", "]"))
                            ?: return Result.failure(IllegalArgumentException("Line ${idx + 1}: Invalid memory address '$src'."))
                        if (addr !in 0..31) {
                            return Result.failure(IllegalArgumentException("Line ${idx + 1}: Address $addr out of 0..31."))
                        }
                        DecodedInstruction("LOAD_MEM", "R$rd", addr.toString(), line)
                    } else {
                        return Result.failure(IllegalArgumentException("Line ${idx + 1}: Use #imm or [addr] for LOAD."))
                    }
                }
                "STORE" -> {
                    val rs = parseReg(parts.getOrNull(1))
                        ?: return Result.failure(IllegalArgumentException("Line ${idx + 1}: Expected source R0..R3."))
                    val dst = parts.getOrNull(2) ?: ""
                    if (!dst.startsWith("[") || !dst.endsWith("]")) {
                        return Result.failure(IllegalArgumentException("Line ${idx + 1}: STORE target must be [addr]."))
                    }
                    val addr = parseNumber(dst.removeSurrounding("[", "]"))
                        ?: return Result.failure(IllegalArgumentException("Line ${idx + 1}: Invalid RAM address '$dst'."))
                    if (addr !in 0..31) {
                        return Result.failure(IllegalArgumentException("Line ${idx + 1}: Address $addr out of 0..31."))
                    }
                    DecodedInstruction("STORE", "R$rs", addr.toString(), line)
                }
                "ADD", "SUB", "AND", "OR", "XOR" -> {
                    val rd = parseReg(parts.getOrNull(1))
                        ?: return Result.failure(IllegalArgumentException("Line ${idx + 1}: Expected Rd (R0..R3)."))
                    val rs = parseReg(parts.getOrNull(2))
                        ?: return Result.failure(IllegalArgumentException("Line ${idx + 1}: Expected Rs (R0..R3)."))
                    DecodedInstruction(op, "R$rd", "R$rs", line)
                }
                else -> return Result.failure(IllegalArgumentException("Line ${idx + 1}: Unknown opcode '$op'."))
            }
            progMap[idx] = decoded
            ram[idx] = encodeOpcodeByte(decoded)
        }

        return Result.success(
            CpuState(
                ram = ram,
                programByAddress = progMap,
                phase = FdePhase.FETCH,
                lastActiveBlocks = setOf("PC"),
                stepExplanation = "Assembled ${lines.size} instruction(s) into RAM[0x00..0x${(lines.size - 1).toString(16).uppercase().padStart(2, '0')}]. Tap 'Step Phase' to begin FETCH."
            )
        )
    }

    fun stepPhase(state: CpuState): CpuState {
        if (state.phase == FdePhase.HALTED) return state

        return when (state.phase) {
            FdePhase.FETCH -> {
                val addr = state.pc.coerceIn(0, 31)
                val word = state.ram[addr]
                val nextPc = (addr + 1) and 0x1F
                val instr = state.programByAddress[addr]
                state.copy(
                    mar = addr,
                    mdr = word,
                    ir = word,
                    pc = nextPc,
                    decodedCurrent = instr,
                    phase = FdePhase.DECODE,
                    lastActiveBlocks = setOf("PC", "MAR", "RAM", "MDR", "IR"),
                    stepExplanation = "FETCH: MAR ← PC (0x${hex2(addr)}), MDR ← RAM[0x${hex2(addr)}] (0x${hex2(word)}), IR ← MDR, PC ← PC+1 (0x${hex2(nextPc)})."
                )
            }

            FdePhase.DECODE -> {
                val instr = state.decodedCurrent ?: DecodedInstruction("NOP", sourceLine = "NOP")
                val desc = when (instr.opcode) {
                    "LOAD_IMM" -> "DECODE: Control Unit decodes '${instr.sourceLine}' -> Immediate load #${instr.op2} into ${instr.op1}."
                    "LOAD_MEM" -> "DECODE: Control Unit decodes '${instr.sourceLine}' -> Memory read from RAM[${instr.op2}] into ${instr.op1}."
                    "STORE" -> "DECODE: Control Unit decodes '${instr.sourceLine}' -> Write ${instr.op1} into RAM[${instr.op2}]."
                    "ADD", "SUB", "AND", "OR", "XOR" -> "DECODE: Control Unit decodes '${instr.sourceLine}' -> Select ${instr.op1} and ${instr.op2} for ALU ${instr.opcode}."
                    "INC", "DEC" -> "DECODE: Control Unit decodes '${instr.sourceLine}' -> Route ${instr.op1} to ALU ${instr.opcode}."
                    "JMP" -> "DECODE: Control Unit decodes '${instr.sourceLine}' -> Unconditional branch target 0x${hex2(instr.op1.toInt())}."
                    "JZ" -> "DECODE: Control Unit decodes '${instr.sourceLine}' -> Check Zero Flag (Z=${if (state.flagZ) 1 else 0}) for branch to 0x${hex2(instr.op1.toInt())}."
                    "HLT" -> "DECODE: Control Unit decodes HLT -> Prepare to halt execution."
                    else -> "DECODE: No-op decoded."
                }
                state.copy(
                    phase = FdePhase.EXECUTE,
                    lastActiveBlocks = setOf("IR", "CU"),
                    stepExplanation = desc
                )
            }

            FdePhase.EXECUTE -> executeInstruction(state)
            FdePhase.HALTED -> state
        }
    }

    fun stepFullInstruction(state: CpuState): CpuState {
        var s = state
        var guard = 0
        while (s.phase != FdePhase.HALTED && guard < 3) {
            s = stepPhase(s)
            guard++
            if (s.phase == FdePhase.FETCH) break
        }
        return s
    }

    private fun executeInstruction(state: CpuState): CpuState {
        val instr = state.decodedCurrent ?: return state.copy(phase = FdePhase.HALTED)
        val regs = state.registers.toMutableList()
        val ram = state.ram.toMutableList()
        var pc = state.pc
        var acc = state.acc
        var z = state.flagZ
        var c = state.flagC
        var n = state.flagN
        var mar = state.mar
        var mdr = state.mdr
        var nextPhase = FdePhase.FETCH
        val active: Set<String>
        val explanation: String

        when (instr.opcode) {
            "LOAD_IMM" -> {
                val rd = instr.op1.removePrefix("R").toInt()
                val imm = instr.op2.toInt() and 0xFF
                regs[rd] = imm
                acc = imm
                z = imm == 0
                n = (imm and 0x80) != 0
                c = false
                active = setOf("CU", "REG", "ACC")
                explanation = "EXECUTE: ${instr.op1} ← #$imm (0x${hex2(imm)}). Flags: Z=${b(z)} N=${b(n)}."
            }

            "LOAD_MEM" -> {
                val rd = instr.op1.removePrefix("R").toInt()
                val addr = instr.op2.toInt()
                mar = addr
                mdr = ram[addr]
                regs[rd] = mdr
                acc = mdr
                z = mdr == 0
                n = (mdr and 0x80) != 0
                active = setOf("MAR", "RAM", "MDR", "REG")
                explanation = "EXECUTE: MAR ← $addr, MDR ← RAM[$addr] ($mdr), ${instr.op1} ← MDR."
            }

            "STORE" -> {
                val rs = instr.op1.removePrefix("R").toInt()
                val addr = instr.op2.toInt()
                mar = addr
                mdr = regs[rs]
                ram[addr] = mdr
                active = setOf("REG", "MAR", "MDR", "RAM")
                explanation = "EXECUTE: MAR ← $addr, MDR ← ${instr.op1} ($mdr), RAM[$addr] ← $mdr."
            }

            "ADD", "SUB", "AND", "OR", "XOR" -> {
                val rd = instr.op1.removePrefix("R").toInt()
                val rs = instr.op2.removePrefix("R").toInt()
                val a = regs[rd]
                val bVal = regs[rs]
                val raw = when (instr.opcode) {
                    "ADD" -> a + bVal
                    "SUB" -> a - bVal
                    "AND" -> a and bVal
                    "OR" -> a or bVal
                    else -> a xor bVal
                }
                val res = raw and 0xFF
                c = when (instr.opcode) {
                    "ADD" -> raw > 0xFF
                    "SUB" -> a < bVal
                    else -> false
                }
                z = res == 0
                n = (res and 0x80) != 0
                regs[rd] = res
                acc = res
                active = setOf("REG", "ALU", "ACC")
                explanation = "EXECUTE: ALU computes ${instr.op1}($a) ${instr.opcode} ${instr.op2}($bVal) = $res (0x${hex2(res)}). Flags: Z=${b(z)} C=${b(c)} N=${b(n)}."
            }

            "INC", "DEC" -> {
                val rd = instr.op1.removePrefix("R").toInt()
                val a = regs[rd]
                val raw = if (instr.opcode == "INC") a + 1 else a - 1
                val res = raw and 0xFF
                c = if (instr.opcode == "INC") raw > 0xFF else a == 0
                z = res == 0
                n = (res and 0x80) != 0
                regs[rd] = res
                acc = res
                active = setOf("REG", "ALU", "ACC")
                explanation = "EXECUTE: ALU ${instr.opcode} ${instr.op1}: $a -> $res. Flags: Z=${b(z)} C=${b(c)} N=${b(n)}."
            }

            "JMP" -> {
                val target = instr.op1.toInt()
                pc = target
                active = setOf("CU", "PC")
                explanation = "EXECUTE: PC ← 0x${hex2(target)} (unconditional jump)."
            }

            "JZ" -> {
                val target = instr.op1.toInt()
                active = setOf("CU", "PC")
                if (z) {
                    pc = target
                    explanation = "EXECUTE: Z=1 (true) -> Branch taken! PC ← 0x${hex2(target)}."
                } else {
                    explanation = "EXECUTE: Z=0 (false) -> Branch not taken; PC stays 0x${hex2(pc)}."
                }
            }

            "HLT" -> {
                nextPhase = FdePhase.HALTED
                active = setOf("CU")
                explanation = "EXECUTE: HLT reached. CPU clock halted after ${state.cycleCount + 1} instruction cycle(s)."
            }

            else -> {
                active = setOf("CU")
                explanation = "EXECUTE: NOP completed."
            }
        }

        return state.copy(
            pc = pc,
            mar = mar,
            mdr = mdr,
            acc = acc,
            registers = regs,
            ram = ram,
            flagZ = z,
            flagC = c,
            flagN = n,
            phase = nextPhase,
            cycleCount = state.cycleCount + 1,
            lastActiveBlocks = active,
            stepExplanation = explanation
        )
    }

    private fun parseReg(token: String?): Int? {
        val t = token?.uppercase() ?: return null
        return when (t) {
            "R0" -> 0; "R1" -> 1; "R2" -> 2; "R3" -> 3; else -> null
        }
    }

    private fun parseNumber(token: String?): Int? {
        val t = token?.trim()?.uppercase() ?: return null
        return if (t.startsWith("0X")) t.removePrefix("0X").toIntOrNull(16) else t.toIntOrNull(10)
    }

    private fun encodeOpcodeByte(instr: DecodedInstruction): Int {
        val opCodeId = when (instr.opcode) {
            "NOP" -> 0x00
            "LOAD_IMM" -> 0x10
            "LOAD_MEM" -> 0x18
            "STORE" -> 0x20
            "ADD" -> 0x30
            "SUB" -> 0x40
            "AND" -> 0x50
            "OR" -> 0x60
            "XOR" -> 0x70
            "INC" -> 0x80
            "DEC" -> 0x88
            "JMP" -> 0x90
            "JZ" -> 0xA0
            "HLT" -> 0xFF
            else -> 0x00
        }
        val rBit = instr.op1.removePrefix("R").toIntOrNull()?.and(0x03) ?: 0
        return (opCodeId or rBit) and 0xFF
    }

    private fun hex2(v: Int): String = (v and 0xFF).toString(16).uppercase().padStart(2, '0')
    private fun b(v: Boolean): Int = if (v) 1 else 0
}
