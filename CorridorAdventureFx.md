/*
    CorridorAdventure.java
    ----------------------
    Complete JavaFX game class with inventory, puzzles, cinematic endings.

    Project structure 
        CorridorAdventureFx/
        │
        ├─ src/
        │   └─ CorridorAdventure.java <-- .java file goes here
        │
        ├─ out/                 <-- compiled .class files go here
        │
        ├─ images/              <-- all game images go here
        │   ├─ shadow.png       <-- shadow image used in death sequence
        │   ├─ death.png        <-- death cinematic image
        │   ├─ room1.png        <-- example room image (if you have visuals per room)
        │   ├─ storage.png      <-- storage room image
        │   ├─ study.png        <-- study room image
        │   └─ any_other_images.png  <-- add other game images here

*/
import javafx.animation.PauseTransition;   //: small delays / timed transitions between scenes.
import javafx.application.Application;  // : JavaFX entry point class.
import javafx.application.Platform;       // : used to exit the app cleanly.
import javafx.geometry.*;                // : layout helpers (Insets, Pos).
import javafx.scene.*;                    //: core scene graph, controls, layout containers.
import javafx.scene.image.*;              //: load and display background images.
import javafx.scene.layout.*;             //: BorderPane, VBox/HBox, StackPane for layout composition.
import javafx.scene.paint.Color;          //: used for overlay/text color.
import javafx.scene.text.Font;             //: set fonts for labels and controls.
import javafx.stage.Stage;                 //: main application window.
import javafx.util.Duration;               //: durations for PauseTransition.
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;


import java.io.File;                       //: check for presence of image files on disk.
import java.util.*;                        //: collections and Optional used by dialogs and state.

public class CorridorAdventure extends Application {

    // -------- timing constants (tweak these if you want slower/faster) -------
    private static final double PAUSE_SHORT = 1.0;
    private static final double PAUSE_MED   = 1.0;
    private static final double PAUSE_LONG  = 3.0;
    private static final double BLACKOUT_DEATH = 2.0;
    private Label desc;
    // -------- game & UI state ----------
    private Stage primaryStage;
    private final String IMAGE_PATH = "C:/Java/Programe/images/"; // change to your images folder
    private String playerName = "";

    private boolean ended = false;         // reached ending flag
    private Set<Character> letters = new LinkedHashSet<>(); // collected letters O,C,K
    private Map<String, Boolean> solved = new HashMap<>(); // solved flags

    // top-right inventory box (rebuilt each time)
    private VBox inventoryBox;
    // hidden log area for internal messages (not shown in UI)
    private TextArea logArea;

    public static void main(String[] args) { launch(args); }

    // -------------------- Helpers --------------------

    /** Load an ImageView for a file under IMAGE_PATH; returns null if not present. */
    private ImageView loadImageView(String fileName) {
        if (fileName == null) return null;
        File f = new File(IMAGE_PATH + fileName);
        if (!f.exists()) return null;
        Image img = new Image(f.toURI().toString());
        ImageView iv = new ImageView(img);
        iv.setPreserveRatio(false);
        iv.setSmooth(true);
        return iv;
    }

    /** Check if image file exists in IMAGE_PATH. */
    private boolean fileExists(String fileName) {
        return fileName != null && new File(IMAGE_PATH + fileName).exists();
    }

    /** Unified button style. */
    private void styleButton(Button b) {
        b.setFont(Font.font(14));
        b.setStyle("-fx-background-color: rgba(0,0,0,0.75); -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-padding: 6 12 6 12; -fx-border-color: #666; -fx-border-width:1;");
    }

    /** Show a brief full-screen black scene and then run the Runnable. */
    private void showTemporaryBlackout(double seconds, Runnable after) {
        StackPane black = new StackPane();
        black.setStyle("-fx-background-color:black;");
        Scene blackScene = new Scene(black, Math.max(1000, primaryStage.getWidth()), Math.max(700, primaryStage.getHeight()));
        primaryStage.setScene(blackScene);
        PauseTransition p = new PauseTransition(Duration.seconds(seconds));
        p.setOnFinished(e -> after.run());
        p.play();
    }

