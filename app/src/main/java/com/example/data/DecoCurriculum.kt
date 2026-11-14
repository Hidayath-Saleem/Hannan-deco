package com.example.data

data class DecoUnitInfo(
    val id: Int,
    val code: String,
    val title: String,
    val shortTitle: String,
    val subtitle: String,
    val theorySummary: String,
    val keyFormulas: List<String>,
    val toolsIncluded: List<String>
)

data class QuizQuestion(
    val id: String,
    val unitId: Int,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class ReportSection(
    val title: String,
    val content: String
)

data class DemoSegment(
    val timeRange: String,
    val title: String,
    val speakerNotes: String,
    val appActions: String
)

object DecoCurriculum {

    val units: List<DecoUnitInfo> = listOf(
        DecoUnitInfo(
            id = 1,
            code = "UNIT 01",
            title = "Number Systems & Boolean Algebra",
            shortTitle = "Logic & K-Map",
            subtitle = "Radix conversions, 2's complement, truth tables & 4-variable K-Maps",
            theorySummary = "Digital systems represent all data in base-2 (binary), compactly written in octal (base-8) or hexadecimal (base-16). Negative integers use 2's complement (invert bits + 1) so addition and subtraction share one adder circuit. Boolean algebra laws (De Morgan's, absorption, distribution) and Gray-code Karnaugh Maps eliminate redundant literals into minimal Sum-of-Products (SOP) gates.",
            keyFormulas = listOf(
                "2's Comp(-N) = ~N + 1 (8-bit range: -128 to +127)",
                "De Morgan: (A·B)' = A' + B'  |  (A+B)' = A'·B'",
                "K-Map Grouping: Power-of-2 adjacent cells (1, 2, 4, 8, 16) eliminate log₂(k) literals"
            ),
            toolsIncluded = listOf("Base & 2's Comp Converter", "Boolean Truth Table", "2–4 Var K-Map Simplifier")
        ),
        DecoUnitInfo(
            id = 2,
            code = "UNIT 02",
            title = "Combinational Circuits",
            shortTitle = "Gates & ICs",
            subtitle = "Logic gate sandbox, half/full adders, multiplexers, decoders & encoders",
            theorySummary = "Combinational logic outputs depend strictly on present inputs—no clock or memory is involved. Basic gates (AND, OR, NOT) and universal gates (NAND, NOR) compose arithmetic blocks like Half/Full Adders, data selectors (Multiplexers), n-to-2ⁿ Decoders for memory addressing, and Priority Encoders that resolve simultaneous requests.",
            keyFormulas = listOf(
                "Half Adder: Sum = A ⊕ B,  Carry = A · B",
                "Full Adder: Sum = A ⊕ B ⊕ Cin,  Cout = A·B + Cin·(A ⊕ B)",
                "4:1 MUX: Y = S1'S0'D0 + S1'S0D1 + S1S0'D2 + S1S0D3"
            ),
            toolsIncluded = listOf("Drag-and-Drop Gate Canvas", "Half & Full Adder", "4:1 MUX, 2:4 Decoder & Encoder")
        ),
        DecoUnitInfo(
            id = 3,
            code = "UNIT 03",
            title = "Sequential Circuits",
            shortTitle = "Flip-Flops",
            subtitle = "SR, JK, D, T flip-flops with timing diagrams, counters & shift registers",
            theorySummary = "Sequential circuits add feedback and a synchronous Clock (CLK) so outputs depend on both current inputs and stored state Q. Flip-flops capture 1 bit on a rising clock edge (CLK ↑). Cascading flip-flops forms binary Counters (frequency division & timing) and Shift Registers (serial-parallel data conversion & ring sequencing).",
            keyFormulas = listOf(
                "SR: Q+ = S + R'Q (S=R=1 invalid) | JK: Q+ = JQ' + K'Q (J=K=1 toggles)",
                "D: Q+ = D (Data/Delay) | T: Q+ = T ⊕ Q (Toggle when T=1)",
                "Mod-N Counter requires ⌈log₂(N)⌉ flip-flops"
            ),
            toolsIncluded = listOf("SR/JK/D/T Simulator", "Live Digital Timing Diagram", "4-Bit Counter & Shift Register")
        ),
        DecoUnitInfo(
            id = 4,
            code = "UNIT 04",
            title = "Computer Organization & CPU",
            shortTitle = "8-Bit CPU",
            subtitle = "Registers, ALU, Fetch-Decode-Execute cycle & tiny assembly workbench",
            theorySummary = "A stored-program von Neumann CPU fetches instructions from memory pointed to by the Program Counter (PC) into the Instruction Register (IR), decodes the opcode in the Control Unit, and executes arithmetic/logic operations via the ALU and general-purpose registers. Status flags (Z, C, N) enable conditional branching.",
            keyFormulas = listOf(
                "FETCH: MAR ← PC; MDR ← MEM[MAR]; IR ← MDR; PC ← PC + 1",
                "DECODE: CU decodes IR opcode & selects operand registers/address",
                "EXECUTE: ALU computes result → Rd, updates Z/C/N flags"
            ),
            toolsIncluded = listOf("8-Bit CPU Datapath", "Step-by-Step FDE Tracer", "Tiny Assembly Editor")
        ),
        DecoUnitInfo(
            id = 5,
            code = "UNIT 05",
            title = "Memory & I/O",
            shortTitle = "Cache & I/O",
            subtitle = "Cache mapping, hit/miss ratio, virtual paging, interrupts & DMA",
            theorySummary = "Memory hierarchies bridge the CPU-DRAM speed gap using fast SRAM caches exploited through spatial and temporal locality. Virtual memory maps logical pages to physical frames via Page Tables, avoiding external fragmentation. High-speed peripherals bypass CPU polling using vectored Interrupts and Direct Memory Access (DMA) cycle stealing.",
            keyFormulas = listOf(
                "Direct Map Line = (Block Addr) mod (Num Lines)",
                "AMAT = Hit Time + (Miss Rate × Miss Penalty)",
                "Physical Addr = (Frame Number × Page Size) + Offset"
            ),
            toolsIncluded = listOf("Direct/Assoc/Set-Assoc Cache", "Virtual Memory Paging", "Interrupt vs DMA Timeline")
        )
    )

    val quizzes: List<QuizQuestion> = listOf(
        // Unit 1
        QuizQuestion(
            id = "u1_q1",
            unitId = 1,
            question = "What is the 8-bit 2's complement representation of decimal -25?",
            options = listOf("11100111", "10011001", "11100110", "00011001"),
            correctIndex = 0,
            explanation = "+25 in 8-bit binary is 00011001. Invert all bits (1's complement) to get 11100110, then add 1 -> 11100111."
        ),
        QuizQuestion(
            id = "u1_q2",
            unitId = 1,
            question = "Convert hexadecimal 0x3F to octal.",
            options = listOf("77", "63", "75", "177"),
            correctIndex = 0,
            explanation = "0x3F = 0011 1111 in binary. Grouping in 3s from right: 000 111 111 -> 0 7 7, which is 77 in octal (63 decimal)."
        ),
        QuizQuestion(
            id = "u1_q3",
            unitId = 1,
            question = "In a 4-variable K-map, grouping 8 adjacent 1-cells eliminates how many literals from the product term?",
            options = listOf("1 literal", "2 literals", "3 literals", "4 literals"),
            correctIndex = 2,
            explanation = "A group of 2^k cells eliminates k variables. Since 8 = 2^3, an octet eliminates 3 variables, leaving a single-literal term."
        ),
        QuizQuestion(
            id = "u1_q4",
            unitId = 1,
            question = "According to De Morgan's theorem, what is (A + B·C)' equivalent to?",
            options = listOf("A' · (B' + C')", "A' + B' · C'", "A' · B' · C'", "(A' + B') · C'"),
            correctIndex = 0,
            explanation = "First apply De Morgan over OR: (A + B·C)' = A' · (B·C)'. Then apply De Morgan over AND: (B·C)' = B' + C', yielding A' · (B' + C')."
        ),
        QuizQuestion(
            id = "u1_q5",
            unitId = 1,
            question = "Why are Karnaugh Map rows and columns ordered 00, 01, 11, 10 instead of standard binary order?",
            options = listOf(
                "To maximize Hamming distance between rows",
                "Gray code ensures adjacent cells differ by exactly 1 bit",
                "To avoid wrap-around grouping",
                "To make decimal conversion faster"
            ),
            correctIndex = 1,
            explanation = "Gray code guarantees that any two physically adjacent cells (including wrap-around edges) differ in only one variable, allowing algebraic cancellation X + X' = 1."
        ),

        // Unit 2
        QuizQuestion(
            id = "u2_q1",
            unitId = 2,
            question = "Which pair of gates are individually called Universal Gates because any Boolean function can be built using only one type?",
            options = listOf("AND and OR", "XOR and XNOR", "NAND and NOR", "NOT and XOR"),
            correctIndex = 2,
            explanation = "Both NAND and NOR are functionally complete (universal): NOT, AND, and OR can all be constructed exclusively from either NAND or NOR gates."
        ),
        QuizQuestion(
            id = "u2_q2",
            unitId = 2,
            question = "What is the Carry-out (Cout) expression of a Full Adder with inputs A, B, and Cin?",
            options = listOf(
                "A·B·Cin",
                "A·B + Cin·(A ⊕ B)",
                "A ⊕ B ⊕ Cin",
                "A + B + Cin"
            ),
            correctIndex = 1,
            explanation = "A Full Adder generates a carry when both A and B are 1 (A·B), or when one of them is 1 and Cin is 1 (Cin·(A ⊕ B))."
        ),
        QuizQuestion(
            id = "u2_q3",
            unitId = 2,
            question = "How many select lines are required for a 16:1 Multiplexer?",
            options = listOf("2", "3", "4", "8"),
            correctIndex = 2,
            explanation = "An N:1 MUX requires log2(N) select lines. For N = 16, log2(16) = 4 select lines (S3..S0)."
        ),
        QuizQuestion(
            id = "u2_q4",
            unitId = 2,
            question = "With Enable=1 and inputs A1=1, A0=0, which active-HIGH output of a 2:4 Decoder is asserted?",
            options = listOf("Y0", "Y1", "Y2", "Y3"),
            correctIndex = 2,
            explanation = "Binary input 10 equals decimal 2, so output Y2 goes HIGH (1) while Y0, Y1, and Y3 remain LOW (0)."
        ),
        QuizQuestion(
            id = "u2_q5",
            unitId = 2,
            question = "In a 4:2 Priority Encoder (D3 highest priority), if D3=0, D2=1, D1=1, D0=0, what is the output Y1Y0?",
            options = listOf("01", "10", "11", "00"),
            correctIndex = 1,
            explanation = "A priority encoder outputs the index of the highest-priority active input. Between D2 and D1, D2 has higher priority, so output is binary 10 (decimal 2)."
        ),

        // Unit 3
        QuizQuestion(
            id = "u3_q1",
            unitId = 3,
            question = "Which input combination causes an indeterminate/forbidden state in a standard clocked SR flip-flop?",
            options = listOf("S=0, R=0", "S=0, R=1", "S=1, R=0", "S=1, R=1"),
            correctIndex = 3,
            explanation = "S=1 and R=1 tries to simultaneously Set and Reset the flip-flop, creating a race condition where Q and Q' violate complementarity."
        ),
        QuizQuestion(
            id = "u3_q2",
            unitId = 3,
            question = "How does a JK flip-flop resolve the S=1, R=1 condition when J=1 and K=1 on a clock edge?",
            options = listOf(
                "It resets Q to 0",
                "It holds the previous state",
                "It toggles Q to Q'",
                "It enters a high-impedance state"
            ),
            correctIndex = 2,
            explanation = "The JK flip-flop feeds Q and Q' back to the input gates so J=1, K=1 cleanly toggles the output (Q_next = Q')."
        ),
        QuizQuestion(
            id = "u3_q3",
            unitId = 3,
            question = "How many flip-flops are minimally required to build a Mod-10 (BCD) decade counter?",
            options = listOf("3", "4", "5", "10"),
            correctIndex = 1,
            explanation = "With n flip-flops, up to 2^n states can be represented. 2^3 = 8 < 10, while 2^4 = 16 >= 10, so 4 flip-flops are required."
        ),
        QuizQuestion(
            id = "u3_q4",
            unitId = 3,
            question = "If a 4-bit SISO shift register initially holds 1010 and shifts right once with Serial-In = 1, what is its new state (MSB to LSB)?",
            options = listOf("1101", "0101", "1011", "0100"),
            correctIndex = 0,
            explanation = "Shifting right moves bits toward LSB (dropping the rightmost 0) and inserts Serial-In (1) at the MSB: 1010 -> 1101."
        ),
        QuizQuestion(
            id = "u3_q5",
            unitId = 3,
            question = "What is the key difference between a latch and a flip-flop?",
            options = listOf(
                "Latches store 2 bits; flip-flops store 1 bit",
                "Latches are level-triggered; flip-flops are edge-triggered",
                "Flip-flops cannot use clocks",
                "Latches have no feedback paths"
            ),
            correctIndex = 1,
            explanation = "A latch is transparent whenever its enable/clock level is active, whereas a flip-flop samples inputs only on a rising or falling clock edge."
        ),

        // Unit 4
        QuizQuestion(
            id = "u4_q1",
            unitId = 4,
            question = "During the FETCH phase of the instruction cycle, which register holds the memory address of the next instruction?",
            options = listOf("Instruction Register (IR)", "Accumulator (ACC)", "Program Counter (PC)", "Stack Pointer (SP)"),
            correctIndex = 2,
            explanation = "The Program Counter (PC) points to the next instruction address, transfers it to MAR to read memory, and then increments."
        ),
        QuizQuestion(
            id = "u4_q2",
            unitId = 4,
            question = "Which CPU component decodes the opcode stored in the Instruction Register (IR) and generates timing/control signals?",
            options = listOf("Arithmetic Logic Unit", "Control Unit (CU)", "Memory Data Register", "Status Flag Register"),
            correctIndex = 1,
            explanation = "The Control Unit interprets the opcode bits in IR during the DECODE phase and orchestrates bus, register, and ALU control lines."
        ),
        QuizQuestion(
            id = "u4_q3",
            unitId = 4,
            question = "After executing SUB R0, R1 where R0 = 5 and R1 = 5, which status flag is guaranteed to be set to 1?",
            options = listOf("Zero Flag (Z)", "Negative Flag (N)", "Overflow Flag (V)", "Interrupt Flag (I)"),
            correctIndex = 0,
            explanation = "5 - 5 = 0. Whenever an ALU operation yields zero, the Zero Flag (Z) is set to 1, allowing instructions like JZ to branch."
        ),
        QuizQuestion(
            id = "u4_q4",
            unitId = 4,
            question = "What does the register transfer notation MAR ← PC represent?",
            options = listOf(
                "Memory contents are copied into PC",
                "The address in PC is loaded into the Memory Address Register",
                "PC is cleared to zero",
                "Instruction Register is written to memory"
            ),
            correctIndex = 1,
            explanation = "In RTL, the arrow points to the destination. MAR ← PC copies the Program Counter address into the Memory Address Register before a memory read."
        ),
        QuizQuestion(
            id = "u4_q5",
            unitId = 4,
            question = "Which architecture uses separate memory buses for instructions and data, allowing simultaneous instruction fetch and data access?",
            options = listOf("Von Neumann architecture", "Harvard architecture", "Accumulator-only architecture", "SISD stack machine"),
            correctIndex = 1,
            explanation = "Harvard architecture separates instruction and data memories/buses to avoid the single-bus von Neumann bottleneck."
        ),

        // Unit 5
        QuizQuestion(
            id = "u5_q1",
            unitId = 5,
            question = "In a 4-line Direct-Mapped cache (1 word/block), blocks 0, 4, 8, and 0 are accessed in sequence. Why do misses keep occurring?",
            options = listOf(
                "Compulsory cold-start misses only",
                "Capacity miss due to block size",
                "Conflict misses because all three addresses map to Line 0 (addr mod 4 = 0)",
                "TLB shootdown invalidations"
            ),
            correctIndex = 2,
            explanation = "0 mod 4 = 0, 4 mod 4 = 0, and 8 mod 4 = 0. All three contend for the exact same cache line, causing conflict thrashing despite empty lines 1–3."
        ),
        QuizQuestion(
            id = "u5_q2",
            unitId = 5,
            question = "If a cache has 45 hits and 5 misses, what is the Hit Ratio?",
            options = listOf("80%", "85%", "90%", "95%"),
            correctIndex = 2,
            explanation = "Total accesses = 45 + 5 = 50. Hit Ratio = 45 / 50 = 0.90 = 90%."
        ),
        QuizQuestion(
            id = "u5_q3",
            unitId = 5,
            question = "What hardware structure translates a Virtual Page Number (VPN) into a Physical Frame Number (PFN) in virtual memory?",
            options = listOf("Program Counter", "Page Table / TLB", "Interrupt Vector Table", "Instruction Decoder"),
            correctIndex = 1,
            explanation = "The Page Table (accelerated by the Translation Lookaside Buffer, TLB) maps virtual page numbers to physical RAM frames while preserving the page offset."
        ),
        QuizQuestion(
            id = "u5_q4",
            unitId = 5,
            question = "Which I/O mechanism transfers large blocks directly between an I/O controller and RAM with minimal CPU involvement?",
            options = listOf("Programmed I/O (Polling)", "Memory-mapped busy wait", "Direct Memory Access (DMA)", "Software trap polling"),
            correctIndex = 2,
            explanation = "A DMA controller takes control of the system bus (via cycle stealing or burst mode) to move data directly between I/O devices and RAM, interrupting the CPU only at completion."
        ),
        QuizQuestion(
            id = "u5_q5",
            unitId = 5,
            question = "When a Page Fault occurs in virtual memory, what happens?",
            options = listOf(
                "The CPU immediately halts with a parity error",
                "The OS fetches the missing page from secondary storage into a physical frame",
                "The cache line is flushed to ALU",
                "The page offset bits are inverted"
            ),
            correctIndex = 1,
            explanation = "A page fault traps to the OS, which selects a free or victim frame (via FIFO/LRU), loads the page from disk, updates the page table valid bit, and restarts the instruction."
        )
    )

    val projectReportSections: List<ReportSection> = listOf(
        ReportSection(
            title = "1. Problem Statement",
            content = "Undergraduate engineering students studying Digital Electronics & Computer Organization (DECO) often struggle to connect abstract mathematical theory (Boolean minimization, state transitions, micro-operations, and cache indexing) with dynamic hardware behavior. Static textbook diagrams cannot show how a Karnaugh map groups prime implicants, how signals propagate across custom logic gates, how clock edges drive flip-flop timing waveforms, how an 8-bit CPU executes Fetch-Decode-Execute cycles, or why direct-mapped caches thrash under strided memory access."
        ),
        ReportSection(
            title = "2. Objectives",
            content = "• Unify all 5 core DECO syllabus units inside one offline-ready, mobile-first Android application.\n" +
                "• Pair concise theory summaries with real-time interactive simulators and step-by-step explanations for every state change.\n" +
                "• Provide hands-on visual instrumentation: colored K-map implicant overlays, drag-and-drop gate wiring, Canvas digital timing diagrams, an 8-bit CPU assembly workbench, and cache/paging visualizers.\n" +
                "• Reinforce conceptual mastery through a persistent 25-question quiz suite (5 MCQs per unit) with instant diagnostic explanations."
        ),
        ReportSection(
            title = "3. Curriculum Unit Mapping",
            content = "• Unit 1 (Number Systems & Boolean Algebra): Radix converter (Bin/Oct/Dec/Hex/8-bit 2's Complement), recursive-descent Truth Table generator with SOP/POS forms, and 2–4 variable Quine-McCluskey K-Map simplifier.\n" +
                "• Unit 2 (Combinational Circuits): Drag-and-drop gate canvas (AND/OR/NOT/NAND/NOR/XOR/XNOR) with live pin wiring plus interactive Half/Full Adders, 4:1 MUX, 2:4 Decoder, and 4:2 Priority Encoder.\n" +
                "• Unit 3 (Sequential Circuits): SR, JK, D, T flip-flop simulator with live Canvas timing waveforms, 4-bit Up/Down Counter, and 4-bit SISO/SIPO/PISO/Ring Shift Register.\n" +
                "• Unit 4 (Computer Organization & CPU): 8-bit von Neumann CPU (R0–R3, PC, IR, MAR, MDR, ACC, Z/C/N flags, 32-byte RAM), tiny assembler, and animated Fetch-Decode-Execute datapath.\n" +
                "• Unit 5 (Memory & I/O): Direct, Fully Associative, and 2-Way Set Associative cache simulator with Hit/Miss ratio, Virtual Memory Paging (FIFO/LRU), and Programmed/Interrupt/DMA I/O timeline."
        ),
        ReportSection(
            title = "4. System Architecture & UI Design",
            content = "Built in Kotlin with Jetpack Compose & Material 3 using MVVM architecture. Deterministic pure-Kotlin simulation engines reside in the engine layer (`Unit1Engine`–`Unit5Engine`), cleanly decoupled from reactive Compose screens. Persistent user preferences (Oscilloscope Dark / Blueprint Light theme and per-unit high scores) are stored locally via Jetpack DataStore. Custom geometric typography (`Space Grotesk`, `Inter`, `JetBrains Mono`) ensures crisp readability of binary, hex, and assembly listings across compact phones and tablets."
        ),
        ReportSection(
            title = "5. Conclusion",
            content = "DECO Lab transforms passive lecture topics into an interactive pocket digital workbench. By exposing every intermediate step—from 2's complement bit inversion and K-map prime implicant selection to CPU micro-operations and cache conflict misses—students build durable hardware intuition validated by unit-level quizzes."
        )
    )

    val demoScriptSegments: List<DemoSegment> = listOf(
        DemoSegment(
            timeRange = "0:00 – 0:45",
            title = "Introduction, Theme & Lab Hub",
            speakerNotes = "Introduce DECO Lab as an all-in-one Android learning and simulation workbench covering all 5 units of Digital Electronics & Computer Organization. Highlight the clean Oscilloscope Dark / Blueprint Light toggle and the overall 25-point Quiz Mastery tracker on the Lab Hub.",
            appActions = "Launch DECO Lab on the Lab Hub -> Toggle Light/Dark mode in the Top Bar -> Scroll through the 5 Unit cards showing theory previews and quiz badges."
        ),
        DemoSegment(
            timeRange = "0:45 – 1:40",
            title = "Unit 1: Converter, Truth Table & K-Map Simplifier",
            speakerNotes = "Demonstrate how a negative decimal like -25 converts to 8-bit 2's complement with step-by-step bit inversion. Switch to K-Map Simplifier, tap cells to form an octet/quad with wrap-around, and show the color-coded prime implicant groups and minimized SOP expression.",
            appActions = "Open Unit 1 -> Enter -25 in Decimal -> Switch to Truth Table -> Switch to 4-Var K-Map and tap cells or load preset."
        ),
        DemoSegment(
            timeRange = "1:40 – 2:30",
            title = "Unit 2: Drag-and-Drop Gates & Combinational ICs",
            speakerNotes = "Show the interactive gate canvas: drag gates on the grid, wire an input switch to a gate, and toggle inputs to watch live cyan HIGH / slate LOW propagation. Switch to Combinational ICs to demonstrate a Full Adder carry propagation and 4:1 MUX selection.",
            appActions = "Open Unit 2 -> Load Half Adder preset on canvas -> Drag a gate & toggle IN_A/IN_B -> Switch to ICs tab and test 4:1 MUX."
        ),
        DemoSegment(
            timeRange = "2:30 – 3:15",
            title = "Unit 3: Flip-Flops, Timing Diagrams & Shift Registers",
            speakerNotes = "Select the JK Flip-Flop, set J=1, K=1, and fire rising clock edges (CLK ↑) to watch Q toggle while the Canvas oscilloscope draws real-time square-wave timing diagrams. Briefly demonstrate the 4-bit Shift Register shifting bits across LED stages.",
            appActions = "Open Unit 3 -> Choose JK -> Set J=1, K=1 -> Tap Clock Pulse 4 times -> Switch to Counters & Shift Registers tab."
        ),
        DemoSegment(
            timeRange = "3:15 – 4:05",
            title = "Unit 4: 8-Bit CPU & Fetch-Decode-Execute Cycle",
            speakerNotes = "Load the Countdown Loop or Add Two Numbers assembly program. Step phase-by-phase through FETCH, DECODE, and EXECUTE so the audience sees PC -> MAR -> MDR -> IR transfers, ALU computation, Z-flag updates, and RAM writes in real time.",
            appActions = "Open Unit 4 -> Load 'Countdown Loop' -> Tap 'Step Phase' repeatedly -> Show highlighted Datapath blocks and micro-op log."
        ),
        DemoSegment(
            timeRange = "4:05 – 5:00",
            title = "Unit 5: Cache Mapping, Paging, DMA & Quiz Mode",
            speakerNotes = "Run the strided address sequence (0, 4, 0, 8) in Direct Mapped mode to show conflict thrashing (0% hits), then switch to 2-Way Set Associative to watch hits recover. Finish by answering a question in Quiz Mode and showing the persistent score tracker.",
            appActions = "Open Unit 5 -> Step through address trace in Direct vs 2-Way -> Glance at Paging & DMA -> Open Quiz tab, answer 1 question, show Score Tracker."
        )
    )
}
