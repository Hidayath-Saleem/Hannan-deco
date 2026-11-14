package com.example

import com.example.engine.BooleanExpressionEvaluator
import com.example.engine.CacheMappingMode
import com.example.engine.CacheSimulatorEngine
import com.example.engine.Cpu8BitEngine
import com.example.engine.FdePhase
import com.example.engine.FlipFlopEngine
import com.example.engine.FlipFlopType
import com.example.engine.KMapCellState
import com.example.engine.KMapSolver
import com.example.engine.LogicCircuitEvaluator
import com.example.engine.NumberBase
import com.example.engine.NumberSystemConverter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DecoEnginesTest {

    @Test
    fun `number converter computes 8-bit twos complement for negative decimal`() {
        val res = NumberSystemConverter.convert("-25", NumberBase.DECIMAL)
        assertTrue(res.isValid)
        assertEquals("11100111", res.twosComplement8Bit)
        assertEquals("-11001", res.binary)
        assertEquals("-31", res.octal)
        assertEquals("-19", res.hex)
    }

    @Test
    fun `boolean parser generates accurate truth table`() {
        val tt = BooleanExpressionEvaluator.generateTruthTable("A ^ B")
        assertTrue(tt.isValid)
        assertEquals(listOf(0, 1, 1, 0), tt.rows.map { it.output })
    }

    @Test
    fun `kmap solver simplifies 4-var wrap quad`() {
        val cells = List(16) { idx ->
            if (idx in listOf(0, 2, 8, 10)) KMapCellState.ONE else KMapCellState.ZERO
        }
        val sol = KMapSolver.solve(4, cells)
        assertEquals("F = B'·D'", sol.minimizedSop)
    }

    @Test
    fun `half adder circuit evaluates sum and carry`() {
        val (nodes, wires) = LogicCircuitEvaluator.presetHalfAdder()
        val res = LogicCircuitEvaluator.evaluate(nodes, wires)
        assertEquals(false, res.nodeOutputs["outS"])
        assertEquals(true, res.nodeOutputs["outC"])
    }

    @Test
    fun `jk flip flop toggles and sr flags invalid`() {
        val jk = FlipFlopEngine.step(FlipFlopType.JK, currentQ = 0, in1 = 1, in2 = 1, cycle = 1)
        assertEquals(1, jk.qNext)
        assertFalse(jk.isInvalid)

        val sr = FlipFlopEngine.step(FlipFlopType.SR, currentQ = 0, in1 = 1, in2 = 1, cycle = 1)
        assertTrue(sr.isInvalid)
    }

    @Test
    fun `8-bit cpu executes add two numbers program`() {
        var state = Cpu8BitEngine.assemble(Cpu8BitEngine.presets[0].code).getOrThrow()
        var steps = 0
        while (state.phase != FdePhase.HALTED && steps < 30) {
            state = Cpu8BitEngine.stepPhase(state)
            steps++
        }
        assertEquals(FdePhase.HALTED, state.phase)
        assertEquals(37, state.ram[20])
    }

    @Test
    fun `cache simulator detects direct-mapped conflict miss vs 2-way hit`() {
        val seq = listOf(0, 4, 0)
        var direct = CacheSimulatorEngine.reset(CacheMappingMode.DIRECT, 4, seq)
        repeat(3) { direct = CacheSimulatorEngine.step(direct) }
        assertEquals(0, direct.hits)
        assertEquals(3, direct.misses)

        var twoWay = CacheSimulatorEngine.reset(CacheMappingMode.TWO_WAY_SET, 4, seq)
        repeat(3) { twoWay = CacheSimulatorEngine.step(twoWay) }
        assertEquals(1, twoWay.hits)
        assertEquals(2, twoWay.misses)
    }
}
