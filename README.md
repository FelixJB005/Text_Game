package programmes;

import java.util.*;

// -------- Main Entry --------
public class TheCorridor {
    public static void main(String[] args) {
        new Game().start();
    }
}

// -------- Game Engine --------
class Game {
    private Room[] rooms;
    private Player player;
    private boolean running = true;
    private Scanner in = new Scanner(System.in);

    public void start() {
        banner();
        askName();
        introStory();
        initWorld();
        look();
        while (running) {
            System.out.print("\n> ");
            String line = in.nextLine().trim();
            if (line.isEmpty()) continue;
            handleCommand(line);
        }
        System.out.println("\nThanks for playing.");
    }

    private void banner() {
        System.out.println("==============================");
        System.out.println("      THE CORRIDOR (Java)     ");
        System.out.println("==============================");
        System.out.println("Type 'command' for commands.");
    }

    private void askName() {
        System.out.print("Before we begin... what is your name, stranger? ");
        String pname = in.nextLine().trim();
        if (pname.isEmpty()) pname = "Detective";
        player = new Player(pname);
        System.out.println("Welcome, " + player.name() + ".");
    }

    private void introStory() {
        System.out.println("\nYou wake up on the cold floor. Your head throbs — someone knocked you out and dragged you here.\n"
                + "Dim lights flicker as you struggle to your feet. Four doors surround you.\n"
                + "At the far end, one door stands out with the word 'EXIT' carved into it... but it is locked tight.");
    }

    private void initWorld() {
        rooms = new Room[6];
        rooms[0] = new Room("corridor",
            "A dim corridor with flickering lights. Four doors surround you. At the far end, a heavy door marked 'EXIT' stands locked.");
        rooms[1] = new Room("room1", "A dusty room. On a desk lies a riddle.",
            new Item("L", "Letter L"), "What is 2 + 2?", "4");
        rooms[2] = new Room("room2", "Stacks of crates hide a paper with a question.",
            new Item("O", "Letter O"), "What is the capital of France?", "paris");
        rooms[3] = new Room("room3", "A dripping pipe echoes. A chalk puzzle is written on the wall.",
            new Item("C", "Letter C"), "I speak without a mouth and hear without ears. What am I?", "echo");
        rooms[4] = new Room("room4", "An abandoned bunk bed. A note is scribbled.",
            new Item("K", "Letter K"), "I’m an odd number. Take away one letter and I become even. What number am I?", "seven");
        rooms[5] = new Room("exit", "The heavy door marked 'EXIT'. It has a keypad and looks impossible to force open.");
        player.moveTo(0);
    }

    private void handleCommand(String line) {
        String[] parts = line.split(" ", 2);
        String cmd = parts[0].toLowerCase();
        String arg = parts.length > 1 ? parts[1].trim() : "";

        if (cmd.equals("command")) help();
        else if (cmd.equals("look")) look();
        else if (cmd.equals("approach")) approach(arg);
        else if (cmd.equals("take")) take(arg);
        else if (cmd.equals("inspect")) inspect(arg);
        else if (cmd.equals("combine")) combine();
        else if (cmd.equals("inventory") || cmd.equals("inv")) inventory();
        else if (cmd.equals("lock")) unlockExit();
        else if (cmd.equals("answer")) answer(arg);
        else if (cmd.equals("exitroom")) exitRoom();
        else if (cmd.equals("quit")) running = false;
        else {
            // Try treating it as a puzzle answer
            Room r = currentRoom();
            if (r.getPuzzle() != null && !r.isSolved()) {
                answer(line);
            } else {
                System.out.println("I don't understand that command.");
            }
        }
    }

    private void help() {
        System.out.println("Commands:");
        System.out.println("  look                 - describe the current room");
        System.out.println("  approach <room/exit> - move toward a room or exit (asks if you want to enter)");
        System.out.println("  take <item>          - pick up an item if puzzle solved");
        System.out.println("  answer <solution>    - attempt to solve the room's puzzle");
        System.out.println("  combine              - combine clues into the pass word");
        System.out.println("  lock                 - type this at the exit door to escape");
        System.out.println("  exitRoom             - type this to escape a room back to the corridor");
        System.out.println("  inventory | inv      - show what you carry");
        System.out.println("  quit                 - leave the game");
    }

    private void look() {
        Room r = currentRoom();
        System.out.println("\n" + r.describe());
        if (r.getPuzzle() != null && !r.isSolved()) {
            System.out.println("Puzzle: " + r.getPuzzle());
            System.out.println("Type 'answer <your answer>' to try solving it.");
        }
        if (r.isSolved() && r.getItem() != null) {
            System.out.println("You see: " + r.getItem().name());
        }
    }

    // -------- FIXED APPROACH --------
    private void approach(String target) {
        // Exit door
        if (target.equalsIgnoreCase("exit")) {
            if (player.location() == 5) {
                System.out.println("You are already at the exit door.");
                return;
            }
            System.out.println("You walk toward the large door marked 'EXIT'. It's locked and has a keypad.");
            player.moveTo(5);
            look();
            return;
        }

        // Rooms 1-4
        int targetIndex = -1;
        if (target.equalsIgnoreCase("room1")) targetIndex = 1;
        else if (target.equalsIgnoreCase("room2")) targetIndex = 2;
        else if (target.equalsIgnoreCase("room3")) targetIndex = 3;
        else if (target.equalsIgnoreCase("room4")) targetIndex = 4;

        if (targetIndex != -1) {
            if (player.location() == targetIndex) {
                System.out.println("You're already inside " + target + ".");
                return;
            }
            System.out.println("You stand before " + target + ". Do you want to enter? (yes/no)");
            String choice = in.nextLine().trim().toLowerCase();

            if (choice.equals("yes")) {
                player.moveTo(targetIndex);
                System.out.println("You enter " + target + "...");
                look();
            } else {
                System.out.println("You decide not to enter and remain in the corridor.");
                player.moveTo(0);
            }
        } else {
            System.out.println("You can't approach that.");
        }
    }

