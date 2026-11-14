package com.example.engine

enum class CacheMappingMode(val title: String, val subtitle: String) {
    DIRECT("Direct Mapped", "Each block maps to exactly 1 line: Line = Block mod N"),
    FULLY_ASSOC("Fully Associative", "Any block can go in any line (LRU replacement)"),
    TWO_WAY_SET("2-Way Set Assoc", "2 lines per set: Set = Block mod (N/2), LRU inside set")
}

data class CacheLineSlot(
    val lineIndex: Int,
    val setIndex: Int,
    val valid: Boolean = false,
    val tag: Int? = null,
    val blockAddress: Int? = null,
    val lruStamp: Int = 0
)

data class CacheAccessTrace(
    val stepIndex: Int,
    val blockAddress: Int,
    val tagBits: Int,
    val indexBits: Int,
    val isHit: Boolean,
    val missType: String, // "HIT", "Compulsory Miss", "Conflict Miss", "Capacity Miss"
    val targetLine: Int,
    val explanation: String
)

data class CacheSimState(
    val mode: CacheMappingMode = CacheMappingMode.DIRECT,
    val lineCount: Int = 4,
    val lines: List<CacheLineSlot> = List(4) { CacheLineSlot(it, it) },
    val sequence: List<Int> = listOf(0, 4, 0, 8, 1, 0, 4, 1),
    val nextPointer: Int = 0,
    val hits: Int = 0,
    val misses: Int = 0,
    val seenBlocks: Set<Int> = emptySet(),
    val lastTrace: CacheAccessTrace? = null
) {
    val totalAccesses: Int get() = hits + misses
    val hitRatioPercent: Int get() = if (totalAccesses == 0) 0 else (hits * 100) / totalAccesses
}

object CacheSimulatorEngine {

    val sampleSequences = listOf(
        "Thrash Test (0,4,0,8,1,0,4,1)" to "0, 4, 0, 8, 1, 0, 4, 1",
        "Spatial Loop (0,1,2,3,0,1,2,3)" to "0, 1, 2, 3, 0, 1, 2, 3",
        "Set Conflict (0,2,4,0,6,2,0,4)" to "0, 2, 4, 0, 6, 2, 0, 4"
    )

    fun parseAddressSequence(raw: String): List<Int> =
        raw.split(",", " ", ";")
            .mapNotNull { it.trim().toIntOrNull() }
            .filter { it in 0..63 }
            .take(16)

    fun reset(mode: CacheMappingMode, lineCount: Int, sequence: List<Int>): CacheSimState {
        val slots = List(lineCount) { idx ->
            val setIdx = when (mode) {
                CacheMappingMode.DIRECT -> idx
                CacheMappingMode.FULLY_ASSOC -> 0
                CacheMappingMode.TWO_WAY_SET -> idx / 2
            }
            CacheLineSlot(lineIndex = idx, setIndex = setIdx)
        }
        return CacheSimState(
            mode = mode,
            lineCount = lineCount,
            lines = slots,
            sequence = sequence
        )
    }