    /**
     * Build a scene with optional background image, a dimming rectangle, and an overlay BorderPane.
     */
    private Scene buildScene(ImageView bg, BorderPane overlay, double w, double h) {
        StackPane root = new StackPane();
        root.setPrefSize(w, h);

        if (bg != null) root.getChildren().add(bg);
        else {
            Region black = new Region();
            black.setStyle("-fx-background-color:black;");
            root.getChildren().add(black);
        }

        // semi-transparent dark overlay to make text readable
        Rectangle dark = new Rectangle();
        dark.setFill(Color.color(0, 0, 0, 0.35));
        dark.widthProperty().bind(root.widthProperty());
        dark.heightProperty().bind(root.heightProperty());
        root.getChildren().add(dark);

        root.getChildren().add(overlay);
        Scene scene = new Scene(root, w, h);

        if (bg != null) {
            bg.fitWidthProperty().bind(scene.widthProperty());
            bg.fitHeightProperty().bind(scene.heightProperty());
        }
        return scene;
    }

    /**
     * Build the top bar: left = narrative description, right = compact inventory (if showInventory).
     * We keep a hidden logArea (not added to scene) to capture text for debugging/history.
     */
    private VBox buildTopBar(boolean showInventory, Label descLabel) {
        if (logArea == null) {
            logArea = new TextArea();
            logArea.setEditable(false);
            logArea.setWrapText(true);
            logArea.setPrefRowCount(4);
            logArea.setFont(Font.font(13));
            // intentionally not added to scene graph (hidden)
        }

        if (inventoryBox == null) {
            inventoryBox = new VBox(4);
            inventoryBox.setPadding(new Insets(6));
            inventoryBox.setAlignment(Pos.TOP_RIGHT);
            inventoryBox.setStyle("-fx-background-color: rgba(0,0,0,0.45); -fx-border-color: #444; -fx-border-width: 1;");
        }

        descLabel.setWrapText(true);
        descLabel.setFont(Font.font("Serif", 15));
        descLabel.setTextFill(Color.WHITE);
        descLabel.setMaxWidth(760);

        HBox topRow = new HBox(12);
        HBox.setHgrow(descLabel, Priority.ALWAYS);
        topRow.getChildren().add(descLabel);
        if (showInventory) {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            updateInventoryUI(); // rebuild inventory contents
            topRow.getChildren().addAll(spacer, inventoryBox);
        }

        VBox container = new VBox(6);
        container.getChildren().add(topRow);
        container.setPadding(new Insets(6));
        return container;
    }

    /**
     * Update inventory UI EXACTLY as requested:
     * If pendantTaken == false:
     *   1. Magnifying Glass
     *   2. Pendant (not taken)
     *   3. L : _
     *   4. O : _
     *   5. C : _
     *   6. K : _
     *
     * If pendantTaken == true:
     *   1. Magnifying Glass
     *   2. Pendant (3)    -> show numeric pendantNumber inside ()
     *   3. L : L         -> literal 'L'
     *   4. O : O/_ etc.
     */
    // Class fields
private boolean pendantTaken = false;
private int pendantNumber = 3; // starting lives

// Update inventory display
private void updateInventoryUI() {
    if (inventoryBox == null) return;
    inventoryBox.getChildren().clear();

    Label line1 = new Label("1. Magnifying Glass");
    line1.setTextFill(Color.WHITE);
    line1.setFont(Font.font(13));

    Label line2 = new Label(
        pendantTaken
            ? ("2. Pendant (" + (pendantNumber >= 0 ? pendantNumber : 0) + ")         ")
            : "2. Pendant (not taken)"
    );
    line2.setTextFill(Color.WHITE);
    line2.setFont(Font.font(13));

    Label line3 = new Label("3. L : " + (pendantTaken ? "L                      " : "_                      "));
    line3.setTextFill(Color.WHITE);
    line3.setFont(Font.font(13));

    Label line4 = new Label("4. O : " + (letters.contains('O') ? "O                    " : "_                      "));
    line4.setTextFill(Color.WHITE);
    line4.setFont(Font.font(13));

    Label line5 = new Label("5. C : " + (letters.contains('C') ? "C                      " : "_                      "));
    line5.setTextFill(Color.WHITE);
    line5.setFont(Font.font(13));

    Label line6 = new Label("6. K : " + (letters.contains('K') ? "K                      " : "_                      "));
    line6.setTextFill(Color.WHITE);
    line6.setFont(Font.font(13));

    inventoryBox.getChildren().addAll(line1, line2, new Separator(), line3, line4, line5, line6);
}

// Fail handling for puzzle attempts


