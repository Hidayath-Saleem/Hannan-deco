# DECO Lab — 5-Minute Demo Script

**Total Duration:** 5 Minutes (300 seconds)  
**Setup:** Launch **DECO Lab** on the Lab Hub screen in Dark Mode.

---

### [0:00 – 0:45] Introduction, Theme Toggle & Lab Hub
- **Say:** "Hello everyone! This is **DECO Lab**, an interactive Digital Electronics & Computer Organization learning and simulation app built for engineering students. It unifies all 5 syllabus units, step-by-step explanations, and a 25-question quiz suite in one mobile-first workbench."
- **Do:**
  1. Show the **Lab Hub** hero banner and the **Quiz Mastery Tracker** (`0/25` or current score).
  2. Tap the **Light/Dark Theme Toggle** in the top-right bar to show Blueprint Light Mode and switch back to Oscilloscope Dark Mode.
  3. Scroll briefly past the 5 Unit cards, then tap **Open Lab** on **Unit 1**.

---

### [0:45 – 1:40] Unit 1: Number Systems, Truth Tables & K-Map Simplifier
- **Say:** "Each unit opens with a collapsible Theory Brief containing key formulas. In Unit 1's Converter, let's inspect decimal `-25`."
- **Do:**
  1. Show `-25` converting to Binary, Octal, Hex, and **8-bit 2's Complement (`11100111`)** with the 4-step bit-inversion trace below.
  2. Tap the **Truth Table** tab to show live parsing of `A & B + !C` with Canonical SOP and POS.
  3. Tap **K-Map (2–4)**, show the 4-variable Gray-code grid (`00, 01, 11, 10`), tap a cell to toggle `0 -> 1 -> X`, and highlight the color-coded Prime Implicant groups and minimized SOP expression (`F = ...`).

---

### [1:40 – 2:30] Unit 2: Drag-and-Drop Logic Gates & Combinational ICs
- **Say:** "Moving to Unit 2—Combinational Circuits—students can build circuits visually on our drag-and-drop gate sandbox or inspect standard ICs."
- **Do:**
  1. Tap `U2: Gates & ICs` at the top strip.
  2. With the **Half Adder** preset loaded, drag the `XOR` gate slightly across the blueprint grid, then tap input `A` or `B` to toggle `1/0` and show the glowing cyan wires updating `SUM` and `CARRY`.
  3. Switch to **Combinational ICs**, select **Full Adder** or **4:1 Multiplexer**, toggle select lines `S1/S0`, and point out the live output chips and logic walkthrough.

---

### [2:30 – 3:15] Unit 3: Flip-Flops, Timing Diagrams & Shift Registers
- **Say:** "Unit 3 adds time and memory with clocked sequential circuits and live digital timing waveforms."
- **Do:**
  1. Tap `U3: Flip-Flops`. Select **JK Flip-Flop**, ensure `J=1, K=1`, and tap **CLK ↑** 3–4 times.
  2. Point out the Canvas **Digital Timing Diagram** plotting `CLK`, `J`, `K`, and toggling `Q` square waves in real time.
  3. Switch to **Counters & Shift Reg**, tap **Shift CLK ↑** on the 4-bit Shift Register to show bits shifting across `Q3..Q0`.

---

### [3:15 – 4:05] Unit 4: 8-Bit CPU & Fetch-Decode-Execute Cycle
- **Say:** "Unit 4 brings Computer Organization to life with a programmable 8-bit CPU and tiny assembly editor."
- **Do:**
  1. Tap `U4: 8-Bit CPU`. Select the **Countdown Loop** or **Add Two Numbers** preset.
  2. Tap **Step Phase** three times to step visibly through **FETCH** (`MAR <- PC`, `IR <- MDR`), **DECODE** (`CU`), and **EXECUTE** (`ALU / REG`), pointing out the highlighted datapath blocks and micro-operation explanation card.
  3. Tap **Step Instr** until `HLT` and show the final sum written into the 32-byte RAM hex dump (`0x25` = `37`).

---

### [4:05 – 5:00] Unit 5: Cache Mapping, Virtual Paging, DMA & Quiz Mode
- **Say:** "Finally, Unit 5 visualizes Memory Hierarchy and I/O. Watch what happens when we run a strided sequence `0, 4, 0, 8` in Direct-Mapped vs 2-Way Set Associative cache."
- **Do:**
  1. Tap `U5: Cache & I/O`, tap **Run All** in **Direct Mapped** mode to show conflict misses, then switch to **2-Way Set Assoc** and tap **Run All** to show the Hit Ratio improving.
  2. Briefly tap **Virtual Paging** and **Interrupts & DMA** tabs to highlight page fault tracking and DMA cycle stealing.
  3. Tap **Quiz** in the bottom navigation bar, answer one MCQ to show instant feedback and explanation, and conclude on the **Score Tracker** (and mention the built-in **Report** tab).