    fun step(state: CacheSimState): CacheSimState {
        if (state.nextPointer >= state.sequence.size) return state
        val stepNum = state.nextPointer + 1
        val addr = state.sequence[state.nextPointer]
        val lines = state.lines.toMutableList()
        val seenBefore = addr in state.seenBlocks

        val targetSet: Int
        val tagVal: Int
        val candidateIndices: List<Int>

        when (state.mode) {
            CacheMappingMode.DIRECT -> {
                targetSet = addr % state.lineCount
                tagVal = addr / state.lineCount
                candidateIndices = listOf(targetSet)
            }
            CacheMappingMode.FULLY_ASSOC -> {
                targetSet = 0
                tagVal = addr
                candidateIndices = lines.indices.toList()
            }
            CacheMappingMode.TWO_WAY_SET -> {
                val setCount = (state.lineCount / 2).coerceAtLeast(1)
                targetSet = addr % setCount
                tagVal = addr / setCount
                candidateIndices = lines.indices.filter { lines[it].setIndex == targetSet }
            }
        }

        val hitIdx = candidateIndices.firstOrNull { idx ->
            lines[idx].valid && lines[idx].tag == tagVal
        }

        return if (hitIdx != null) {
            lines[hitIdx] = lines[hitIdx].copy(lruStamp = stepNum)
            val trace = CacheAccessTrace(
                stepIndex = stepNum,
                blockAddress = addr,
                tagBits = tagVal,
                indexBits = targetSet,
                isHit = true,
                missType = "HIT",
                targetLine = hitIdx,
                explanation = "Access #$stepNum -> Block $addr (Tag=$tagVal, Set/Line=$targetSet): FOUND in Line $hitIdx! Cache HIT."
            )
            state.copy(
                lines = lines,
                nextPointer = state.nextPointer + 1,
                hits = state.hits + 1,
                seenBlocks = state.seenBlocks + addr,
                lastTrace = trace
            )
        } else {
            val emptyIdx = candidateIndices.firstOrNull { !lines[it].valid }
            val chosenIdx = emptyIdx ?: candidateIndices.minByOrNull { lines[it].lruStamp }!!
            val evictedBlock = lines[chosenIdx].blockAddress
            val allCacheFull = lines.all { it.valid }
            val missKind = when {
                !seenBefore -> "Compulsory Miss"
                state.mode == CacheMappingMode.FULLY_ASSOC || allCacheFull -> "Capacity Miss"
                else -> "Conflict Miss"
            }

            lines[chosenIdx] = lines[chosenIdx].copy(
                valid = true,
                tag = tagVal,
                blockAddress = addr,
                lruStamp = stepNum
            )

            val evictMsg = if (evictedBlock != null) " Replaced Block $evictedBlock via LRU." else " Loaded into empty Line $chosenIdx."
            val trace = CacheAccessTrace(
                stepIndex = stepNum,
                blockAddress = addr,
                tagBits = tagVal,
                indexBits = targetSet,
                isHit = false,
                missType = missKind,
                targetLine = chosenIdx,
                explanation = "Access #$stepNum -> Block $addr (Tag=$tagVal, Set/Line=$targetSet): $missKind.$evictMsg"
            )
            state.copy(
                lines = lines,
                nextPointer = state.nextPointer + 1,
                misses = state.misses + 1,
                seenBlocks = state.seenBlocks + addr,
                lastTrace = trace
            )
        }
    }
}

enum class PagePolicy(val title: String) {
    FIFO("FIFO (First-In First-Out)"),
    LRU("LRU (Least Recently Used)")
}

data class PageFrame(
    val frameIndex: Int,
    val loadedPage: Int? = null,
    val lastUsedStep: Int = 0,
    val loadedAtStep: Int = 0
)

data class PagingSimState(
    val policy: PagePolicy = PagePolicy.LRU,
    val pageSequence: List<Int> = listOf(1, 2, 3, 4, 1, 2, 5, 1, 2, 3),
    val frames: List<PageFrame> = List(3) { PageFrame(it) },
    val pointer: Int = 0,
    val hits: Int = 0,
    val faults: Int = 0,
    val lastIsFault: Boolean? = null,
    val lastTranslatedPhysical: String = "--",
    val stepExplanation: String = "3 Physical Frames (4 KB pages). Tap 'Next Page Access' to trace VPN -> PFN translation."
)

object PagingSimulatorEngine {

    fun reset(policy: PagePolicy, sequence: List<Int>): PagingSimState = PagingSimState(
        policy = policy,
        pageSequence = sequence
    )