    /** Prompt for single-line answer used by puzzles. */
    private String promptForAnswer(String title, String question) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(question);
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    /** Append to the hidden log (internal only). */
    private void appendText(String text) {
        if (logArea == null) return;
        logArea.appendText(text + "\n");
    }

    /** Print poetic stanza into the hidden log (useful for debugging/history). */
    private void printLockStanza(int number) {
    if (desc != null) {
        desc.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
    }

    appendText("The cursed lock begins ticking...");
    appendText("The number shifts: " + number);
    if (number == 3) appendText("A shadow flickers at the edge of your vision.");
    if (number == 2) appendText("You hear footsteps behind you, drawing closer...");
    if (number == 1) appendText("A whisper breathes your name. Your skin turns cold.");
    if (number <= 0) {
        appendText("The lock clicks to zero.");
        appendText("The shadows rise like a tide, swallowing every inch of light.");
        appendText("You are trapped eternally, consumed by the darkness...");
    }
}


    // -------------------- Application start --------------------
    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("Corridor Adventure");
        showTitleScreen();
    }

    // -------------------- Screens --------------------

    /** Title screen: name field + Start / Credits / Walkthrough buttons. */
    private void showTitleScreen() {
        ImageView bg = loadImageView("title.jpg");
        BorderPane overlay = new BorderPane();
        overlay.setPrefSize(1000, 700);

        Label prompt = new Label("Enter your name and step into the Case.");
        prompt.setTextFill(Color.BLUEVIOLET);
        prompt.setFont(Font.font(18));

        TextField nameField = new TextField();
        nameField.setPromptText("Enter name here");
        nameField.setMaxWidth(360);
        nameField.setFont(Font.font(15));

        Button startBtn = new Button("Start");
        Button creditsBtn = new Button("Credits");
        Button walkBtn = new Button("Walkthrough");
        styleButton(startBtn); styleButton(creditsBtn); styleButton(walkBtn);

        startBtn.setOnAction(e -> {
            playerName = nameField.getText().isEmpty() ? "Detective" : nameField.getText();
            resetGameState();
            appendText("You are " + playerName + ", a world-class detective who doesn't let mysteries rest.");
            showMansionOut();
        });

        creditsBtn.setOnAction(e -> showCredits());
        walkBtn.setOnAction(e -> showWalkthrough());

        VBox controls = new VBox(10, prompt, nameField, startBtn, creditsBtn, walkBtn);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(40, 0, 40, 0)); // nudge down a bit so controls don't sit at top
        overlay.setCenter(controls);

        Scene scene = buildScene(bg, overlay, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showCredits() {
        BorderPane overlay = new BorderPane();
        overlay.setPrefSize(1000,700);
        Label credits = new Label(
                "Corridor Adventure \n" +
                        "Group : 13\n\n" +
                        "Created by:\n" +
                        "  Andria (Coding, Group-Leader)\n" +
                        "  Adil (Coding, Puzzles)\n" +
                        "  Felix (Coding, Idea & Images)\n" +
                        "  Anusree (Coding, Game Plot)\n\n" +
                        "GameEngine: JavaFX\n\nThanks to: \"Empty cups of coffee everywhere\" "
        );
        credits.setTextFill(Color.WHITE);
        credits.setFont(Font.font(16));
        credits.setWrapText(true);
        credits.setMaxWidth(760);
        overlay.setCenter(credits);

        Button back = new Button("Back");
        styleButton(back);
        back.setOnAction(e -> showTitleScreen());
        overlay.setBottom(back);
        BorderPane.setAlignment(back, Pos.BOTTOM_CENTER);
        BorderPane.setMargin(back, new Insets(0,0,40,0));

        Scene s = buildScene(null, overlay, 1000,700);
        primaryStage.setScene(s);
    }

    private void showWalkthrough() {
        BorderPane overlay = new BorderPane();
        overlay.setPrefSize(1000,700);
        Label w = new Label(
                "Walkthrough:\n" +
                        "1. Enter your name and wake with the magnifying glass.\n" +
                        "2. Take the pendant in Mansion In to carry it (shows Life value).\n" +
                        "3. Solve: Room 1 -> O, Storage -> C, Study -> K.\n" +
                        "4. Death Ending : Wrong answers or trying South early lower Life value on the \" Pendant \". \n \t \t If L < 0 you will be swallowed by darkness.\n" +
                        "5. Escape Ending : Gather O C K to escape through South.\n \t \t To see another sunrise."
        );
        w.setWrapText(true);
        w.setTextFill(Color.WHITE);
        w.setFont(Font.font(15));
        w.setMaxWidth(760);
        overlay.setCenter(w);

        Button back = new Button("Back");
        styleButton(back);
        back.setOnAction(e -> showTitleScreen());
        overlay.setBottom(back);
        BorderPane.setAlignment(back, Pos.BOTTOM_CENTER);
        BorderPane.setMargin(back, new Insets(0,0,40,0));

        Scene s = buildScene(null, overlay, 1000,700);
        primaryStage.setScene(s);
    }

    /** Mansion outside description + button to approach the mansion. */
    private void showMansionOut() {
        ImageView bg = loadImageView("mansion_out.jpg");
        BorderPane overlay = new BorderPane();

        Label desc = new Label(
                playerName + " — a world-class detective. Rain beads on your collar and the house stares back with blind windows. " +
                        "You have come following the trail of numerous missing cases — names and faces that never found their way home.\n\n" +
                        "A magnifying glass sits in your pocket, a small, stubborn proof that you won't leave a question unanswered. " +
                        "The mansion's stonework is pitted and patient; shutters rattle with the sighs of rooms that remember."
        );
        overlay.setTop(buildTopBar(true, desc));
        BorderPane.setMargin(overlay.getTop(), new Insets(8));

        Button north = new Button("North (Approach Mansion)");
        styleButton(north);
        north.setOnAction(e -> showMansionIn());

        VBox center = new VBox(12, north);
        center.setAlignment(Pos.TOP_CENTER);
        center.setPadding(new Insets(40,12,12,12));
        overlay.setCenter(center);

        Scene s = buildScene(bg, overlay, 1000,700);
        primaryStage.setScene(s);
    }

    /** Mansion interior: inspect & take pendant. */
    private void showMansionIn() {
        ImageView bg = loadImageView("mansion_in.jpg");
        BorderPane overlay = new BorderPane();

        Label desc = new Label(
                "You step through a heavy door into the faded grandeur of the hall. Cobwebs cling to carved bannisters and dust motes spin in the slanting light.\n\n" +
                        "You search the room — drawers, mantle, and a narrow table — and at last your fingers close around a small, scarred pendant. It sits quiet and cold, " +
                        "yet somehow it feels alive, as if it were calling your name."
        );
        overlay.setTop(buildTopBar(true, desc));
        BorderPane.setMargin(overlay.getTop(), new Insets(8));

        Button inspect = new Button("Inspect Pendant");
        Button take = new Button("Take Pendant");
        styleButton(inspect); styleButton(take);

        inspect.setOnAction(e -> {
            appendText("You study the pendant: the back bears a carved 'L'. The front is a dial currently reading " + pendantNumber + ".");
            desc.setText("You study the pendant: the back bears a carved 'L'. The front is a dial currently reading " + pendantNumber + ".");
        });

        take.setOnAction(e -> {
            if (!pendantTaken) {
                pendantTaken = true;
                updateInventoryUI();
            }
            showTemporaryBlackout(PAUSE_SHORT, this::showHallway);
        });

        HBox bottom = new HBox(14, inspect, take);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(10));
        overlay.setBottom(bottom);
        BorderPane.setAlignment(bottom, Pos.BOTTOM_CENTER);

        Scene s = buildScene(bg, overlay, 1000,700);
        primaryStage.setScene(s);
    }

    /** Corridor hallway with navigation to rooms and exit. */
    private void showHallway() {
        ImageView bg = loadImageView("hallway.jpg");
        BorderPane overlay = new BorderPane();

        Label desc = new Label(
                "A narrow corridor stretches out, lit by a single flickering bulb. Doors wait north, east, and west. " +
                        "South, a heavy iron door bristles with ancient locks."
        );
        desc.setTextFill(Color.rgb(161, 3, 252, 1.0));
        overlay.setTop(buildTopBar(true, desc));
        BorderPane.setMargin(overlay.getTop(), new Insets(8));

        Button north = new Button("North (Room 1)");
        Button east = new Button("East (Storage)");
        Button west = new Button("West (Study)");
        Button south = new Button("South (Exit)");
        styleButton(north); styleButton(east); styleButton(west); styleButton(south);

        north.setOnAction(e -> showPuzzleRoom("Room 1"));
        east.setOnAction(e -> showPuzzleRoom("Storage"));
        west.setOnAction(e -> showPuzzleRoom("Study"));
        south.setOnAction(e -> trySouthDoor());

        VBox topCenter = new VBox(north);
        topCenter.setAlignment(Pos.TOP_CENTER);
        topCenter.setPadding(new Insets(6,0,0,0));
        overlay.setCenter(topCenter);

        VBox leftBox = new VBox(west); leftBox.setAlignment(Pos.CENTER_LEFT); leftBox.setPadding(new Insets(0,12,0,12));
        overlay.setLeft(leftBox);

        VBox rightBox = new VBox(east); rightBox.setAlignment(Pos.CENTER_RIGHT); rightBox.setPadding(new Insets(0,12,0,12));
        overlay.setRight(rightBox);

        VBox bottomBox = new VBox(south); bottomBox.setAlignment(Pos.BOTTOM_CENTER); bottomBox.setPadding(new Insets(0,0,28,0));
        overlay.setBottom(bottomBox);

        updateInventoryUI();

        Scene s = buildScene(bg, overlay, 1000,700);
        primaryStage.setScene(s);
    }

