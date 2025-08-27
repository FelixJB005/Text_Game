package programmes;

import java.util.*;

public class CorridorEscape {

    private Scanner scanner = new Scanner(System.in);
    private Player player = new Player();
    private boolean testerMode = false;

    public static void main(String[] args) {
        new CorridorEscape().start();
    }

    private void start() {
        banner();

        System.out.print("Are you a player or a tester? ");
        String mode = scanner.nextLine().trim().toLowerCase();

        // Ask for name (was missing previously)
        System.out.print("Enter your name: ");
        String name = scanner.nextLine().trim();
        player.setName(name.isEmpty() ? "Player" : name);

        if (mode.equals("tester")) {
            testerMode = true;
            System.out.println("\n[Tester mode enabled. You can freely explore and exit without combining clues.]");
            System.out.println("Available commands: look, approach <room>, inventory, combine, hint, quit");
            player.moveTo(0);
            look();
        } else {
            System.out.println("\nYou find yourself in a long, dim corridor with doors on each side.");
            player.moveTo(0); // corridor
            look(); // show initial description for non-testers also
        }

        while (true) {
            System.out.print("\n> ");
            String cmd = scanner.nextLine().trim().toLowerCase();
            if (cmd.equals("quit")) {
                System.out.println("Game over.");
                break;
            } else if (cmd.startsWith("approach")) {
                move(cmd);
            } else if (cmd.equals("look")) {
                look();
            } else if (cmd.equals("combine")) {
                combine();
            } else if (cmd.equals("inventory")) {
                player.showInventory();
            } else if (cmd.equals("hint")) {
                hint();
            } else {
                System.out.println("Unknown command.");
            }
        }
    }

    private void banner() {
        System.out.println("\n===================================");
        System.out.println("\n                                   ");
        System.out.println("          THE CORRIDOR ESCAPE       ");
        System.out.println("\n                                   ");
        System.out.println("===================================\n");
    }

    private void move(String cmd) {
        if (cmd.startsWith("approach")) {
            String target = cmd.substring(8).trim();

            switch (target) {
                case "room1":
                    player.moveTo(1);
                    break;
                case "room2":
                    player.moveTo(2);
                    break;
                case "room3":
                    player.moveTo(3);
                    break;
                case "room4":
                    player.moveTo(4);
                    break;
                case "exit":
                    player.moveTo(5);
                    break;
                default:
                    System.out.println("That place doesn’t exist.");
                    return;
            }
            look();
        }
    }

    private void look() {
        switch (player.getLocation()) {
            case 0:
                System.out.println("You are in the corridor. Doors lead to Room1, Room2, Room3, Room4, and the Exit.");
                break;
            case 1:
                System.out.println("Room1: A stone tablet with strange carvings awaits you.");
                puzzleRoom1();
                break;
            case 2:
                System.out.println("Room2: A dusty library with a single glowing book.");
                puzzleRoom2();
                break;
            case 3:
                System.out.println("Room3: A flickering terminal hums faintly.");
                puzzleRoom3();
                break;
            case 4:
                System.out.println("Room4: A painting hangs crooked, hiding something behind it.");
                puzzleRoom4();
                break;
            case 5:
                if (testerMode) {
                    System.out.println("As a tester, you bypass the puzzles. The door accepts your presence.");
                    conclusionScene();
                    System.exit(0);
                } else {
                    if (player.hasCombined()) {
                        System.out.print("The keypad glows. Enter the passkey: ");
                        String pass = scanner.nextLine().trim().toLowerCase();
                        if (pass.equals("lock")) {
                            conclusionScene();
                            System.exit(0);
                        } else {
                            System.out.println("Wrong passkey.");
                        }
                    } else {
                        System.out.println("The keypad glows, but the symbols make no sense… You need to combine your clues first.");
                    }
                }
                break;
        }
    }

    private void puzzleRoom1() {
        if (!player.hasItem("Roman Numeral Clue")) {
            System.out.print("Puzzle: What is X + V in Roman numerals? ");
            String ans = scanner.nextLine().trim().toUpperCase();
            if (ans.equals("XV")) {
                System.out.println("Correct! You found a Roman Numeral Clue.");
                System.out.print("Take the item? (yes/no): ");
                if (scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                    player.addItem("Roman Numeral Clue");
                }
            } else {
                System.out.println("Wrong answer.");
            }
        }
    }

