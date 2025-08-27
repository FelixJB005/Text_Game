package programmes;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.Optional;

public class CorridorEscapeChronicles extends Application {

    private Stage stage;
    private TextArea output;
    private VBox actionsBox;

    private String playerName = "Detective";
    private boolean testerMode = false;

    private boolean relicMansion = false;
    private boolean relicTrain = false;

    private boolean m_clue1 = false;
    private boolean m_clue2 = false;
    private boolean m_clue3 = false;
    private boolean m_clue4 = false;

    private boolean t_clue1 = false;
    private boolean t_clue2 = false;
    private boolean t_clue3 = false;

    private boolean fRoman = false;
    private boolean fCipher = false;
    private boolean fBinary = false;
    private boolean fRiddle = false;
    private boolean combined = false;

    private enum Mission { NONE, MANSION, TRAIN }
    private Mission currentMission = Mission.NONE;

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;

        output = new TextArea();
        output.setEditable(false);
        output.setWrapText(true);
        output.setFont(Font.font("Monospaced", 13));
        output.setPrefRowCount(18);

        actionsBox = new VBox(8);
        actionsBox.setPadding(new Insets(10));
        actionsBox.setPrefWidth(300);

        Label topInfo = new Label("Corridor Escape Chronicles");
        topInfo.setFont(Font.font("Monospaced", 18));
        topInfo.setPadding(new Insets(6));

        HBox bottom = new HBox(10);
        bottom.setPadding(new Insets(8));
        bottom.setAlignment(Pos.CENTER_LEFT);

        Button restartBtn = new Button("Restart");
        restartBtn.setOnAction(e -> restart());

        Button quitBtn = new Button("Quit");
        quitBtn.setOnAction(e -> stage.close());

        bottom.getChildren().addAll(restartBtn, quitBtn);

        BorderPane root = new BorderPane();
        root.setTop(topInfo);
        root.setCenter(output);
        root.setRight(actionsBox);
        root.setBottom(bottom);
        BorderPane.setMargin(output, new Insets(8));
        BorderPane.setMargin(actionsBox, new Insets(8));

        Scene scene = new Scene(root, 1000, 560);
        stage.setTitle("Corridor Escape Chronicles");
        stage.setScene(scene);
        stage.show();

