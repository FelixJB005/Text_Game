import java.util.*;


// -------- Main Entry --------
public class Main {
public static void main(String[] args) {
new Game().start();
}
}


// -------- Game Engine --------
class Game {
private Room[] rooms;
private Player player = new Player("Detective");
private boolean running = true;
private Scanner in = new Scanner(System.in);


public void start() {
banner();
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
System.out.println(" THE CORRIDOR (Java) ");
System.out.println("==============================");
System.out.println("Type 'help' for commands.");
}


private void introStory() {
System.out.println("\nYou wake up on the cold floor. Your head throbs — someone knocked you out and dragged you here.\n"
+ "Dim lights flicker. A corridor stretches ahead with doors on both sides.\n"
+ "Each room whispers of a clue. Only by solving the puzzles in each room and collecting all five letters can you escape.");
}


private void initWorld() {
rooms = new Room[7];
rooms[0] = new Room("corridor", "A dim corridor with flickering lights. Five doors surround you. The exit is forward.");
rooms[1] = new Room("room1", "A dusty room. On a desk lies a riddle.", new Item("C", "Letter C"), "What is 2 + 2?", "4");
rooms[2] = new Room("room2", "Stacks of crates hide a paper with a question.", new Item("O", "Letter O"), "What is the capital of France?", "paris");
rooms[3] = new Room("room3", "A dripping pipe echoes. A chalk puzzle is written on the wall.", new Item("D", "Letter D"), "What word is spelled backwards as 'god'?", "dog");
rooms[4] = new Room("room4", "An abandoned bunk bed. A note is scribbled.", new Item("E", "Letter E"), "How many sides does a square have?", "4");
rooms[5] = new Room("room5", "A broken console screen flickers with text.", new Item("X", "Letter X"), "Complete the sequence: A, B, C, ?", "d");
rooms[6] = new Room("exit", "A large steel door with a keypad. This is the only way out.");


player.moveTo(0);
}


private void handleCommand(String line) {
String[] parts = line.split(" ", 2);
String cmd = parts[0].toLowerCase();
String arg = parts.length > 1 ? parts[1].trim() : "";


if (cmd.equals("help")) help();
else if (cmd.equals("look")) look();
else if (cmd.equals("go")) go(arg);
else if (cmd.equals("take")) take(arg);
else if (cmd.equals("inspect")) inspect(arg);
else if (cmd.equals("combine")) combine();
else if (cmd.equals("inventory") || cmd.equals("inv")) inventory();
else if (cmd.equals("codex")) unlockExit();
else if (cmd.equals("answer")) answer(arg);
else if (cmd.equals("quit")) running = false;
else System.out.println("I don't understand that command.");
}


private void help() {
System.out.println("Commands:");
System.out.println(" look - describe the current room");
System.out.println(" go <north|east|west|up|forward|south|back> - move between rooms");
}