private void trySouthDoor() {
    // choose the background image
    String attemptImage = fileExists("exit_attempt.jpg") 
            ? "exit_attempt.jpg" 
            : (fileExists("exit.jpg") ? "exit.jpg" : null);
    ImageView bg = attemptImage != null ? loadImageView(attemptImage) : null;

    BorderPane overlay = new BorderPane();

    // local narration label for this scene
    Label attemptText = new Label("You press your hand to the heavy southern door and pull. It resists with centuries of rust.");
    attemptText.setWrapText(true);
    attemptText.setTextFill(Color.WHITE);
    attemptText.setFont(Font.font(15));
    overlay.setCenter(attemptText);

    // build and show scene
    Scene s = buildScene(bg, overlay, 1000, 700);
    primaryStage.setScene(s);

    // pause before checking outcome
    PauseTransition pause = new PauseTransition(Duration.seconds(PAUSE_MED)); 
    pause.setOnFinished(ev -> {
        if (letters.contains('O') && letters.contains('C') && letters.contains('K')) {
            // success path
            appendText("You assemble the letters and press them into the dial. The mechanism accepts them with a shudder.");
            showEscapeSequence();
        } else {
            // failure path
            attemptText.setText("Shadows creep along the lock's seam, a cold breath exhaling from the iron. The mechanism resists.");
            attemptText.setTextFill(Color.RED);

            // print lock stanza text (use attemptText instead of global desc)
            printLockStanzaLocal(pendantNumber, attemptText);

            pendantNumber--;
            updateInventoryUI();

            if (pendantNumber < 0) {
                showDeathSequence();
            } else {
                PauseTransition p2 = new PauseTransition(Duration.seconds(PAUSE_SHORT));
                p2.setOnFinished(ev2 -> showHallway()); // go back to hallway
                p2.play();
            }
        }
    });
    pause.play();
}