        showIntroThenName();
    }

    // --------------------------- Intro & Setup ---------------------------
    private void showIntroThenName() {
        String dragon =
                "               / ___====-_  _-====___ \\  \n" +
                "           _--^^^#####//      \\\\#####^^^--_\n" +
                "        _-^##########// (    ) \\\\##########^-_\n" +
                "       -############//  |\\\\^^/|  \\\\############-\n" +
                "     _/############//   (o  o)   \\\\############\\\\_  \n" +
                "    /#############((     \\\\__/     ))#############\\\\\n" +
                "   -###############\\\\    |  /\\\\   //###############- \n" +
                "  -#################\\\\  / UUU \\\\  //#################-\n" +
                " -###################\\\\/  (__)  \\\\//###################-\n" +
                "-####################(          )####################-\n" +
                "-#####################\\\\        /#####################-\n" +
                "-###################   \\\\  __  /   ###################-\n" +
                "-#################      ( |  | )      #################-\n" +
                "-###############        ( |  | )        ###############-\n" +
                " -###########           ( |  | )             ###########- \n" +
                "   -######               ( | | )                 ######- \n" +
                "     -##                   |  |                   ##-  \n" +
                "                        [__ >_< __]\n\n";

        output.setText(dragon + "Welcome to Corridor Escape Chronicles.\n");

        TextInputDialog nameDialog = new TextInputDialog("");
        nameDialog.setTitle("Welcome");
        nameDialog.setHeaderText("Enter your name");
        nameDialog.setContentText("Name:");
        Optional<String> nameRes = nameDialog.showAndWait();
        if (nameRes.isPresent() && !nameRes.get().trim().isEmpty()) {
            playerName = nameRes.get().trim();
        } else {
            playerName = "Detective";
        }

        Alert testerAlert = new Alert(Alert.AlertType.CONFIRMATION);
        testerAlert.setTitle("Mode");
        testerAlert.setHeaderText("Are you a tester? (Tester mode skips puzzles)");
        testerAlert.setContentText("Select Yes for Tester, No for Player.");
        ButtonType yes = new ButtonType("Yes");
        ButtonType no = new ButtonType("No");
        testerAlert.getButtonTypes().setAll(yes, no);
        Optional<ButtonType> testerRes = testerAlert.showAndWait();
        testerMode = testerRes.isPresent() && testerRes.get() == yes;

        Alert walkAlert = new Alert(Alert.AlertType.CONFIRMATION);
        walkAlert.setTitle("Walkthrough");
        walkAlert.setHeaderText("Do you want a concise walkthrough and plots?");
        walkAlert.setContentText("Yes shows a short guide and plots; No skips it.");
        walkAlert.getButtonTypes().setAll(yes, no);
        Optional<ButtonType> walkRes = walkAlert.showAndWait();
        if (walkRes.isPresent() && walkRes.get() == yes) {
            showWalkthroughDialog();
        }

        ChoiceDialog<String> missionDlg = new ChoiceDialog<>("Old Mansion", "Old Mansion", "Train Mission");
        missionDlg.setTitle("Choose Mission");
        missionDlg.setHeaderText("Choose your mission, " + playerName);
        missionDlg.setContentText("Mission:");
        Optional<String> missionRes = missionDlg.showAndWait();
        if (missionRes.isPresent()) {
            String choice = missionRes.get();
            if (choice.equals("Train Mission")) {
                currentMission = Mission.TRAIN;
                startTrainMission();
            } else {
                currentMission = Mission.MANSION;
                startMansionMission();
            }
        } else {
            currentMission = Mission.MANSION;
            startMansionMission();
        }
    }

    private void showWalkthroughDialog() {
        String walk =
                "CONCISE WALKTHROUGH & PLOTS\n\n" +
                "Short game flow (golden path):\n" +
                "1) Play a mission (Old Mansion or Train Mission) — each has 4 puzzles + final reveal.\n" +
                "2) Earn that mission's relic (Mansion = Pendant, Train = Key).\n" +
                "3) Return to the Corridor Hub and enter the Final Corridor.\n" +
                "4) Solve 4 final mini-puzzles (Roman, Caesar, Binary, Riddle), combine clues => 'LOCK', then Exit.\n\n" +
                "PLOT (Old Mansion) - quick:\n" +
                "A missing old man, a pendant, scattered journal scraps, a locked drawer and a basement box. Solve puzzles to expose the culprit.\n\n" +
                "PLOT (Train Mission) - quick:\n" +
                "A passenger vanished on a midnight train. Search Dining -> Sleeper -> Luggage -> Engine. Accuse correctly to save them.\n\n" +
                "Tester mode: if enabled, puzzles auto-complete so you can test scenes quickly.\n\n" +
                "Controls: Buttons appear with clear labels. Use hints when stuck.\n";
        TextArea ta = new TextArea(walk);
        ta.setWrapText(true);
        ta.setEditable(false);
        ta.setPrefSize(600, 360);
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Walkthrough & Plots");
        a.getDialogPane().setContent(ta);
        a.showAndWait();
    }

    // --------------------------- Mansion Mission ---------------------------
    private void startMansionMission() {
        output.appendText("\n--- OLD MANSION ---\nYou step into the mansion foyer. Solve a chain of puzzles to find the culprit.\n");
        actionsBox.getChildren().clear();
        actionsBox.getChildren().addAll(
                makeButton("Foyer: Roman puzzle", this::mansionFoyer),
                makeButton("Library: Assemble scraps", this::mansionLibrary),
                makeButton("Study: Riddle", this::mansionStudy),
                makeButton("Basement: Lockbox", this::mansionBasement),
                makeButton("Confront & Claim Relic", this::mansionConfront),
                makeButton("Return to Corridor Hub", this::returnToHub)
        );
        if (testerMode) autoCompleteMansion();
    }

    private void mansionFoyer() {
        if (m_clue1) { say("You already solved the Roman puzzle here."); return; }
        TextInputDialog d = new TextInputDialog("");
        d.setTitle("Foyer - Roman");
        d.setHeaderText("What is X + V (enter Roman numerals)?");
        d.setContentText("Answer:");
        Optional<String> ans = d.showAndWait();
        if (!ans.isPresent()) { say("Cancelled."); return; }
        String s = ans.get().trim();
        if (s.equalsIgnoreCase("XV")) {
            m_clue1 = true;
            say("Correct. You find a scratched letter 'L' in the margin.");
        } else {
            say("Wrong. Hint: 10 + 5.");
        }
    }

    private void mansionLibrary() {
        if (m_clue2) { say("You already assembled the scraps here."); return; }
        TextInputDialog d = new TextInputDialog("");
        d.setTitle("Library - Scraps");
        d.setHeaderText("Unscramble to form the phrase (type exactly): HIDES IN PLAIN SIGHT");
        d.setContentText("Phrase:");
        Optional<String> ans = d.showAndWait();
        if (!ans.isPresent()) { say("Cancelled."); return; }
        if (ans.get().trim().equalsIgnoreCase("HIDES IN PLAIN SIGHT")) {
            m_clue2 = true;
            say("Correct. A margin note reads: 'E'.");
        } else {
            say("Not matching. Use exact phrase.");
        }
    }

    private void mansionStudy() {
        if (m_clue3) { say("Riddle already solved here."); return; }
        TextInputDialog d = new TextInputDialog("");
        d.setTitle("Study - Riddle");
        d.setHeaderText("Riddle: I am taken before you're born, I follow after you die. What am I?");
        d.setContentText("Answer (one word):");
        Optional<String> ans = d.showAndWait();
        if (!ans.isPresent()) { say("Cancelled."); return; }
        String a = ans.get().trim().toLowerCase();
        if (a.equals("name") || a.equals("a name")) {
            m_clue3 = true;
            say("Correct. The drawer opens: letter 'O' on a smudged opener.");
        } else {
            say("Incorrect. Think what you get at birth and keep forever.");
        }
    }

    private void mansionBasement() {
        if (m_clue4) { say("You already opened the lockbox here."); return; }
        TextInputDialog d = new TextInputDialog("");
        d.setTitle("Basement - Lockbox");
        d.setHeaderText("Enter the 4-letter code (hint from clues):");
        d.setContentText("Code:");
        Optional<String> ans = d.showAndWait();
        if (!ans.isPresent()) { say("Cancelled."); return; }
        if (ans.get().trim().equalsIgnoreCase("LOCK")) {
            m_clue4 = true;
            say("Lockbox opens. You find a pendant — enough to identify the culprit.");
        } else {
            say("Wrong. Use the letters you've found across the mansion.");
        }
    }

    private void mansionConfront() {
        if (m_clue1 && m_clue2 && m_clue3 && m_clue4) {
            relicMansion = true;
            say("You confront the suspect with proof. Case closed. You obtain the Mansion Relic.");
            returnToHub();
        } else {
            say("You lack evidence. Make sure you've solved all mansion puzzles.");
        }
    }

    private void autoCompleteMansion() {
        m_clue1 = m_clue2 = m_clue3 = m_clue4 = true;
        relicMansion = true;
        say("[Tester] Mansion puzzles auto-completed. Mansion Relic obtained.");
    }

    // --------------------------- Train Mission ---------------------------
    private void startTrainMission() {
        output.appendText("\n--- TRAIN MISSION ---\nA passenger vanished on the Midnight Express. Search the cars and accuse correctly.\n");
        actionsBox.getChildren().clear();
        actionsBox.getChildren().addAll(
                makeButton("Dining Car: Cipher", this::trainDining),
                makeButton("Sleeper Car: Binary", this::trainSleeper),
                makeButton("Luggage Car: Riddle", this::trainLuggage),
                makeButton("Engine Room: Accuse", this::trainEngine),
                makeButton("Return to Corridor Hub", this::returnToHub)
        );
        if (testerMode) autoCompleteTrain();
    }

    private void trainDining() {
        if (t_clue1) { say("Dining cipher already solved."); return; }
        TextInputDialog d = new TextInputDialog("");
        d.setTitle("Dining - Cipher");
        d.setHeaderText("Decode (Caesar shift 1 back): MPQF");
        d.setContentText("Answer:");
        Optional<String> ans = d.showAndWait();
        if (!ans.isPresent()) { say("Cancelled."); return; }
        if (ans.get().trim().equalsIgnoreCase("lope")) {
            t_clue1 = true;
            say("Correct. You find a napkin with numbers and a partial initial.");
        } else {
            say("Incorrect. Shift each letter back by one.");
        }
    }

    private void trainSleeper() {
        if (t_clue2) { say("Binary trunk already decoded."); return; }
        TextInputDialog d = new TextInputDialog("");
        d.setTitle("Sleeper - Binary");
        d.setHeaderText("Translate 01001100 to ASCII (single letter):");
        d.setContentText("Answer:");
        Optional<String> ans = d.showAndWait();
        if (!ans.isPresent()) { say("Cancelled."); return; }
        if (ans.get().trim().equalsIgnoreCase("L")) {
            t_clue2 = true;
            say("Correct. The trunk contains a riddle.");
        } else {
            say("Wrong. Convert to decimal (76) then ASCII.");
        }
    }

    private void trainLuggage() {
        if (t_clue3) { say("Luggage riddle already solved."); return; }
        TextInputDialog d = new TextInputDialog("");
        d.setTitle("Luggage - Riddle");
        d.setHeaderText("Riddle: I speak without a mouth and hear without ears. What am I?");
        d.setContentText("Answer (one word):");
        Optional<String> ans = d.showAndWait();
        if (!ans.isPresent()) { say("Cancelled."); return; }
        if (ans.get().trim().equalsIgnoreCase("echo")) {
            t_clue3 = true;
            say("Correct. You collect notes to confront the culprit.");
        } else {
            say("No. Think of a mountain's trick.");
        }
    }

    private void trainEngine() {
        if (!(t_clue1 && t_clue2 && t_clue3)) {
            say("You don't have enough evidence. Search the Dining, Sleeper and Luggage cars first.");
            return;
        }
        ChoiceDialog<String> accuse = new ChoiceDialog<>("Conductor", "Conductor", "Porter");
        accuse.setTitle("Engine - Accuse");
        accuse.setHeaderText("Who do you accuse?");
        accuse.setContentText("Choose suspect:");
        Optional<String> pick = accuse.showAndWait();
        if (!pick.isPresent()) { say("Accusation cancelled."); return; }
        String suspect = pick.get();
        if (suspect.equals("Conductor")) {
            relicTrain = true;
            say("You accused the Conductor. He confesses. Train Relic obtained.");
            returnToHub();
        } else {
            say("Porter has an alibi. The train slows too late; the truth slips away. Try another path.");
        }
    }

    private void autoCompleteTrain() {
        t_clue1 = t_clue2 = t_clue3 = true;
        relicTrain = true;
        say("[Tester] Train puzzles auto-completed. Train Relic obtained.");
    }

    // --------------------------- Corridor Hub & Final Corridor ---------------------------
    private void returnToHub() {
        currentMission = Mission.NONE;
        combined = false;
        output.appendText("\n--- Corridor Hub ---\n");
        output.appendText("Progress: Mansion relic: " + (relicMansion ? "Obtained" : "Not yet") +
                " | Train relic: " + (relicTrain ? "Obtained" : "Not yet") + "\n");
        actionsBox.getChildren().clear();
        actionsBox.getChildren().addAll(
                makeButton("Enter Final Corridor", this::enterFinalCorridor),
                makeButton("Play Old Mansion", () -> {
                    currentMission = Mission.MANSION;
                    startMansionMission();
                }),
                makeButton("Play Train Mission", () -> {
                    currentMission = Mission.TRAIN;
                    startTrainMission();
                }),
                makeButton("Show Walkthrough", this::showWalkthroughDialog)
        );
    }

    private void enterFinalCorridor() {
        if (!relicMansion && !relicTrain && !testerMode) {
            say("Final Corridor locked. Finish at least one mission.");
            return;
        }
        output.appendText("\n--- Final Corridor ---\nCollect the four clues (Room1..Room4) then combine them to form the passkey.\n");
        actionsBox.getChildren().clear();
        actionsBox.getChildren().addAll(
                makeButton("Room1 - Roman (X+V)", this::finalRoom1),
                makeButton("Room2 - Caesar (MPQF)", this::finalRoom2),
                makeButton("Room3 - Binary (01001100)", this::finalRoom3),
                makeButton("Room4 - Riddle (echo)", this::finalRoom4),
                makeButton("Combine clues", this::combineFinal),
                makeButton("Exit Door", this::finalExit),
                makeButton("Return to Hub", this::returnToHub)
        );
        if (testerMode) {
            fRoman = fCipher = fBinary = fRiddle = true;
            combined = true;
            say("[Tester] Final clues auto-collected. Combine -> LOCK.");
        }
    }

    private void finalRoom1() {
        if (fRoman) { say("You already have the Roman clue."); return; }
        TextInputDialog d = new TextInputDialog("");
        d.setTitle("Final Room1");
        d.setHeaderText("What is X + V (Roman)?");
        d.setContentText("Answer:");
        Optional<String> a = d.showAndWait();
        if (!a.isPresent()) { say("Cancelled."); return; }
        if (a.get().trim().equalsIgnoreCase("XV")) {
            fRoman = true;
            say("Roman clue collected.");
        } else {
            say("Wrong. Hint: 10 + 5.");
        }
    }

    private void finalRoom2() {
        if (fCipher) { say("You already have the Cipher clue."); return; }
        TextInputDialog d = new TextInputDialog("");
        d.setTitle("Final Room2");
        d.setHeaderText("Decode MPQF (shift back 1):");
        d.setContentText("Answer:");
        Optional<String> a = d.showAndWait();
        if (!a.isPresent()) { say("Cancelled."); return; }
        if (a.get().trim().equalsIgnoreCase("lope")) {
            fCipher = true;
            say("Cipher clue collected.");
        } else {
            say("Wrong. Shift back by 1.");
        }
    }

    private void finalRoom3() {
        if (fBinary) { say("You already have the Binary clue."); return; }
        TextInputDialog d = new TextInputDialog("");
        d.setTitle("Final Room3");
        d.setHeaderText("01001100 in ASCII = ?");
        d.setContentText("Answer:");
        Optional<String> a = d.showAndWait();
        if (!a.isPresent()) { say("Cancelled."); return; }
        if (a.get().trim().equalsIgnoreCase("L")) {
            fBinary = true;
            say("Binary clue collected.");
        } else {
            say("Wrong. Hint: decimal 76 -> ASCII.");
        }
    }

    private void finalRoom4() {
        if (fRiddle) { say("You already have the Riddle clue."); return; }
        TextInputDialog d = new TextInputDialog("");
        d.setTitle("Final Room4");
        d.setHeaderText("I speak without a mouth and hear without ears. What am I?");
        d.setContentText("Answer:");
        Optional<String> a = d.showAndWait();
        if (!a.isPresent()) { say("Cancelled."); return; }
        if (a.get().trim().equalsIgnoreCase("echo")) {
            fRiddle = true;
            say("Riddle clue collected.");
        } else {
            say("Try again. Think mountains.");
        }
    }

    private void combineFinal() {
        if (fRoman && fCipher && fBinary && fRiddle) {
            combined = true;
            say("You piece the four clues together… The passkey is: LOCK");
        } else {
            say("You don't have all four clues yet.");
        }
    }

    private void finalExit() {
        if (!combined && !testerMode) {
            say("Combine the clues first.");
            return;
        }
        if (testerMode) {
            output.appendText("\n[Tester] Bypassing passkey. === ESCAPE ACHIEVED ===\nThe door opens. Pale light pours in. You made it out, " + playerName + ".\n");
            actionsBox.getChildren().clear();
            actionsBox.getChildren().addAll(makeButton("Restart Game", this::restart), makeButton("Quit", () -> stage.close()));
            return;
        }
        TextInputDialog d = new TextInputDialog("");
        d.setTitle("Exit Keypad");
        d.setHeaderText("Enter the passkey to exit:");
        d.setContentText("Passkey:");
        Optional<String> a = d.showAndWait();
        if (!a.isPresent()) { say("Cancelled."); return; }
        if (a.get().trim().equalsIgnoreCase("LOCK")) {
            output.appendText("\n=== ESCAPE ACHIEVED ===\nThe door opens. Pale light pours in. You made it out, " + playerName + ".\n");
            actionsBox.getChildren().clear();
            actionsBox.getChildren().addAll(makeButton("Restart Game", this::restart), makeButton("Quit", () -> stage.close()));
        } else {
            say("Wrong passkey.");
        }
    }

    // --------------------------- Utilities ---------------------------
    private Button makeButton(String label, Runnable r) {
        Button b = new Button(label);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setOnAction(e -> r.run());
        return b;
    }

    private void say(String text) {
        output.appendText(text + "\n");
    }

    private void restart() {
        playerName = "Detective";
        testerMode = false;
        relicMansion = relicTrain = false;
        m_clue1 = m_clue2 = m_clue3 = m_clue4 = false;
        t_clue1 = t_clue2 = t_clue3 = false;
        fRoman = fCipher = fBinary = fRiddle = combined = false;
        currentMission = Mission.NONE;
        output.clear();
        showIntroThenName();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