    private void take(String name) {
        Room r = currentRoom();
        if (!r.isSolved()) {
            System.out.println("You need to solve the puzzle first.");
            return;
        }
        if (r.getItem() == null) {
            System.out.println("No item here.");
            return;
        }
        if (name.equalsIgnoreCase(r.getItem().name())) {
            player.addItem(r.getItem());
            System.out.println("Taken: " + r.getItem().name());
            r.removeItem();
        } else {
            System.out.println("That item is not here.");
        }
    }

    private void inspect(String target) {
        if (player.has(target)) {
            Item it = player.getItem(target);
            System.out.println(it.describe());
        } else {
            System.out.println("You don't see that.");
        }
    }

    private void answer(String attempt) {
        Room r = currentRoom();
        if (r.getPuzzle() == null) {
            System.out.println("No puzzle here.");
            return;
        }
        if (attempt.equalsIgnoreCase(r.getAnswer())) {
            System.out.println("Correct! A clue appears: " + r.getItem().name());
            r.setSolved(true);

            if (r.getItem() != null) {
                System.out.println("Do you want to pick up the item? (yes/no)");
                String choice = in.nextLine().trim().toLowerCase();
                if (choice.equals("yes")) {
                    player.addItem(r.getItem());
                    System.out.println("You picked up: " + r.getItem().name());
                    r.removeItem();
                } else {
                    System.out.println("You leave the item where it is.");
                }
            }

            System.out.println("Do you want to leave the room and go back to the corridor? (yes/no)");
            String leave = in.nextLine().trim().toLowerCase();
            if (leave.equals("yes")) {
                player.moveTo(0);
                System.out.println("You return to the corridor.");
                look();
            } else {
                System.out.println("You remain inside " + r.id() + ".");
            }

        } else {
            System.out.println("Wrong answer. Try again.");
        }
    }

    private void combine() {
        if (player.has("L") && player.has("O") && player.has("C") && player.has("K")) {
            System.out.println("You combine the letters into the word: LOCK.");
        } else {
            System.out.println("You don't have all the letters yet.");
        }
    }

    private void unlockExit() {
        if (player.location() == 5) {
            System.out.print("Enter password: ");
            String attempt = in.nextLine().trim().toLowerCase();
            if (attempt.equals("lock")) {
                System.out.println("The keypad accepts the word LOCK. The heavy door unlocks, and you step into freedom!");
                running = false;
            } else {
                System.out.println("The keypad flashes red: WRONG PASSWORD.");
            }
        } else {
            System.out.println("You can only use this command at the exit door.");
        }
    }

    private void exitRoom() 
    {
    int loc = player.location();
    if (loc >= 1 && loc <= 4) {
        player.moveTo(0);
        System.out.println("You leave " + currentRoom().id() + " and return to the corridor.");
        look();
    } else {
        System.out.println("You’re not inside a room right now.");
    }
    }

    private void inventory() {
        if (player.inventoryCount() == 0) {
            System.out.println("You're carrying nothing.");
        } else {
            System.out.print(player.name() + " is carrying: ");
            player.showInventory();
        }
    }

    private Room currentRoom() {
        return rooms[player.location()];
    }
}

// -------- Domain: Room --------
class Room {
    private String id;
    private String description;
    private Item item;
    private String puzzle;
    private String answer;
    private boolean solved = false;

    public Room(String id, String description) {
        this.id = id;
        this.description = description;
    }

    public Room(String id, String description, Item item, String puzzle, String answer) {
        this.id = id;
        this.description = description;
        this.item = item;
        this.puzzle = puzzle;
        this.answer = answer;
    }

    public String id() { return id; }
    public String describe() { return description; }

    public Item getItem() { return item; }
    public void removeItem() { this.item = null; }

    public String getPuzzle() { return puzzle; }
    public String getAnswer() { return answer; }
    public boolean isSolved() { return solved; }
    public void setSolved(boolean s) { this.solved = s; }
}

// -------- Domain: Item --------
class Item {
    private String name;
    private String description;

    public Item(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String name() { return name; }
    public String describe() { return description; }
}

// -------- Domain: Player --------
class Player {
    private String playerName;
    private int currentRoom;
    private Item[] inventory = new Item[10];
    private int count = 0;

    public Player(String playerName) { this.playerName = playerName; }
    public String name() { return playerName; }

    public void moveTo(int roomIndex) { this.currentRoom = roomIndex; }
    public int location() { return currentRoom; }

    public void addItem(Item item) { inventory[count++] = item; }

    public boolean has(String name) {
        for (int i = 0; i < count; i++) {
            if (inventory[i] != null && inventory[i].name().equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    public Item getItem(String name) {
        for (int i = 0; i < count; i++) {
            if (inventory[i] != null && inventory[i].name().equalsIgnoreCase(name)) return inventory[i];
        }
        return null;
    }

    public int inventoryCount() { return count; }

    public void showInventory() {
        for (int i = 0; i < count; i++) {
            if (inventory[i] != null) System.out.print(inventory[i].name() + " ");
        }
        System.out.println();
    }
}
