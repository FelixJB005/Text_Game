/*
    CorridorAdventure.java
    ----------------------
    JavaFX game with detailed inline comments for every major element (buttons, sounds, scenes, transitions, helpers).

    NOTES:
      - Sound files expected under: C:/Java/Programe/sounds/
      - Images expected under:  C:/Java/Programe/images/
      - Use JavaFX module flags when compiling/running as you already do. :
                >>java --enable-native-access=javafx.graphics --module-path "C:\Java\javafx-sdk-25\lib" --add-modules javafx.controls,javafx.fxml,javafx.media -cp out CorridorAdventure
    Edited to include: extensive comments on buttons, sounds, scenes, transitions, helper utilities.
*/
/*
Project Resources
-----------------
Images (located in C:/Java/Programe/images/):
    ├─ title.jpg       <-- title screen image
    ├─ mansion_out.jpg <-- mansion exterior image
    ├─ mansion_in.jpg  <-- mansion interior image
    ├─ hallway.jpg     <-- hallway image
    ├─ room_1.jpg      <-- room 1 image
    ├─ storage.jpg     <-- storage room image
    ├─ study.jpg       <-- study room image
    ├─ exit.jpg        <-- exit door image
    ├─ escape.jpg      <-- escape ending image
    ├─ exit_2.jpg      <-- death ending image
    └─ shadow.png      <-- shadow image used in death sequence

Sounds (located in C:/Java/Programe/sounds/):
    ├─ sound_3.mp3  <-- "mansion_in" loop; also reused for blackout/puzzle-fails/lock-stanza
    └─ sound_7.mp3  <-- blackouts, puzzle-fails, and lock-stanza dedicated version
*/

import javafx.animation.PauseTransition;  // small delays / timed transitions between scenes
import javafx.animation.ScaleTransition;  // scaling animations for effects
import javafx.application.Application;    // JavaFX entry point class
import javafx.application.Platform;       // used to exit the app cleanly

import javafx.geometry.*;                 // layout helpers (Insets, Pos)
import javafx.scene.*;                    // core scene graph, nodes, controls, containers
import javafx.scene.image.*;              // load and display background images
import javafx.scene.layout.*;             // BorderPane, VBox/HBox, StackPane
import javafx.scene.paint.Color;          // overlay/text color
import javafx.scene.text.Font;            // fonts for labels and controls
import javafx.scene.text.FontWeight;      // bold/weight styles
import javafx.stage.Stage;                // main application window
import javafx.util.Duration;              // durations for PauseTransition

import javafx.scene.control.*;            // buttons, labels, inputs
import javafx.scene.effect.DropShadow;    // drop shadow effects
import javafx.scene.shape.Rectangle;      // used for overlays, masking, etc

import javafx.scene.media.Media;          // audio media file reference
import javafx.scene.media.MediaPlayer;    // audio playback control

import java.io.File;                      // check file presence (images/sounds)
import java.util.*;                       // lists, maps, random, etc


public class CorridorAdventure extends Application {

    // ------------------------
    // Timing constants
    // ------------------------
    private static final double PAUSE_SHORT = 1.0;       // small wait (in seconds)
    private static final double PAUSE_MED   = 2.0;       // medium wait (in seconds)
    private static final double PAUSE_LONG  = 3.0;       // long wait (in seconds)
    private static final double BLACKOUT_DEATH = 2.0;    // blackout duration used in the death sequence

    // Simple label reference used by some methods (kept as field for convenience)
    private Label desc;

    // ------------------------
    // Paths & stage
    // ------------------------
    private Stage primaryStage;                          // primary JavaFX Stage (main window)
    private final String IMAGE_PATH = "C:/Java/Programe/images/"; // folder for images (unchanged)
    private final String SOUND_PATH = "C:/Java/Programe/sounds/"; // folder for sounds (as requested)
    private String playerName = "";                      // player's entered name

    // ------------------------
    // Game state
    // ------------------------
    private boolean ended = false;                       // whether an ending was reached
    private Set<Character> letters = new LinkedHashSet<>(); // collected letters (O,C,K)
    private Map<String, Boolean> solved = new HashMap<>();  // solved flags per room

    // ------------------------
    // UI helpers
    // ------------------------
    private VBox inventoryBox;                           // inventory UI container (top-right)
    private TextArea logArea;                            // hidden debug/log area (not shown to player)

    // ------------------------
    // Pendant / lives state
    // ------------------------
    private boolean pendantTaken = false;                // whether pendant is picked up
    private int pendantNumber = 3;                       // pendant "life" count (decrements on failures)

    // ------------------------
    // Audio players (MediaPlayer objects)
    // ------------------------
    // sound1: title / credits / walkthrough / hallway / puzzle rooms (general area)
    // sound2: mansion_out (outside mansion)
    // sound3: mansion_in (only mansion interior loop)
    // sound4: south door attempt music
    // sound5: death sequence music
    // sound6: escape sequence music
    // sound7: blackouts, puzzle-fails, lock-stanza moments (new mapping)
    private MediaPlayer sound1Player;
    private MediaPlayer sound2Player;
    private MediaPlayer sound3Player; // mansion_in only
    private MediaPlayer sound4Player;
    private MediaPlayer sound5Player;
    private MediaPlayer sound6Player;
    private MediaPlayer sound7Player; // blackout/fail/stanza

    // ------------------------
    // -- Audio helper methods
    // ------------------------

