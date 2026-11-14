# DECO Lab — Project Report

## 1. Problem Statement
Undergraduate engineering students studying **Digital Electronics & Computer Organization (DECO)** often struggle to bridge abstract mathematical representations (radix arithmetic, Boolean minimization, state equations, micro-operations, and cache indexing) with live hardware behavior. Static textbook figures do not let learners observe how Karnaugh maps group adjacent minterms, how signals propagate through combinational logic gates, how clock edges drive flip-flop timing diagrams, how an 8-bit CPU steps through Fetch-Decode-Execute cycles, or why direct-mapped caches thrash under strided memory accesses.

## 2. Objectives
- Build an offline-ready, mobile-friendly Android learning and simulation tool (**DECO Lab**) covering all 5 core DECO units.
- Pair concise, formula-backed **Theory Summary Cards** with interactive real-time simulators.
- Provide **step-by-step explanations** for every conversion, simplification, gate evaluation, clock edge, CPU phase, and cache/page lookup.
- Include a persistent **25-question Quiz Mode** (5 MCQs per unit) with instant diagnostic feedback and a unit-by-unit Score Tracker.
- Support **Oscilloscope Dark Mode** and **Blueprint Light Mode** with responsive layouts for phones and tablets.

## 3. Curriculum Unit Mapping

| Unit | Syllabus Topic | In-App Modules & Interactive Features |
| :--- | :--- | :--- |
| **Unit 1** | Number Systems & Boolean Algebra | Radix Converter (Bin, Oct, Dec, Hex, 8-bit 2's Complement), Boolean Expression Truth Table Generator (SOP/POS), and 2–4 Variable Gray-Code K-Map Simplifier with color-coded prime implicant groups. |
| **Unit 2** | Combinational Circuits | Drag-and-drop Logic Gate Canvas (AND, OR, NOT, NAND, NOR, XOR, XNOR, IN, LED) with pin-to-pin wiring and live signal highlighting, plus Half/Full Adders, 4:1 MUX, 2:4 Decoder, and 4:2 Priority Encoder ICs. |
| **Unit 3** | Sequential Circuits | SR, JK, D, and T Flip-Flop Simulator with real-time Canvas Digital Timing Diagrams, 4-bit Up/Down Binary/BCD Counter, and 4-bit Shift Register (SISO, SIPO, Ring, Johnson). |
| **Unit 4** | Computer Organization & CPU | 8-Bit CPU Simulator (`R0–R3`, `PC`, `IR`, `MAR`, `MDR`, `ACC`, Flags `Z/C/N`, 32-byte RAM), animated Datapath highlighting, Tiny Assembly Editor with presets, and phase-by-phase Fetch-Decode-Execute stepping. |
| **Unit 5** | Memory & I/O | Cache Mapping Visualizer (Direct, Fully Associative, 2-Way Set Associative) with Hit/Miss ratio gauge, Virtual Memory Paging (FIFO/LRU), and Programmed vs Interrupt vs DMA I/O timeline. |

## 4. Architecture & Implementation
- **Platform**: Android (Kotlin, Jetpack Compose, Material Design 3).
- **Architecture**: MVVM (`MainViewModel` + pure-Kotlin deterministic simulation engines `Unit1Engine`–`Unit5Engine` + `PreferencesRepository`).
- **Persistence**: Jetpack DataStore Preferences saves Dark/Light theme selection and per-unit Quiz high scores across launches.
- **Typography & Design**: Bundled `Space Grotesk`, `Inter`, and `JetBrains Mono` fonts for crisp numerical/assembly alignment, custom adaptive launcher icon, and Edge-to-Edge adaptive `NavigationBar` / `NavigationRail`.

## 5. Conclusion
DECO Lab turns all five units of Digital Electronics & Computer Organization into an interactive pocket engineering workbench. By visualizing intermediate hardware states alongside step-by-step explanations and unit quizzes, students gain practical intuition that reinforces classroom theory.