    private void puzzleRoom2() {
        if (!player.hasItem("Cipher Clue")) {
            System.out.print("Puzzle: Decode this (Caesar shift 1): MPQF. ");
            String ans = scanner.nextLine().trim().toLowerCase();
            if (ans.equals("lope")) {
                System.out.println("Correct! You found a Cipher Clue.");
                System.out.print("Take the item? (yes/no): ");
                if (scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                    player.addItem("Cipher Clue");
                }
            } else {
                System.out.println("Wrong answer.");
            }
        }
    }

    private void puzzleRoom3() {
        if (!player.hasItem("Binary Clue")) {
            System.out.print("Puzzle: What does 01001100 translate to in ASCII? ");
            String ans = scanner.nextLine().trim().toUpperCase();
            if (ans.equals("L")) {
                System.out.println("Correct! You found a Binary Clue.");
                System.out.print("Take the item? (yes/no): ");
                if (scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                    player.addItem("Binary Clue");
                }
            } else {
                System.out.println("Wrong answer.");
            }
        }
    }

    private void puzzleRoom4() {
        if (!player.hasItem("Riddle Clue")) {
            System.out.print("Puzzle: I speak without a mouth and hear without ears. What am I? ");
            String ans = scanner.nextLine().trim().toLowerCase();
            if (ans.equals("echo")) {
                System.out.println("Correct! You found a Riddle Clue.");
                System.out.print("Take the item? (yes/no): ");
                if (scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                    player.addItem("Riddle Clue");
                }
            } else {
                System.out.println("Wrong answer.");
            }
        }
    }

    private void combine() {
        if (player.hasAllItems()) {
            System.out.println("You piece the four clues together… The answer is LOCK.");
            player.setCombined(true);
        } else {
            System.out.println("You don’t have all the clues yet.");
        }
    }

    private void hint() {
        switch (player.getLocation()) {
            case 1:
                System.out.println("Hint: X is 10, V is 5.");
                break;
            case 2:
                System.out.println("Hint: Caesar shift means move each letter back one.");
                break;
            case 3:
                System.out.println("Hint: Convert binary to ASCII.");
                break;
            case 4:
                System.out.println("Hint: Think about something that repeats your voice.");
                break;
            default:
                System.out.println("No hints here.");
        }
    }

    private void conclusionScene() {
        System.out.println("\n===================================");
        System.out.println("           ESCAPE ACHIEVED          ");
        System.out.println("===================================");
        System.out.println("The keypad beeps. The lock clicks open. ");
        System.out.println("The EXIT door groans as it swings wide, spilling pale light into the corridor.");
        System.out.println();
        System.out.println("You step through. The stale air of confinement is replaced by the cool breath of freedom.");
        System.out.println("Behind you, the corridor waits in silence, its secrets locked away once more.");
        System.out.println();
        System.out.println("You made it out.");
        System.out.println("===================================");
    }
}

class Player {
    private int location = 0;
    private List<String> inventory = new ArrayList<>();
    private boolean combined = false;
    private String name = "Player";

    public void moveTo(int loc) {
        this.location = loc;
    }

    public int getLocation() {
        return location;
    }

    public void addItem(String item) {
        if (!inventory.contains(item)) {
            inventory.add(item);
            System.out.println(item + " added to inventory.");
        }
    }

    public boolean hasItem(String item) {
        return inventory.contains(item);
    }

    public void showInventory() {
        if (inventory.isEmpty()) {
            System.out.println("Inventory is empty.");
        } else {
            System.out.println("Inventory: " + inventory);
        }
    }

    public boolean hasAllItems() {
        return inventory.contains("Roman Numeral Clue") &&
               inventory.contains("Cipher Clue") &&
               inventory.contains("Binary Clue") &&
               inventory.contains("Riddle Clue");
    }

    public void setCombined(boolean value) {
        combined = value;
    }

    public boolean hasCombined() {
        return combined;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