    /**
     * createPlayerFromBase:
     *  - tries to create a MediaPlayer from SOUND_PATH using the base filename (e.g., "sound_1")
     *  - checks multiple common extensions (.mp3, .wav, .aac, .m4a) in that order
     *  - sets cycle count to indefinite when loop==true (looping background music)
     *  - returns null if no file found or creation fails
     *
     *  Important: JavaFX supports a set of codecs depending on platform; if a file fails to load,
     *  check the file format and confirm the JDK/OS supports it.
     */
    private MediaPlayer createPlayerFromBase(String baseName, boolean loop) {
        String[] exts = { ".mp3", ".wav", ".aac", ".m4a" };
        for (String ext : exts) {
            String fname = baseName + ext;
            File f = new File(SOUND_PATH + fname);
            if (f.exists()) {
                try {
                    Media media = new Media(f.toURI().toString()); // create Media from file URI
                    MediaPlayer mp = new MediaPlayer(media);
                    mp.setCycleCount(loop ? MediaPlayer.INDEFINITE : 1); // loop or play once
                    return mp;
                } catch (Exception ex) {
                    // If creating MediaPlayer fails — print error and continue trying other extensions
                    System.err.println("Failed to create MediaPlayer for " + fname + " : " + ex.getMessage());
                }
            }
        }
        return null; // none of the candidate files existed or were valid
    }

    /**
     * startLoop(idx)
     *  - convenience method to lazily initialize and start the MediaPlayer for a sound slot.
     *  - idx selects which sound (1..7) to start
     *  - method avoids re-creating players if they already exist (it resumes if paused/stopped)
     */
    private void startLoop(int idx) {
        switch (idx) {
            case 1:
                if (sound1Player != null) {
                    MediaPlayer.Status s = sound1Player.getStatus();
                    if (s != MediaPlayer.Status.PLAYING) sound1Player.play();
                    return;
                }
                sound1Player = createPlayerFromBase("sound_1", true);
                if (sound1Player != null) sound1Player.play();
                break;
            case 2:
                if (sound2Player != null) {
                    MediaPlayer.Status s = sound2Player.getStatus();
                    if (s != MediaPlayer.Status.PLAYING) sound2Player.play();
                    return;
                }
                sound2Player = createPlayerFromBase("sound_2", true);
                if (sound2Player != null) sound2Player.play();
                break;
            case 3:
                // sound_3 = mansion_in only (per user's request)
                if (sound3Player != null) {
                    MediaPlayer.Status s = sound3Player.getStatus();
                    if (s != MediaPlayer.Status.PLAYING) sound3Player.play();
                    return;
                }
                sound3Player = createPlayerFromBase("sound_3", true);
                if (sound3Player != null) sound3Player.play();
                break;
            case 4:
                if (sound4Player != null) {
                    MediaPlayer.Status s = sound4Player.getStatus();
                    if (s != MediaPlayer.Status.PLAYING) sound4Player.play();
                    return;
                }
                sound4Player = createPlayerFromBase("sound_4", true);
                if (sound4Player != null) sound4Player.play();
                break;
            case 5:
                if (sound5Player != null) {
                    MediaPlayer.Status s = sound5Player.getStatus();
                    if (s != MediaPlayer.Status.PLAYING) sound5Player.play();
                    return;
                }
                sound5Player = createPlayerFromBase("sound_5", true);
                if (sound5Player != null) sound5Player.play();
                break;
            case 6:
                if (sound6Player != null) {
                    MediaPlayer.Status s = sound6Player.getStatus();
                    if (s != MediaPlayer.Status.PLAYING) sound6Player.play();
                    return;
                }
                sound6Player = createPlayerFromBase("sound_6", true);
                if (sound6Player != null) sound6Player.play();
                break;
            case 7:
                // sound_7 = blackouts / puzzle-fails / lock-stanza (new mapping)
                if (sound7Player != null) {
                    MediaPlayer.Status s = sound7Player.getStatus();
                    if (s != MediaPlayer.Status.PLAYING) sound7Player.play();
                    return;
                }
                sound7Player = createPlayerFromBase("sound_7", true);
                if (sound7Player != null) sound7Player.play();
                break;
        }
    }

    /**
     * stopSound(idx)
     *  - stops, disposes, and nulls-out the MediaPlayer for the given sound slot index
     *  - disposing helps free native resources
     */
    private void stopSound(int idx) {
        try {
            switch (idx) {
                case 1:
                    if (sound1Player != null) { sound1Player.stop(); sound1Player.dispose(); sound1Player = null; }
                    break;
                case 2:
                    if (sound2Player != null) { sound2Player.stop(); sound2Player.dispose(); sound2Player = null; }
                    break;
                case 3:
                    if (sound3Player != null) { sound3Player.stop(); sound3Player.dispose(); sound3Player = null; }
                    break;
                case 4:
                    if (sound4Player != null) { sound4Player.stop(); sound4Player.dispose(); sound4Player = null; }
                    break;
                case 5:
                    if (sound5Player != null) { sound5Player.stop(); sound5Player.dispose(); sound5Player = null; }
                    break;
                case 6:
                    if (sound6Player != null) { sound6Player.stop(); sound6Player.dispose(); sound6Player = null; }
                    break;
                case 7:
                    if (sound7Player != null) { sound7Player.stop(); sound7Player.dispose(); sound7Player = null; }
                    break;
            }
        } catch (Exception ex) {
            // Media APIs sometimes throw on stopping/disposal — log but do not crash game
            System.err.println("Error stopping sound " + idx + " : " + ex.getMessage());
        }
    }

    

    // ------------------------
    // Main & entry
    // ------------------------
    public static void main(String[] args) {
        launch(args); // JavaFX launch; execution continues in start()
    }

    // ------------------------
    // Image helpers
    // ------------------------

    /**
     * loadImageView(fileName)
     *  - loads an ImageView from IMAGE_PATH + fileName
     *  - returns null if the file doesn't exist
     *  - preserves ratio disabled (fills background), smoothing enabled
     *
     *  Note: we purposely return null when file missing; callers handle fallbacks.
     */
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

    /**
     * fileExists(fileName)
     *  - convenience check for image files in IMAGE_PATH
     */
    private boolean fileExists(String fileName) {
        return fileName != null && new File(IMAGE_PATH + fileName).exists();
    }

    // ------------------------
    // UI styling helper
    // ------------------------

