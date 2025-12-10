# THE CORRIDOR
AN TEXT-BASED JAVA ADVENTURE GAME

Welcome to THE CORRIDOR — a tense, atmospheric text adventure written in Java. You awaken in a dim hallway with only your wits and a few salvaged items. Every choice matters: explore, fight, sneak, solve puzzles, or retreat. The corridor is alive; listen, watch, and survive.

---

## Quick start

Prerequisites
- Java 11+ installed (OpenJDK or Oracle JRE/JDK)

Run from source (simple / generic instructions — adjust package paths if your Main class differs):
1. Open a terminal in the repository root.
2. Compile:
   - If source classes are in the top-level `src` folder:
     - javac -d out src/**/*.java
   - Or use your IDE (IntelliJ, Eclipse) to build the project.
3. Run:
   - java -cp out Main
   - Or run your project's main class as appropriate.
4. If a JAR is provided or produced:
   - java -jar Corridor.jar

If you want, tell me where your `Main` class lives (path/package) and I will give exact commands or add a runnable Gradle/Maven/Ant script.

---

## How to play (commands)

Commands are entered by typing them and pressing Enter. Commands are case-insensitive.

Movement
- go north / go n
- go south / go s
- go east  / go e
- go west  / go w

Observation & interaction
- look          — examine current room
- look <object> — examine an object or feature
- inspect <object>
- listen        — get auditory hints
- examine <object>

Inventory & items
- inventory (or inv) — list carried items
- take <item>        — pick up item
- drop <item>
- use <item> [on <target>]
- equip <item>       — equip weapon or tool
- unequip <item>

Combat & stealth
- attack <target>
- strike <target>
- flee
- sneak
- hide

Game control
- save <name>
- load <name>
- help
- stats — show player health, stamina, sanity, etc.
- quit

Tip: Short forms are supported in many cases (e.g., "n" for "go north", "inv" for "inventory").

---

## Core game mechanics

This section explains the in-game systems — the rules that determine outcomes of your actions.

1. Player state
   - Health (HP): When HP reaches 0 you die. Regain via healing items or resting.
   - Stamina (SP): Actions such as running, attacking, and heavy tasks consume stamina. If stamina hits 0 you become exhausted and suffer penalties.
   - Sanity (SN): Exposure to dark sights, unnatural events, or prolonged isolation reduces sanity. Low sanity causes hallucinations (misleading room descriptions) and degraded combat performance.
   - Light: Many rooms are dark. A light source (torch, candle, lantern) is required to see details and avoid traps.

2. Movement & exploration
   - Each move costs a small amount of stamina.
   - Some rooms are locked or blocked and require keys, tools, or puzzle solutions.
   - Random events may trigger when entering a room (creaks, whispers, enemy spawns).
   - Thorough exploration (using `look`) can reveal hidden exits, traps, notes, or useful items.

3. Inventory & items
   - Limited inventory: you can carry only a fixed number of items (this can be increased via backpacks).
   - Items are typed: consumable (first-aid kit), equipment (knife), key (rusty key), utility (rope), quest (map fragment).
   - Combining or using items on targets can unlock puzzles (e.g., use key on door, light torch at altar).

4. Combat
   - Turn-based, simple actions: attack, defend, use item, flee.
   - Weapons have damage ranges and may consume stamina to use.
   - Armor reduces incoming damage and may cost stamina to wear/move in.
   - Critical hits and misses are possible (based on weapon, player stats, and randomness).
   - Enemies have behaviors: aggressive, patrol, stealthy, or stationary. Some react to sound or light.

5. Stealth & detection
   - Noise and light influence detection. Running and attacking make noise.
   - Sneaking reduces detection chance but consumes stamina.
   - If an enemy detects you, it may call reinforcements or flee to alert others.

6. Puzzles & environment
   - Puzzles are logic or exploration-based (lever sequences, pattern observation, wiring).
   - Environmental hazards include traps (pressure plates), collapsing floors, sealed doors, poison gas.
   - Some puzzles require combining items or reading notes found in the corridor.

7. Save / Load
   - Use `save <name>` to store your game state.
   - `load <name>` to restore it.
   - Important: save often! Some sections are unforgiving.

8. Consequences & endings
   - Multiple endings depend on choices, items recovered, areas explored, and sanity level.
   - Some endings unlock secrets for future playthroughs (New Game+ possibilities).

---

## Example gameplay transcript

You: look
Game: "A dim corridor stretches ahead. The walls are damp; a faint sound of water echoes. A flicker of light comes from the room to the east."

You: go east
Game: "You step into a small chamber. A table holds a rusted key and a rolled note."

You: take key
Game: "You pick up the Rusted Key."

You: read note
Game: "'They hide the truth behind the third tile.'"

You: inventory
Game: "You are carrying: Rusted Key, Torch (lit)."

You: go west
Game: "A locked door bars your way northwest."

You: use key on door
Game: "The key grinds, then turns. The lock gives with a shudder and the door opens..."

---

## Design notes (for contributors / maintainers)

- Language: Java (100%).
- Expected coding practices: small classes for room, item, player, entity, parser, and game engine.
- Parser: simple verb-noun parsing with flexible synonyms (e.g., take/pick up/grab).
- Save format: recommended JSON for human readability and ease of debugging.
- Tests: add unit tests for parser, item interactions, and combat mechanics where feasible.