    fun step(state: PagingSimState): PagingSimState {
        if (state.pointer >= state.pageSequence.size) return state
        val stepNum = state.pointer + 1
        val vpn = state.pageSequence[state.pointer]
        val offsetHex = ((vpn * 0x137) and 0xFFF).toString(16).uppercase().padStart(3, '0')
        val frames = state.frames.toMutableList()
        val hitFrame = frames.firstOrNull { it.loadedPage == vpn }

        return if (hitFrame != null) {
            frames[hitFrame.frameIndex] = hitFrame.copy(lastUsedStep = stepNum)
            val phys = "Frame ${hitFrame.frameIndex} + 0x$offsetHex"
            state.copy(
                frames = frames,
                pointer = state.pointer + 1,
                hits = state.hits + 1,
                lastIsFault = false,
                lastTranslatedPhysical = phys,
                stepExplanation = "Step #$stepNum (VPN $vpn, Offset 0x$offsetHex): PAGE HIT in Physical Frame ${hitFrame.frameIndex} -> Physical Addr = $phys."
            )
        } else {
            val freeFrame = frames.firstOrNull { it.loadedPage == null }
            val victim = freeFrame ?: when (state.policy) {
                PagePolicy.FIFO -> frames.minByOrNull { it.loadedAtStep }!!
                PagePolicy.LRU -> frames.minByOrNull { it.lastUsedStep }!!
            }
            val oldPage = victim.loadedPage
            frames[victim.frameIndex] = victim.copy(
                loadedPage = vpn,
                lastUsedStep = stepNum,
                loadedAtStep = stepNum
            )
            val phys = "Frame ${victim.frameIndex} + 0x$offsetHex"
            val replaceText = if (oldPage != null) "Evicted Page $oldPage (${state.policy.name})" else "Allocated free Frame ${victim.frameIndex}"
            state.copy(
                frames = frames,
                pointer = state.pointer + 1,
                faults = state.faults + 1,
                lastIsFault = true,
                lastTranslatedPhysical = phys,
                stepExplanation = "Step #$stepNum (VPN $vpn): PAGE FAULT! OS loads Page $vpn from disk -> $replaceText -> Physical Addr = $phys."
            )
        }
    }
}

enum class IoTransferMode(val title: String, val cpuEfficiency: String) {
    PROGRAMMED("Programmed I/O (Polling)", "Low — CPU stuck in busy-wait loop"),
    INTERRUPT("Interrupt-Driven I/O", "Medium — CPU works until IRQ per byte/word"),
    DMA("Direct Memory Access (DMA)", "High — DMA transfers full block directly to RAM")
}

data class IoTimelineStep(
    val phaseTitle: String,
    val cpuState: String,
    val busOwner: String,
    val detail: String
)

object IoTransferEngine {
    fun stepsFor(mode: IoTransferMode): List<IoTimelineStep> = when (mode) {
        IoTransferMode.PROGRAMMED -> listOf(
            IoTimelineStep("1. Issue Read Command", "Busy (Command)", "CPU", "CPU writes read command to I/O Device Control Register."),
            IoTimelineStep("2. Poll Status Flag", "Busy-Waiting (100%)", "CPU", "CPU repeatedly reads Status Register in a loop until READY=1, wasting cycles."),
            IoTimelineStep("3. CPU Reads Data", "Busy (Transfer)", "CPU", "CPU copies 1 word from I/O Data Register into CPU register, then stores to RAM."),
            IoTimelineStep("4. Repeat Per Word", "Blocked", "CPU", "Steps 2–3 repeat for every single byte/word until the block finishes.")
        )
        IoTransferMode.INTERRUPT -> listOf(
            IoTimelineStep("1. Issue Read & Context Switch", "Running User Task", "CPU", "CPU starts I/O device and returns to running other user threads immediately."),
            IoTimelineStep("2. Device Asserts IRQ", "Interrupted", "CPU + IRQ", "When data is ready, I/O Controller raises Interrupt Request (IRQ) line."),
            IoTimelineStep("3. Save Context & Run ISR", "Running ISR", "CPU", "CPU pushes PC/Flags, jumps to Interrupt Service Routine via Vector Table, transfers word."),
            IoTimelineStep("4. Restore & Resume", "Running User Task", "CPU", "CPU executes IRET to resume original program; repeats once per word/chunk.")
        )
        IoTransferMode.DMA -> listOf(
            IoTimelineStep("1. Program DMA Controller", "Configuring DMA", "CPU", "CPU writes Start RAM Address, Byte Count, and Direction to DMA Controller, then continues working."),
            IoTimelineStep("2. Bus Request (HOLD / HLDA)", "Free (Parallel Work)", "DMA Controller", "DMA asserts Bus Request; CPU grants bus mastership (HLDA) or yields via cycle stealing."),
            IoTimelineStep("3. Direct Block Transfer", "Running User Task", "DMA -> RAM", "DMA moves the entire block directly between I/O Device and RAM with zero CPU intervention."),
            IoTimelineStep("4. Single Completion IRQ", "Handles 1 Done IRQ", "CPU", "Only after all bytes finish transferring does DMA send one Interrupt to notify the CPU.")
        )
    }
}