// helper that works on the given label instead of desc
private void printLockStanzaLocal(int number, Label target) {
    target.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
    target.setText("The cursed lock begins ticking....\n \t The number shifts: " + number);

    if (number == 3) appendText("A shadow flickers at the edge of your vision.");
    if (number == 2) appendText("You hear footsteps behind you, drawing closer...");
    if (number == 1) appendText("A whisper breathes your name. Your skin turns cold.");
    if (number <= 0) {
        appendText("The lock clicks to zero.");
        appendText("The shadows rise like a tide, swallowing every inch of light.");
        appendText("You are trapped eternally, consumed by the darkness...");
    }
}


    /** Show a puzzle room with Inspect / Solve / Back controls. */
    private void showPuzzleRoom(String roomName) {
        ImageView bg = loadImageView(roomBackgroundFor(roomName));
        BorderPane overlay = new BorderPane();

        Label desc = new Label(roomDescription(roomName));
        desc.setWrapText(true);
        desc.setTextFill(Color.WHITE);
        desc.setFont(Font.font(15));
        overlay.setTop(buildTopBar(true, desc));
        BorderPane.setMargin(overlay.getTop(), new Insets(6));

        Button inspect = new Button("Inspect Room");
        Button solve = new Button("Solve");
        Button back = new Button("Back");
        styleButton(inspect); styleButton(solve); styleButton(back);

        inspect.setOnAction(e -> {
            switch (roomName) {
                case "Room 1":
                    appendText("Your fingers trace a circle pattern on the floor; one tile is subtly different.");
                    desc.setText(desc.getText() + "\nYou feel the different tile underfoot.");
                    break;
                case "Storage":
                    appendText("A jar wobbles loose. Behind it a crescent chip glints faintly.");
                    desc.setText(desc.getText() + "\nYou find a small crescent-shaped chip tucked away.");
                    break;
                case "Study":
                    appendText("The drawer hums faintly; the carved pattern hints at a single consonant.");
                    desc.setText(desc.getText() + "\nThe carved pattern points to a letter.");
                    break;
                default:
                    appendText("You look around, nothing of interest.");
                    desc.setText(desc.getText() + "\nNothing of note.");
            }
        });

        solve.setOnAction(e -> doSolve(roomName, desc));
        back.setOnAction(e -> showHallway());

        HBox bottom = new HBox(12, inspect, solve, back);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(10));
        overlay.setBottom(bottom);

        Scene s = buildScene(bg, overlay, 1000,700);
        primaryStage.setScene(s);
    }

    private String roomDescription(String room) {
        switch (room) {
            case "Room 1":
                return "Room 1 — a bare chamber with peeling wallpaper. On a crooked table rests a puzzle box, its carvings forming strange circles.";
            case "Storage":
                return "Storage — cracked crates and warped shelves. Something metallic glints between the debris.";
            case "Study":
                return "Study — shelves sag beneath moldy tomes. A desk with a locked drawer hums faintly.";
            default:
                return "A old door with scratch marks and an exit sign was present... it stood Dark and silent. The pendant seems to glow near it.";
        }
    }

    private String roomBackgroundFor(String room) {
        switch (room) {
            case "Room 1": return "room_1.jpg";
            case "Storage": return "storage.jpg";
            case "Study": return "study.jpg";
            default: return "storage.jpg";
        }
    }

    /** Puzzle solving logic: correct answers add letters; wrong answers call failPuzzleAndReturn. */
    private void doSolve(String room, Label desc) {
        if (ended) return;
        String ans;
        switch (room) {
            case "Room 1":
                if (solved.getOrDefault("room1", false)) { appendText("You've already solved this."); desc.setText("You've already solved this."); return; }
                ans = promptForAnswer("Room 1 Puzzle", "Room 1 riddle:\n'I appear round in moon and door. I am one complete circle. What letter am I?'");
                if (ans != null && ans.trim().equalsIgnoreCase("o")) {
                    letters.add('O'); solved.put("room1", true);
                    appendText("Correct. The tile yields the letter 'O'.");
                    desc.setText("Correct. The tile yields the letter 'O'.");
                    updateInventoryUI();
                } else {
                    appendText("That's not right.");
                    desc.setText("That's not right.");
                    failPuzzleAndReturn(room, desc);
                }
                break;
            case "Storage":
                if (solved.getOrDefault("storage", false)) { appendText("Storage puzzle already solved."); desc.setText("Storage puzzle already solved."); return; }
                ans = promptForAnswer("Storage Puzzle", "Storage puzzle:\n'A crescent curve.' Type the single letter.");
                if (ans != null && ans.trim().equalsIgnoreCase("c")) {
                    letters.add('C'); solved.put("storage", true);
                    appendText("Correct. You find the letter 'C'.");
                    desc.setText("Correct. You find the letter 'C'.");
                    updateInventoryUI();
                } else {
                    appendText("Nothing happens.");
                    desc.setText("Nothing happens.");
                    failPuzzleAndReturn(room, desc);
                }
                break;
            case "Study":
                if (solved.getOrDefault("study", false)) { appendText("Study puzzle already solved."); desc.setText("Study puzzle already solved."); return; }
                ans = promptForAnswer("Study Puzzle", "Study puzzle:\n'Silent in knife; starts knock.' Which letter?");
                if (ans != null && ans.trim().equalsIgnoreCase("k")) {
                    letters.add('K'); solved.put("study", true);
                    appendText("Correct. The drawer reveals 'K'.");
                    desc.setText("Correct. The drawer reveals 'K'.");
                    updateInventoryUI();
                } else {
                    appendText("The clue resists you.");
                    desc.setText("The clue resists you.");
                    failPuzzleAndReturn(room, desc);
                }
                break;
        }
    }

    /**
     * Fail handling:
     * - decrement pendantNumber and update inventory
     * - if pendantNumber < 0 -> run the death sequence (images + blackout + shadow.png)
     * - otherwise -> short textual feedback and return to same room (no blackout)
     */
    private void failPuzzleAndReturn(String room, Label desc) {
    // decrement lives on failure, clamp at 0
    pendantNumber = Math.max(pendantNumber - 1, 0);

    // update UI immediately
    updateInventoryUI();

    // print updated lock state (optional)
    printLockStanza(pendantNumber);

    if (pendantNumber <= 0) {
        // player dies
        showDeathSequence();
        return;
    }

    // give textual feedback
    desc.setText("The pendant grows colder. You steel yourself to try again.");
    desc.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

    appendText("A chill runs through you as the puzzle slips away...");

    // return to the same room after a short pause
    PauseTransition p = new PauseTransition(Duration.seconds(PAUSE_MED));
    p.setOnFinished(ev -> showPuzzleRoom(room));
    p.play();
}



    // -------------------- Death & Escape sequences --------------------

    /**
     * Death sequence EXACT (per your instruction):
     * 1) show exit_2.jpg for 3s (or shadow.png fallback)
     * 2) blackout for 2s
     * 3) show shadow.png for 3s
     * 4) final death text + alert -> exit
     */