    /**
     * styleButton(b)
     *  - applies a unified visual style to game buttons (text color, background, padding, border)
     *  - keeps UI consistent across the scenes
     */
    private void styleButton(Button b) {
        b.setFont(Font.font(14));
        b.setStyle("-fx-background-color: rgba(0,0,0,0.75); -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-padding: 6 12 6 12; -fx-border-color: #666; -fx-border-width:1;");
    }

    // ------------------------
    // Blackout utility
    // ------------------------

    /**
     * showTemporaryBlackout(seconds, after)
     *  - creates a full-screen black scene for 'seconds' seconds, then runs 'after' Runnable
     *  - used for transitions that need a cinematic blackout
     */
    private void showTemporaryBlackout(double seconds, Runnable after) {
        // Use current stage dimensions to cover screen properly
        double width = primaryStage.getWidth() > 0 ? primaryStage.getWidth() : 1000;
        double height = primaryStage.getHeight() > 0 ? primaryStage.getHeight() : 700;

        StackPane black = new StackPane();
        black.setStyle("-fx-background-color: black;");          // plain black pane
        black.setPrefSize(width, height);

        Scene blackScene = new Scene(black, width, height);
        primaryStage.setScene(blackScene);

        PauseTransition p = new PauseTransition(Duration.seconds(seconds));
        p.setOnFinished(e -> after.run()); // call the continuation
        p.play();
    }

    // ------------------------
    // Scene builder
    // ------------------------

    /**
     * buildScene(bg, overlay, w, h)
     *  - composits a background image (ImageView) and overlay BorderPane into a Scene with a dark translucent mask
     *  - ensures background fits to scene size by binding fitWidth/fitHeight
     *  - overlay contains the actual UI controls and narrative text
     */
    private Scene buildScene(ImageView bg, BorderPane overlay, double w, double h) {
        StackPane root = new StackPane();
        root.setPrefSize(w, h);

        if (bg != null) root.getChildren().add(bg);
        else {
            // fallback: full-black region if no bg provided
            Region black = new Region();
            black.setStyle("-fx-background-color:black;");
            root.getChildren().add(black);
        }

        // translucent rectangle to dim background for readability
        Rectangle dark = new Rectangle();
        dark.setFill(Color.color(0, 0, 0, 0.35));
        dark.widthProperty().bind(root.widthProperty());
        dark.heightProperty().bind(root.heightProperty());
        root.getChildren().add(dark);

        // add overlay on top (contains controls)
        root.getChildren().add(overlay);
        Scene scene = new Scene(root, w, h);

        if (bg != null) {
            // make the bg scale with window size
            bg.fitWidthProperty().bind(scene.widthProperty());
            bg.fitHeightProperty().bind(scene.heightProperty());
        }
        return scene;
    }

    // ------------------------
    // Top bar & inventory
    // ------------------------

    /**
     * buildTopBar(showInventory, descLabel)
     *  - constructs the top portion of the UI with narrative label on left and optional inventory box on right
     *  - also lazily initializes the hidden log area (logArea) and inventoryBox
     *  - descLabel is provided by the caller (scene-specific narrative)
     */
    private VBox buildTopBar(boolean showInventory, Label descLabel) {
        // initialize hidden log area if needed
        if (logArea == null) {
            logArea = new TextArea();
            logArea.setEditable(false);
            logArea.setWrapText(true);
            logArea.setPrefRowCount(4);
            logArea.setFont(Font.font(13));
            // intentionally not added to the scene graph (hidden)
        }

        // initialize inventory container if needed
        if (inventoryBox == null) {
            inventoryBox = new VBox(4);
            inventoryBox.setPadding(new Insets(6));
            inventoryBox.setAlignment(Pos.TOP_RIGHT);
            inventoryBox.setStyle("-fx-background-color: rgba(0,0,0,0.45); -fx-border-color: #444; -fx-border-width: 1;");
        }

        // configure the narrative label (descLabel)
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
            updateInventoryUI(); // rebuild inventory contents to reflect game state
            topRow.getChildren().addAll(spacer, inventoryBox);
        }