private void showDeathSequence() {
    // Step 1: show exit_2.jpg (fallback to shadow.png if missing)
    String firstImg = fileExists("exit_2.jpg") ? "exit_2.jpg" : (fileExists("shadow.png") ? "shadow.png" : null);
    ImageView bg1 = loadImageView(firstImg);

    BorderPane overlay1 = new BorderPane();
    Label p1 = new Label("A sudden shudder runs through the chamber.");
    p1.setWrapText(true);
    p1.setTextFill(Color.RED);
    p1.setFont(Font.font(16));
    overlay1.setCenter(p1);

    primaryStage.setScene(buildScene(bg1, overlay1, 1000, 700));

    PauseTransition wait1 = new PauseTransition(Duration.seconds(PAUSE_LONG)); // ~3s
    wait1.setOnFinished(ev1 -> {
        // Step 2: blackout for BLACKOUT_DEATH seconds (2s)
        showTemporaryBlackout(BLACKOUT_DEATH, () -> {
            // Step 3: show shadow.png explicitly for 3s (if present)
            String shadowImg = fileExists("shadows.png") ? "shadows.png" : null;
            ImageView bg2 = loadImageView(shadowImg);

            BorderPane overlay2 = new BorderPane();
            Label p2 = new Label("Shadows rise like a tide, swallowing every inch of light.");
            p2.setWrapText(true);
            p2.setTextFill(Color.web("#a303ff"));
            p2.setFont(Font.font(18));
            overlay2.setCenter(p2);

            primaryStage.setScene(buildScene(bg2, overlay2, 1000, 700));

            PauseTransition wait2 = new PauseTransition(Duration.seconds(PAUSE_LONG)); // ~3s
            wait2.setOnFinished(ev2 -> {
    String bgFinal = fileExists("mansion_out.jpg") ? "mansion_out.jpg" : null;
    ImageView bgFinalView = loadImageView(bgFinal);

    BorderPane finalOverlay = new BorderPane();
    Label endText = new Label(
        "The door never opens. The mansion exhales its last breath, and you are swallowed in silence...\n\n \t\t\t\t\t--- Ending: Death ---"
    );
    endText.setWrapText(true);
    endText.setTextFill(Color.web("#ff3333"));
    endText.setFont(Font.font("Serif", 20));

    // 🔹 Buttons styled like main menu
    Button retryBtn = new Button("  Retry  ");
    retryBtn.setOnAction(e -> {
        System.out.println("Retry from death scene."); // debug
        showTitleScreen(); // restart game
    });

    Button exitBtn = new Button("    Exit   ");
    exitBtn.setOnAction(e -> {
        System.out.println("Game exited from death scene."); // debug
        Platform.exit();
    });

    // Apply same style as menu buttons
    String style = "-fx-font-size: 14px; -fx-background-color: black; -fx-text-fill: white;";
    retryBtn.setStyle(style);
    exitBtn.setStyle(style);

    VBox vbox = new VBox(20, endText, retryBtn, exitBtn);
    vbox.setAlignment(Pos.CENTER);

    finalOverlay.setCenter(vbox);

    Scene deathScene = buildScene(bgFinalView, finalOverlay, 1000, 700);
    primaryStage.setScene(deathScene);
});

            wait2.play();
        });
    });
    wait1.play();
}

    /**
     * Escape sequence EXACT (per your instruction):
     * escape.jpg (3s) -> mansion_out.jpg (3s) -> final escape text + alert -> exit
     */
    private void showEscapeSequence() {
    // Step 1: escape.jpg or escape.png
    String escImg = fileExists("escape.jpg") ? "escape.jpg" : (fileExists("escape.png") ? "escape.png" : null);
    ImageView bg1 = loadImageView(escImg);

    BorderPane overlay1 = new BorderPane();
    Label t1 = new Label("Light floods the corridor. The heavy lock surrenders with an anguished groan.");
    t1.setWrapText(true);
    t1.setTextFill(Color.BLUEVIOLET);
    t1.setFont(Font.font(25));
    overlay1.setCenter(t1);

    primaryStage.setScene(buildScene(bg1, overlay1, 1000, 700));

    PauseTransition p1 = new PauseTransition(Duration.seconds(PAUSE_LONG)); // ~3s
    p1.setOnFinished(ev1 -> {
        // Step 2: mansion_out.jpg final scene
        ImageView bg2 = loadImageView("mansion_out.jpg");
        BorderPane overlay2 = new BorderPane();

        Label t2 = new Label(
            "You step out onto cold, wet grass. The mansion shrinks behind you.\n\n \t\t\t\t\t--- Ending: Escape ---"
        );
        t2.setWrapText(true);
        t2.setTextFill(Color.CORAL);
        t2.setFont(Font.font(16));

        // 🔹 Buttons (styled like main menu)
        Button escapeBtn = new Button("Escape");
        escapeBtn.setOnAction(e -> {
            System.out.println("Escape ending confirmed."); // debug
            Platform.exit();
        });

        Button exitBtn = new Button("Exit");
        exitBtn.setOnAction(e -> {
            System.out.println("Game exited from escape scene."); // debug
            Platform.exit();
        });

        // Apply consistent styling
        escapeBtn.setStyle("-fx-font-size: 14px; -fx-background-color: black; -fx-text-fill: white;");
        exitBtn.setStyle("-fx-font-size: 14px; -fx-background-color: black; -fx-text-fill: white;");

        VBox vbox = new VBox(20, t2, escapeBtn, exitBtn);
        vbox.setAlignment(Pos.CENTER);

        overlay2.setCenter(vbox);

        Scene finalScene = buildScene(bg2, overlay2, 1000, 700);
        primaryStage.setScene(finalScene);
    });
    p1.play();
}


    // -------------------- Reset --------------------
    private void resetGameState() {
        pendantNumber = 3;
        pendantTaken = false;
        ended = false;
        letters.clear();
        solved.clear();
        if (logArea != null) logArea.clear();
        updateInventoryUI();
    }
}