        VBox container = new VBox(6);
        container.getChildren().add(topRow);
        container.setPadding(new Insets(6));
        return container;
    }

    /**
     * updateInventoryUI()
     *  - rebuilds the inventoryBox content according to pendantTaken and collected letters
     *  - exact formatting intentionally imitates user-specified layout
     */
    private void updateInventoryUI() {
        if (inventoryBox == null) return;
        inventoryBox.getChildren().clear();

        // static magnifying glass line (always present)
        Label line1 = new Label("1. Magnifying Glass");
        line1.setTextFill(Color.WHITE);
        line1.setFont(Font.font(13));

        // pendant line: shows either "not taken" or current lives
        Label line2 = new Label(
            pendantTaken
                ? ("2. Pendant (" + (pendantNumber >= 0 ? pendantNumber : 0) + ")         ")
                : "2. Pendant (not taken)"
        );
        line2.setTextFill(Color.WHITE);
        line2.setFont(Font.font(13));

        // the L is permanent (per user's spec), other letters show if collected
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

    // ------------------------
    // Prompt & logging utilities
    // ------------------------

    /**
     * promptForAnswer(title, question)
     *  - opens a modal TextInputDialog and returns the entered String, or null if cancelled
     *  - used by puzzle solving logic
     */
    private String promptForAnswer(String title, String question) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(question);
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    /**
     * appendText(text)
     *  - appends a line to the hidden logArea (internal debugging/history)
     */
    private void appendText(String text) {
        if (logArea == null) return;
        logArea.appendText(text + "\n");
    }

    /**
     * printLockStanza(number)
     *  - writes poetic lock-stanza lines into hidden log (used when pendant number changes)
     *  - also updates desc style if desc exists (visual hint in UI)
     */
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

    // ------------------------
    // Application start (entry point after launch())
    // ------------------------
    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("Corridor Adventure");
        showTitleScreen(); // first scene shown to player
    }

    // ------------------------
    // Scene: Title Screen
    // ------------------------

    /**
     * showTitleScreen()
     *  - constructs the title scene with name input and three buttons:
     *      -> Start  : stops sound_1 and proceeds to mansion_out
     *      -> Credits: shows credits screen (sound_1 continues)
     *      -> Walkthrough: shows walkthrough (sound_1 continues)
     *  - starts sound_1 (title/credits/walkthrough area music)
     */
    private void showTitleScreen() {
        ImageView bg = loadImageView("title.jpg");           // background image for title (optional)
        BorderPane overlay = new BorderPane();
        overlay.setPrefSize(1000, 700);

        // Prompt label (large & colored)
        Label prompt = new Label("Enter your name and step into the Case.");
        prompt.setTextFill(Color.BLUEVIOLET);
        prompt.setFont(Font.font(25));

        // Name input field (center)
        TextField nameField = new TextField();
        nameField.setPromptText("Enter name here");
        nameField.setMaxWidth(360);
        nameField.setFont(Font.font(15));

        // Buttons on the title screen
        Button startBtn = new Button("Start");
        Button creditsBtn = new Button("Credits");
        Button walkBtn = new Button("Walkthrough");

        // Apply consistent style to buttons
        styleButton(startBtn); styleButton(creditsBtn); styleButton(walkBtn);

        // Start button action:
        //  - stop sound_1 (title music) immediately as requested
        //  - set playerName (or default "Detective"), reset game state, then show mansion_out scene
        startBtn.setOnAction(e -> {
            stopSound(1); // stop title/area background
            playerName = nameField.getText().isEmpty() ? "Detective" : nameField.getText();
            resetGameState();
            appendText("You are " + playerName + ", a world-class detective who doesn't let mysteries rest.");
            showMansionOut();
        });

        // Credits and Walkthrough open their respective scenes; sound_1 remains playing there
        creditsBtn.setOnAction(e -> showCredits());
        walkBtn.setOnAction(e -> showWalkthrough());

        // Layout controls vertically in center
        VBox controls = new VBox(10, prompt, nameField, startBtn, creditsBtn, walkBtn);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(40, 0, 40, 0));
        overlay.setCenter(controls);

        Scene scene = buildScene(bg, overlay, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Start loop for sound_1 (title/credits/walkthrough & also hallway/puzzle rooms)
        startLoop(1);
    }

    // ------------------------
    // Scene: Credits
    // ------------------------

    /**
     * showCredits()
     *  - shows credits text
     *  - uses sound_1 (title-area music) — we call startLoop(1) to ensure it plays if not already
     */
    private void showCredits() {
        startLoop(1); // ensure title music is active when viewing credits

        BorderPane overlay = new BorderPane();
        overlay.setPrefSize(1000,700);

        // Credits label (wrapped)
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

        // Back button returns to title
        Button back = new Button("Back");
        styleButton(back);
        back.setOnAction(e -> showTitleScreen());
        overlay.setBottom(back);
        BorderPane.setAlignment(back, Pos.BOTTOM_CENTER);
        BorderPane.setMargin(back, new Insets(0,0,40,0));

        Scene s = buildScene(null, overlay, 1000,700);
        primaryStage.setScene(s);
    }

    // ------------------------
    // Scene: Walkthrough
    // ------------------------

    /**
     * showWalkthrough()
     *  - displays gameplay instructions; sound_1 continues playing
     */
    private void showWalkthrough() {
        startLoop(1); // ensure title music is active

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

    // ------------------------
    // Scene: Mansion Outside
    // ------------------------

    /**
     * showMansionOut()
     *  - mansion exterior description with a single "North" button that approaches the mansion
     *  - starts sound_2 (mansion outside loop)
     *  - pressing North stops sound_2 and opens mansion interior (showMansionIn)
     */
    private void showMansionOut() {
        ImageView bg = loadImageView("mansion_out.jpg");
        BorderPane overlay = new BorderPane();

        Label desc = new Label(
        playerName + " — a world-class detective. Rain beads on your collar and the house stares back with blind windows. " +
        "You have come following the trail of numerous missing cases — names and faces that never found their way home.\n\n" +
        "A magnifying glass sits in your pocket, a small, stubborn proof that you won't leave a question unanswered. " +
        "The mansion's stonework is pitted and patient; shutters rattle with the sighs of rooms that remember."
            );
        desc.setWrapText(true);
        desc.setTextFill(Color.BLUE);
        desc.setFont(Font.font("Serif", FontWeight.BOLD, 22));

        Node topBar = buildTopBar(true, desc); // top bar includes inventory
        overlay.setTop(topBar);
        BorderPane.setMargin(topBar, new Insets(8));

        // Single button to approach mansion (North)
        Button north = new Button("North (Approach Mansion)");
        styleButton(north);

        // On click: stop mansion_out music (sound_2) then open mansion interior
        north.setOnAction(e -> {
            stopSound(2); // ensure sound_2 stops when entering
            showMansionIn();
        });

        VBox center = new VBox(12, north);
        center.setAlignment(Pos.TOP_CENTER);
        center.setPadding(new Insets(40,12,12,12));
        overlay.setCenter(center);

        Scene s = buildScene(bg, overlay, 1000,700);
        primaryStage.setScene(s);

        // start sound_2 (mansion outside)
        startLoop(2);
    }

    // ------------------------
    // Scene: Mansion Interior (mansion_in)
    // ------------------------

    /**
     * showMansionIn()
     *  - interior description with Inspect Pendant and Take Pendant buttons
     *  - starts sound_3 (mansion_in loop) when the scene loads
     *  - pressing Take Pendant:
     *      - marks pendantTaken = true
     *      - stops sound_3 (mansion_in) as requested
     *      - waits 3s, then starts sound_7 for a blackout moment, shows blackout, stops sound_7, then goes to hallway
     *
     *  Note: sound_3 now strictly represents mansion_in loop only.
     */
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

        // Buttons for inspect & take pendant
        Button inspect = new Button("Inspect Pendant");
        Button take = new Button("Take Pendant");
        styleButton(inspect); styleButton(take);

        // Start mansion_in music (sound_3) when interior loads
        startLoop(3);

        // Inspect: updates description & hidden log but doesn't change audio state
        inspect.setOnAction(e -> {
            appendText("You study the pendant: the back bears a carved 'L'. The front is a dial currently reading " + pendantNumber + ".");
            desc.setText("You study the pendant: the back bears a carved 'L'. The front is a dial currently reading " + pendantNumber + ".");
        });

        // Take: pick up pendant, stop mansion_in audio (sound_3), then blackout sequence with sound_7
        take.setOnAction(e -> {
            if (!pendantTaken) {
                pendantTaken = true;
                updateInventoryUI();

                // textual & UI feedback for taking pendant
                desc.setText("As you take and admire the Pendant a sharp pain hits Your skull.\n \t You clutch Your head, but everything fades and You awaken in a Dark Corridor.");
                desc.setTextFill(Color.BLUEVIOLET);
                desc.setFont(Font.font("System", 20));

                appendText("As you take and admire the Pendant a sharp pain hits Your skull.\n \t You clutch Your head, but everything fades and You awaken in a Dark Corridor.");

                // Stop mansion_in loop immediately (user requested)
                stopSound(3);

                // Short pause before blackout starts so the text is seen by player
                PauseTransition pause = new PauseTransition(Duration.seconds(3));
                pause.setOnFinished(ev -> {
                    // For the blackout, start sound_7 (blackout/stanza/fail audio)
                    startLoop(7);

                    // showTemporaryBlackout will show a black screen for PAUSE_MED seconds
                    // After blackout completes we stop sound_7 and show the hallway
                    showTemporaryBlackout(PAUSE_MED, () -> {
                        stopSound(7);
                        showHallway();
                    });
                });
                pause.play();
            }
        });

        HBox bottom = new HBox(14, inspect, take);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(10));
        overlay.setBottom(bottom);
        BorderPane.setAlignment(bottom, Pos.BOTTOM_CENTER);

        Scene s = buildScene(bg, overlay, 1000,700);
        primaryStage.setScene(s);
    }

    // ------------------------
    // Scene: Hallway
    // ------------------------

    /**
     * showHallway()
     *  - corridor with navigation buttons for Room 1 (North), Storage (East), Study (West), and Exit (South)
     *  - ensures general area music (sound_1) is playing while in hallway and puzzle rooms
     *  - pressing South stops sound_1 and invokes trySouthDoor()
     */
    private void showHallway() {
        ImageView bg = loadImageView("hallway.jpg");
        BorderPane overlay = new BorderPane();

        Label desc = new Label(
                "A narrow corridor stretches out, lit by a single flickering bulb. Doors await north, east, and west. " +
                        "South, a heavy iron door bristles with ancient locks."
        );
        desc.setTextFill(Color.rgb(161, 3, 252, 1.0));
        overlay.setTop(buildTopBar(true, desc));
        BorderPane.setMargin(overlay.getTop(), new Insets(8));

        // Four navigation buttons
        Button north = new Button("North (Room 1)");
        Button east = new Button("East (Storage)");
        Button west = new Button("West (Study)");
        Button south = new Button("South (Exit)");
        styleButton(north); styleButton(east); styleButton(west); styleButton(south);

        // Wire buttons to their scenes
        north.setOnAction(e -> showPuzzleRoom("Room 1"));
        east.setOnAction(e -> showPuzzleRoom("Storage"));
        west.setOnAction(e -> showPuzzleRoom("Study"));

        // South behavior: stop general area music (sound_1) then attempt door
        south.setOnAction(e -> {
            stopSound(1); // per request: close music loop when pressing South(exit)
            trySouthDoor();
        });

        // Layout the directional buttons into the overlay
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

        updateInventoryUI(); // refresh inventory to current state

        Scene s = buildScene(bg, overlay, 1000,700);
        primaryStage.setScene(s);

        // Ensure the general area music (sound_1) is playing in hallway/puzzle rooms
        startLoop(1);
    }

    // ------------------------
    // South door attempt (Exit)
    // ------------------------

    /**
     * trySouthDoor()
     *  - shows an attempt scene when player presses South on hallway
     *  - uses sound_4 during the attempt (south door music)
     *  - if player has O,C,K -> stop sound_4 and call showEscapeSequence()
     *  - if not -> start sound_7 for lock-stanza/fail audio, decrement pendantNumber, update inventory UI
     *      -> if pendantNumber <= 0: stop sounds and run death sequence
     *      -> otherwise: stop temp sounds and return to hallway after a PauseTransition
     */
    private void trySouthDoor() {
        // Ensure sound_1 (general area) is stopped (caller already did this; keep safety)
        stopSound(1);

        // Start south-door-specific music (sound_4)
        startLoop(4);

        // Candidate backgrounds: exit.jpg preferred, fallback to mansion_out.jpg if none
        String[] candidates = { "exit.jpg", "exit.png", "mansion_out.jpg" };
        String chosen = null;
        for (String c : candidates) {
            if (fileExists(c)) { chosen = c; break; }
        }
        ImageView bg = chosen != null ? loadImageView(chosen) : null;

        // Transparent overlay (so we only see bg and centered text)
        BorderPane overlay = new BorderPane();
        overlay.setStyle("-fx-background-color: transparent;");

        // Primary attempt message label (center)
        Label attemptText = new Label("You press your hand to the heavy southern door and pull. It resists with centuries of rust.");
        attemptText.setWrapText(true);
        attemptText.setFont(Font.font(20));
        attemptText.setMaxWidth(600);
        attemptText.setAlignment(Pos.CENTER);
        attemptText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        attemptText.setStyle("-fx-text-fill: #B7410E; -fx-background-color: transparent;"); // rusty text

        StackPane centerPane = new StackPane(attemptText);
        centerPane.setAlignment(Pos.CENTER);
        centerPane.setStyle("-fx-background-color: transparent;");
        overlay.setCenter(centerPane);

        Scene s = buildScene(bg, overlay, 1000, 700);
        primaryStage.setScene(s);

        // Short pause to simulate the door attempt cinematic
        PauseTransition pause = new PauseTransition(Duration.seconds(PAUSE_MED));
        pause.setOnFinished(ev -> {
            if (letters.contains('O') && letters.contains('C') && letters.contains('K')) {
                // Player has all letters -> success path
                stopSound(4); // stop south-door music before escaping
                appendText("You assemble the letters and press them into the dial. The mechanism accepts them with a shudder.");
                showEscapeSequence();
            } else {
                // Failure path -> change displayed text, start lock-stanza audio (sound_7)
                attemptText.setText("Shadows creep along the lock's seam, a cold breath exhaling from the iron. The mechanism resists.");
                attemptText.setStyle("-fx-text-fill: red; -fx-font-size: 15px; -fx-background-color: transparent;");


                // Put stanza text into the label (visual) and into the hidden log (printLockStanzaLocal)
                printLockStanzaLocal(pendantNumber, attemptText);

                // decrement pendant lives and update inventory UI
                pendantNumber--;
                updateInventoryUI();

                if (pendantNumber <= 0) {
                    // player dead: stop temporary sounds and begin death sequence
                    stopSound(4);
                    showDeathSequence();
                } else {
                    // Not dead yet: after a small pause stop the temp sounds and return to hallway
                    PauseTransition p2 = new PauseTransition(Duration.seconds(PAUSE_MED));
                    p2.setOnFinished(ev2 -> {
                        stopSound(4);
                        showHallway();
                    });
                    p2.play();
                }
            }
        });
        pause.play();
    }

    /**
     * printLockStanzaLocal(number, target)
     *  - builds a readable stanza string for the on-screen label showing the pendant's "ticking" flavor text
     *  - this is a local variant used to display the stanza visually (printLockStanza writes to hidden log)
     */
    private void printLockStanzaLocal(int number, Label target) {
        target.setStyle("-fx-font-weight: bold; -fx-background-color: transparent;");
        target.setTextFill(Color.RED);

        StringBuilder sb = new StringBuilder();
        sb.append("The cursed Pendant begins ticking....\n\t\t\t\t\t\t\tThe number shifts: ").append(number-1).append("\n\n");

        if (number == 3) sb.append("\tA shadow flickers at the edge of your vision.\n");
        if (number == 2) sb.append("\tYou hear footsteps behind you, drawing closer...\n");
        if (number == 1) sb.append("A whisper breathes your name. Your skin turns cold.\n");
        if (number <= 0) {
            sb.append("The lock clicks to zero.\n");
            sb.append("The shadows rise like a tide, swallowing every inch of light.\n");
            sb.append("You are trapped eternally, consumed by the darkness...\n");
        }

        target.setText(sb.toString());
        target.setWrapText(true);
        target.setAlignment(Pos.CENTER);
        target.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
    }

    // ------------------------
    // Scene: Puzzle Room
    // ------------------------

    /**
     * showPuzzleRoom(roomName)
     *  - generic puzzle room UI used for Room 1, Storage, Study
     *  - includes Inspect, Solve, Back buttons
     *  - starts sound_1 (general area music) while inside
     */
    private void showPuzzleRoom(String roomName) {
        ImageView bg = loadImageView(roomBackgroundFor(roomName));
        BorderPane overlay = new BorderPane();

        Label desc = new Label(roomDescription(roomName));
        desc.setWrapText(true);
        desc.setTextFill(Color.WHITE);
        desc.setFont(Font.font(15));
        overlay.setTop(buildTopBar(true, desc));
        BorderPane.setMargin(overlay.getTop(), new Insets(6));

        // Buttons: Inspect (gives hint), Solve (prompts user), Back (return to hallway)
        Button inspect = new Button("Inspect Room");
        Button solve = new Button("Solve");
        Button back = new Button("Back");
        styleButton(inspect); styleButton(solve); styleButton(back);

        // Ensure general area music plays in puzzle rooms
        startLoop(1);

        // Inspect adds descriptive hints to the room label and hidden log
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

        // Solve routes to doSolve which handles puzzle answers & failure behavior
        solve.setOnAction(e -> doSolve(roomName, desc));
        back.setOnAction(e -> showHallway()); // Back returns to hallway

        HBox bottom = new HBox(12, inspect, solve, back);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(10));
        overlay.setBottom(bottom);

        Scene s = buildScene(bg, overlay, 1000,700);
        primaryStage.setScene(s);
    }

    // Helper: human-readable room descriptions (unchanged)
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

    // Helper: selects background image filename for each room
    private String roomBackgroundFor(String room) {
        switch (room) {
            case "Room 1": return "room_1.jpg";
            case "Storage": return "storage.jpg";
            case "Study": return "study.jpg";
            default: return "storage.jpg";
        }
    }

    // ------------------------
    // Puzzle solving logic
    // ------------------------

    /**
     * doSolve(room, desc)
     *  - prompts the user with a puzzle-specific question and checks the answer
     *  - correct answers add letters to inventory and update UI
     *  - incorrect answers call failPuzzleAndReturn which handles decrementing pendantNumber and audio/visual feedback
     */
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

    // ------------------------
    // Fail handling
    // ------------------------

    /**
     * failPuzzleAndReturn(room, desc)
     *  - decrements pendantNumber on wrong answers (but clamps at 0)
     *  - updates the UI, prints stanza to hidden log via printLockStanza
     *  - plays sound_7 (puzzle-fail/stanza audio) for the failure pause duration, then returns to same room
     *  - if lives drop to 0, invokes showDeathSequence (stops relevant audio first)
     */
    private void failPuzzleAndReturn(String room, Label desc) {
        // decrement lives on failure, clamp at 0
        pendantNumber = Math.max(pendantNumber - 1, 0);

        // immediate UI update
        updateInventoryUI();

        // add stanza into hidden log
        printLockStanza(pendantNumber);

        if (pendantNumber <= 0) {
            // death path: stop area & stanza music then show death
            stopSound(1);
            stopSound(7); // ensure stanza/fail sound stopped
            stopSound(4);
            showDeathSequence();
            return;
        }

        // show short failure message centered over room background
        String message = "The pendant grows colder. You steel yourself to try again.";
        appendText("A chill runs through you as the puzzle slips away...");

        String bgFile = roomBackgroundFor(room);
        ImageView bg = (bgFile != null && fileExists(bgFile)) ? loadImageView(bgFile) : null;

        BorderPane overlay = new BorderPane();
        overlay.setStyle("-fx-background-color: transparent;");

        Label centerMsg = new Label(message);
        centerMsg.setWrapText(true);
        centerMsg.setTextFill(Color.RED);
        centerMsg.setFont(Font.font(18));
        centerMsg.setMaxWidth(600);
        centerMsg.setAlignment(Pos.CENTER);
        centerMsg.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        centerMsg.setStyle("-fx-font-weight: bold; -fx-background-color: transparent;");

        StackPane centerPane = new StackPane(centerMsg);
        centerPane.setAlignment(Pos.CENTER);
        centerPane.setStyle("-fx-background-color: transparent;");
        overlay.setCenter(centerPane);

        Scene s = buildScene(bg, overlay, 1000, 700);
        primaryStage.setScene(s);

        // Play sound_7 for puzzle-fail moment
        startLoop(7);

        // After a pause, stop the fail audio and return to the same puzzle room
        PauseTransition p = new PauseTransition(Duration.seconds(PAUSE_MED));
        p.setOnFinished(ev -> {
            stopSound(7);
            showPuzzleRoom(room);
        });
        p.play();
    }

    // ------------------------
    // Death sequence
    // ------------------------

    /**
     * showDeathSequence()
     *  - EXACT sequence as requested:
     *      1) show exit_2.jpg (or shadow.png fallback) for ~3s
     *      2) blackout for BLACKOUT_DEATH seconds
     *      3) show shadow.png scene for ~3s (start death music sound_5 at this stage)
     *      4) final death text + Retry/Exit buttons; stop death music when final screen is displayed
     *
     *  - sound_5 is used to score the "shadows" + death stages (starts at shadow stage).
     */
    private void showDeathSequence() {
        // Step 1: show exit_2.jpg (fallback to shadow.png)
        String firstImg = fileExists("exit_2.jpg") ? "exit_2.jpg" : (fileExists("shadow.png") ? "shadow.png" : null);
        ImageView bg1 = loadImageView(firstImg);

        BorderPane overlay1 = new BorderPane();
        Label p1 = new Label("A sudden shudder runs through the chamber.");
        p1.setWrapText(true);
        p1.setTextFill(Color.RED);
        p1.setFont(Font.font(16));
        overlay1.setCenter(p1);

        primaryStage.setScene(buildScene(bg1, overlay1, 1000, 700));

        // Wait ~3s on first image, then blackout stage
        PauseTransition wait1 = new PauseTransition(Duration.seconds(PAUSE_LONG));
        wait1.setOnFinished(ev1 -> {
            // Blackout stage (no death music yet)
            showTemporaryBlackout(BLACKOUT_DEATH, () -> {
                // Step 3: show shadow.png explicitly for ~3s and start death music (sound_5)
                String shadowImg = fileExists("shadows.png") ? "shadows.png" : null;
                ImageView bg2 = loadImageView(shadowImg);

                BorderPane overlay2 = new BorderPane();
                Label p2 = new Label("Shadows rise like a tide, swallowing every inch of light.");
                p2.setWrapText(true);
                p2.setTextFill(Color.web("#a303ff"));
                p2.setFont(Font.font(18));
                overlay2.setCenter(p2);

                // Start death sequence music (sound_5) at the shadow stage
                startLoop(5);

                primaryStage.setScene(buildScene(bg2, overlay2, 1000, 700));

                // Wait ~3s, then show final death screen (stop death music when final screen appears)
                PauseTransition wait2 = new PauseTransition(Duration.seconds(PAUSE_LONG));
                wait2.setOnFinished(ev2 -> {
                    // Final death scene background (mansion_out if present)
                    String bgFinal = fileExists("mansion_out.jpg") ? "mansion_out.jpg" : null;
                    ImageView bgFinalView = loadImageView(bgFinal);

                    BorderPane finalOverlay = new BorderPane();
                    Label endText = new Label(
                        "The door never opens. The mansion exhales its last breath, and you are swallowed in silence...\n\n \t\t\t\t\t\t\t--- Ending: Death ---"
                    );
                    endText.setWrapText(true);
                    endText.setTextFill(Color.web("#ff3333"));
                    endText.setFont(Font.font("Serif", 20));

                    // Stop death music now that final death screen is shown
                    stopSound(5);

                    // Retry & Exit buttons on the final death screen
                    Button retryBtn = new Button("Retry");
                    retryBtn.setOnAction(e -> {
                        System.out.println("Retry from death scene.");
                        showTitleScreen();
                    });

                    Button exitBtn = new Button("    Exit   ");
                    exitBtn.setOnAction(e -> {
                        System.out.println("Game exited from death scene.");
                        Platform.exit();
                    });

                    // Button styling and hover effects (cosmetic)
                    String style = "-fx-font-size: 14px;"
                             + "-fx-background-color: black;"
                             + "-fx-text-fill: white;"
                             + "-fx-font-weight: bold;"
                             + "-fx-padding: 6 12 6 12;"
                             + "-fx-border-color: #666;"
                             + "-fx-border-width: 1;"
                             + "-fx-background-radius: 6;"
                             + "-fx-border-radius: 6;";
                    retryBtn.setStyle(style);
                    exitBtn.setStyle(style);

                    DropShadow ds = new DropShadow();
                    ds.setRadius(6);
                    ds.setOffsetY(2);
                    ds.setColor(Color.color(0, 0, 0, 0.5));
                    retryBtn.setEffect(ds);
                    exitBtn.setEffect(ds);

                    double hoverScale = 0.96;
                    Duration dur = Duration.millis(120);

                    ScaleTransition stEnterRetry = new ScaleTransition(dur, retryBtn);
                    stEnterRetry.setToX(hoverScale);
                    stEnterRetry.setToY(hoverScale);
                    ScaleTransition stExitRetry = new ScaleTransition(dur, retryBtn);
                    stExitRetry.setToX(1.0);
                    stExitRetry.setToY(1.0);
                    retryBtn.setOnMouseEntered(e -> { stExitRetry.stop(); stEnterRetry.playFromStart(); });
                    retryBtn.setOnMouseExited(e -> { stEnterRetry.stop(); stExitRetry.playFromStart(); });

                    ScaleTransition stEnterExit = new ScaleTransition(dur, exitBtn);
                    stEnterExit.setToX(hoverScale);
                    stEnterExit.setToY(hoverScale);
                    ScaleTransition stExitExit = new ScaleTransition(dur, exitBtn);
                    stExitExit.setToX(1.0);
                    stExitExit.setToY(1.0);
                    exitBtn.setOnMouseEntered(e -> { stExitExit.stop(); stEnterExit.playFromStart(); });
                    exitBtn.setOnMouseExited(e -> { stEnterExit.stop(); stExitExit.playFromStart(); });

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

    // ------------------------
    // Escape sequence
    // ------------------------

    /**
     * showEscapeSequence()
     *  - plays escape cinematic:
     *      1) escape.jpg for ~3s (sound_6 starts at beginning of sequence)
     *      2) mansion_out.jpg for ~3s (stops sound_6 prior to final screen)
     *      3) final escape text + Restart/Escape buttons
     *  - sound_6 is intended to play across the cinematic and is stopped when final escape screen is shown
     */
    private void showEscapeSequence() {
        // Step 1 background
        String escImg = fileExists("escape.jpg") ? "escape.jpg" : (fileExists("escape.png") ? "escape.png" : null);
        ImageView bg1 = loadImageView(escImg);

        BorderPane overlay1 = new BorderPane();
        Label t1 = new Label("Light floods the corridor. The heavy lock surrenders with an anguished groan.");
        t1.setWrapText(true);
        t1.setTextFill(Color.BLUEVIOLET);
        t1.setFont(Font.font(25));
        overlay1.setCenter(t1);

        primaryStage.setScene(buildScene(bg1, overlay1, 1000, 700));

        // Start escape music (sound_6)
        startLoop(6);

        PauseTransition p1 = new PauseTransition(Duration.seconds(PAUSE_LONG)); // ~3s
        p1.setOnFinished(ev1 -> {
            // Step 2: show mansion_out.jpg and final escape text/buttons
            ImageView bg2 = loadImageView("mansion_out.jpg");
            BorderPane overlay2 = new BorderPane();

            Label t2 = new Label(
                "You step out onto cold, wet grass. The mansion shrinks behind you.\n\n\t\t\t\t\t--- Ending: Escape ---"
            );
            t2.setWrapText(true);
            t2.setTextFill(Color.CORAL);
            t2.setFont(Font.font(16));

            // Stop escape music now that final escape screen is shown
            stopSound(6);

            // Buttons for restart or exit (cosmetic + functional)
            Button restartBtn = new Button(" Restart ");
            restartBtn.setOnAction(e -> showTitleScreen());

            Button exitBtn = new Button("      Escape      ");
            exitBtn.setOnAction(e -> Platform.exit());

            String style = "-fx-background-color: rgba(0,0,0,0.75);"
                 + "-fx-text-fill: white;"
                 + "-fx-font-weight: bold;"
                 + "-fx-padding: 6 12 6 12;"
                 + "-fx-border-color: #666;"
                 + "-fx-border-width:1;";
            restartBtn.setStyle(style);
            exitBtn.setStyle(style);

            DropShadow ds = new DropShadow();
            ds.setRadius(6);
            ds.setOffsetY(2);
            ds.setColor(Color.color(0, 0, 0, 0.5));
            restartBtn.setEffect(ds);
            exitBtn.setEffect(ds);

            double hoverScale = 0.96;
            Duration dur = Duration.millis(120);

            ScaleTransition stEnterR = new ScaleTransition(dur, restartBtn);
            stEnterR.setToX(hoverScale);
            stEnterR.setToY(hoverScale);

            ScaleTransition stExitR = new ScaleTransition(dur, restartBtn);
            stExitR.setToX(1.0);
            stExitR.setToY(1.0);

            restartBtn.setOnMouseEntered(e -> { stExitR.stop(); stEnterR.playFromStart(); });
            restartBtn.setOnMouseExited(e -> { stEnterR.stop(); stExitR.playFromStart(); });

            ScaleTransition stEnterE = new ScaleTransition(dur, exitBtn);
            stEnterE.setToX(hoverScale);
            stEnterE.setToY(hoverScale);

            ScaleTransition stExitE = new ScaleTransition(dur, exitBtn);
            stExitE.setToX(1.0);
            stExitE.setToY(1.0);

            exitBtn.setOnMouseEntered(e -> { stExitE.stop(); stEnterE.playFromStart(); });
            exitBtn.setOnMouseExited(e -> { stEnterE.stop(); stExitE.playFromStart(); });

            VBox vbox = new VBox(20, t2, restartBtn, exitBtn);
            vbox.setAlignment(Pos.CENTER);
            overlay2.setCenter(vbox);

            Scene finalScene = buildScene(bg2, overlay2, 1000, 700);
            primaryStage.setScene(finalScene);
        });
        p1.play();
    }

    // ------------------------
    // Reset state utility
    // ------------------------

    /**
     * resetGameState()
     *  - resets global game variables when starting a new playthrough
     *  - leaves sound state alone (caller decides which sounds to start/stop)
     */
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
