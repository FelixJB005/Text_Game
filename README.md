// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA). import java.util.Optional; import javafx.application.Application; import javafx.geometry.Insets; import javafx.geometry.Pos; import javafx.scene.Node; import javafx.scene.Scene; import javafx.scene.control.Alert; import javafx.scene.control.Button; import javafx.scene.control.ButtonType; import javafx.scene.control.ChoiceDialog; import javafx.scene.control.Label; import javafx.scene.control.TextArea; import javafx.scene.control.TextInputDialog; import javafx.scene.control.Alert.AlertType; import javafx.scene.layout.BorderPane; import javafx.scene.layout.HBox; import javafx.scene.layout.VBox; import javafx.scene.text.Font; import javafx.stage.Stage;

public class CorridorEscapeChronicles extends Application { private Stage stage; private TextArea output; private VBox actionsBox; private String playerName = "Detective"; private boolean testerMode = false; private boolean relicMansion = false; private boolean relicTrain = false; private boolean m_clue1 = false; private boolean m_clue2 = false; private boolean m_clue3 = false; private boolean m_clue4 = false; private boolean t_clue1 = false; private boolean t_clue2 = false; private boolean t_clue3 = false; private boolean fRoman = false; private boolean fCipher = false; private boolean fBinary = false; private boolean fRiddle = false; private boolean combined = false; private CorridorEscapeChronicles$Mission currentMission;

public CorridorEscapeChronicles() { this.currentMission = CorridorEscapeChronicles.Mission.NONE; }

public void start(Stage primaryStage) { this.stage = primaryStage; this.output = new TextArea(); this.output.setEditable(false); this.output.setWrapText(true); this.output.setFont(Font.font("Monospaced", 13.0)); this.output.setPrefRowCount(18); this.actionsBox = new VBox(8.0); this.actionsBox.setPadding(new Insets(10.0)); this.actionsBox.setPrefWidth(300.0); Label topInfo = new Label("Corridor Escape Chronicles"); topInfo.setFont(Font.font("Monospaced", 18.0)); topInfo.setPadding(new Insets(6.0)); HBox bottom = new HBox(10.0); bottom.setPadding(new Insets(8.0)); bottom.setAlignment(Pos.CENTER_LEFT); Button restartBtn = new Button("Restart"); restartBtn.setOnAction((e) -> { this.restart(); }); Button quitBtn = new Button("Quit"); quitBtn.setOnAction((e) -> { this.stage.close(); }); bottom.getChildren().addAll(new Node[]{restartBtn, quitBtn}); BorderPane root = new BorderPane(); root.setTop(topInfo); root.setCenter(this.output); root.setRight(this.actionsBox); root.setBottom(bottom); BorderPane.setMargin(this.output, new Insets(8.0)); BorderPane.setMargin(this.actionsBox, new Insets(8.0)); Scene scene = new Scene(root, 1000.0, 560.0); this.stage.setTitle("Corridor Escape Chronicles"); this.stage.setScene(scene); this.stage.show(); this.showIntroThenName(); }

private void showIntroThenName() { String dragon = " / ====- -==== \ \n --^^^#####// \\#####^^^--\n -^##########// ( ) \\##########^-\n -############// |\\^^/| \\############-\n /############// (o o) \\############\\ \n /#############(( \\/ ))#############\\\n -###############\\ | | //###############- \n -#################\\ / \\ //#################-\n -###################\\/ () \\//###################-\n-####################( )####################-\n-#####################\\ /#####################-\n-################### \\ __ / ###################-\n-################# ( | | ) #################-\n-############### ( | | ) ###############-\n -########### ( | | ) ###########- \n -###### ( | | ) ######- \n -## | | ##- \n [__ >_< __]\n\n"; this.output.setText(dragon + "Welcome to Corridor Escape Chronicles.\n"); TextInputDialog nameDialog = new TextInputDialog(""); nameDialog.setTitle("Welcome"); nameDialog.setHeaderText("Enter your name"); nameDialog.setContentText("Name:"); Optional nameRes = nameDialog.showAndWait(); if (nameRes.isPresent() && !((String)nameRes.get()).trim().isEmpty()) { this.playerName = ((String)nameRes.get()).trim(); } else { this.playerName = "Detective"; }

  Alert testerAlert = new Alert(AlertType.CONFIRMATION);
  testerAlert.setTitle("Mode");
  testerAlert.setHeaderText("Are you a tester? (Tester mode skips puzzles)");
  testerAlert.setContentText("Select Yes for Tester, No for Player.");
  ButtonType yes = new ButtonType("Yes");
  ButtonType no = new ButtonType("No");
  testerAlert.getButtonTypes().setAll(new ButtonType[]{yes, no});
  Optional<ButtonType> testerRes = testerAlert.showAndWait();
  this.testerMode = testerRes.isPresent() && testerRes.get() == yes;
  Alert walkAlert = new Alert(AlertType.CONFIRMATION);
  walkAlert.setTitle("Walkthrough");
  walkAlert.setHeaderText("Do you want a concise walkthrough and plots?");
  walkAlert.setContentText("Yes shows a short guide and plots; No skips it.");
  walkAlert.getButtonTypes().setAll(new ButtonType[]{yes, no});
  Optional<ButtonType> walkRes = walkAlert.showAndWait();
  if (walkRes.isPresent() && walkRes.get() == yes) {
     this.showWalkthroughDialog();
  }

  ChoiceDialog<String> missionDlg = new ChoiceDialog("Old Mansion", new String[]{"Old Mansion", "Train Mission"});
  missionDlg.setTitle("Choose Mission");
  missionDlg.setHeaderText("Choose your mission, " + this.playerName);
  missionDlg.setContentText("Mission:");
  Optional<String> missionRes = missionDlg.showAndWait();
  if (missionRes.isPresent()) {
     String choice = (String)missionRes.get();
     if (choice.equals("Train Mission")) {
        this.currentMission = CorridorEscapeChronicles.Mission.TRAIN;
        this.startTrainMission();
     } else {
        this.currentMission = CorridorEscapeChronicles.Mission.MANSION;
        this.startMansionMission();
     }
  } else {
     this.currentMission = CorridorEscapeChronicles.Mission.MANSION;
     this.startMansionMission();
  }
}

private void showWalkthroughDialog() { String walk = "CONCISE WALKTHROUGH & PLOTS\n\nShort game flow (golden path):\n1) Play a mission (Old Mansion or Train Mission) — each has 4 puzzles + final reveal.\n2) Earn that mission's relic (Mansion = Pendant, Train = Key).\n3) Return to the Corridor Hub and enter the Final Corridor.\n4) Solve 4 final mini-puzzles (Roman, Caesar, Binary, Riddle), combine clues => 'LOCK', then Exit.\n\nPLOT (Old Mansion) - quick:\nA missing old man, a pendant, scattered journal scraps, a locked drawer and a basement box. Solve puzzles to expose the culprit.\n\nPLOT (Train Mission) - quick:\nA passenger vanished on a midnight train. Search Dining -> Sleeper -> Luggage -> Engine. Accuse correctly to save them.\n\nTester mode: if enabled, puzzles auto-complete so you can test scenes quickly.\n\nControls: Buttons appear with clear labels. Use hints when stuck.\n"; TextArea ta = new TextArea(walk); ta.setWrapText(true); ta.setEditable(false); ta.setPrefSize(600.0, 360.0); Alert a = new Alert(AlertType.INFORMATION); a.setTitle("Walkthrough & Plots"); a.getDialogPane().setContent(ta); a.showAndWait(); }

private void startMansionMission() { this.currentMission = CorridorEscapeChronicles.Mission.MANSION; this.output.appendText("\n--- OLD MANSION ---\nYou step into the mansion foyer. Solve a chain of puzzles to find the culprit.\n"); this.actionsBox.getChildren().clear(); this.actionsBox.getChildren().addAll(new Node[]{this.makeButton("Foyer: Roman puzzle", this::mansionFoyer), this.makeButton("Library: Assemble scraps", this::mansionLibrary), this.makeButton("Study: Riddle", this::mansionStudy), this.makeButton("Basement: Lockbox", this::mansionBasement), this.makeButton("Confront & Claim Relic", this::mansionConfront), this.makeButton("Return to Corridor Hub", this::returnToHub)}); if (this.testerMode) { this.autoCompleteMansion(); }

}

private void mansionFoyer() { if (this.m_clue1) { this.say("You already solved the Roman puzzle here."); } else { TextInputDialog d = new TextInputDialog(""); d.setTitle("Foyer - Roman"); d.setHeaderText("What is X + V (enter Roman numerals)?"); d.setContentText("Answer:"); Optional ans = d.showAndWait(); if (!ans.isPresent()) { this.say("Cancelled."); } else { if (((String)ans.get()).trim().equalsIgnoreCase("XV")) { this.m_clue1 = true; this.say("Correct. You find a scratched letter 'L' in the margin."); } else { this.say("Wrong. Hint: 10 + 5."); }

     }
  }
}

private void mansionLibrary() { if (this.m_clue2) { this.say("You already assembled the scraps here."); } else { TextInputDialog d = new TextInputDialog(""); d.setTitle("Library - Scraps"); d.setHeaderText("Unscramble to form the phrase (type exactly): HIDES IN PLAIN SIGHT"); d.setContentText("Phrase:"); Optional ans = d.showAndWait(); if (!ans.isPresent()) { this.say("Cancelled."); } else { if (((String)ans.get()).trim().equalsIgnoreCase("HIDES IN PLAIN SIGHT")) { this.m_clue2 = true; this.say("Correct. A margin note reads: 'E'."); } else { this.say("Not matching. Use exact phrase."); }

     }
  }
}

private void mansionStudy() { if (this.m_clue3) { this.say("Riddle already solved here."); } else { TextInputDialog d = new TextInputDialog(""); d.setTitle("Study - Riddle"); d.setHeaderText("Riddle: I am taken before you're born, I follow after you die. What am I?"); d.setContentText("Answer (one word):"); Optional ans = d.showAndWait(); if (!ans.isPresent()) { this.say("Cancelled."); } else { String a = ((String)ans.get()).trim().toLowerCase(); if (!a.equals("name") && !a.equals("a name")) { this.say("Incorrect. Think what you get at birth and keep forever."); } else { this.m_clue3 = true; this.say("Correct. The drawer opens: letter 'O' on a smudged opener."); }

     }
  }
}

private void mansionBasement() { if (this.m_clue4) { this.say("You already opened the lockbox here."); } else { TextInputDialog d = new TextInputDialog(""); d.setTitle("Basement - Lockbox"); d.setHeaderText("Enter the 4-letter code (hint from clues):"); d.setContentText("Code:"); Optional ans = d.showAndWait(); if (!ans.isPresent()) { this.say("Cancelled."); } else { if (((String)ans.get()).trim().equalsIgnoreCase("LOCK")) { this.m_clue4 = true; this.say("Lockbox opens. You find a pendant — enough to identify the culprit."); } else { this.say("Wrong. Use the letters you've found across the mansion."); }

     }
  }
}

private void mansionConfront() { if (this.m_clue1 && this.m_clue2 && this.m_clue3 && this.m_clue4) { this.relicMansion = true; this.say("You confront the suspect with proof. Case closed. You obtain the Mansion Relic."); this.returnToHub(); } else { this.say("You lack evidence. Make sure you've solved all mansion puzzles."); }

}

private void autoCompleteMansion() { this.m_clue1 = this.m_clue2 = this.m_clue3 = this.m_clue4 = true; this.relicMansion = true; this.say("[Tester] Mansion puzzles auto-completed. Mansion Relic obtained."); }

private void startTrainMission() { this.currentMission = CorridorEscapeChronicles.Mission.TRAIN; this.output.appendText("\n--- TRAIN MISSION ---\nA passenger vanished on the Midnight Express. Search the cars and accuse correctly.\n"); this.actionsBox.getChildren().clear(); this.actionsBox.getChildren().addAll(new Node[]{this.makeButton("Dining Car: Cipher", this::trainDining), this.makeButton("Sleeper Car: Binary", this::trainSleeper), this.makeButton("Luggage Car: Riddle", this::trainLuggage), this.makeButton("Engine Room: Accuse", this::trainEngine), this.makeButton("Return to Corridor Hub", this::returnToHub)}); if (this.testerMode) { this.autoCompleteTrain(); }

}

private void trainDining() { if (this.t_clue1) { this.say("Dining cipher already solved."); } else { TextInputDialog d = new TextInputDialog(""); d.setTitle("Dining - Cipher"); d.setHeaderText("Decode (Caesar shift 1 back): MPQF"); d.setContentText("Answer:"); Optional ans = d.showAndWait(); if (!ans.isPresent()) { this.say("Cancelled."); } else { if (((String)ans.get()).trim().equalsIgnoreCase("lope")) { this.t_clue1 = true; this.say("Correct. You find a napkin with numbers and a partial initial."); } else { this.say("Incorrect. Shift each letter back by one."); }

     }
  }
}

private void trainSleeper() { if (this.t_clue2) { this.say("Binary trunk already decoded."); } else { TextInputDialog d = new TextInputDialog(""); d.setTitle("Sleeper - Binary"); d.setHeaderText("Translate 01001100 to ASCII (single letter):"); d.setContentText("Answer:"); Optional ans = d.showAndWait(); if (!ans.isPresent()) { this.say("Cancelled."); } else { if (((String)ans.get()).trim().equalsIgnoreCase("L")) { this.t_clue2 = true; this.say("Correct. The trunk contains a riddle."); } else { this.say("Wrong. Convert to decimal (76) then ASCII."); }

     }
  }
}

private void trainLuggage() { if (this.t_clue3) { this.say("Luggage riddle already solved."); } else { TextInputDialog d = new TextInputDialog(""); d.setTitle("Luggage - Riddle"); d.setHeaderText("Riddle: I speak without a mouth and hear without ears. What am I?"); d.setContentText("Answer (one word):"); Optional ans = d.showAndWait(); if (!ans.isPresent()) { this.say("Cancelled."); } else { if (((String)ans.get()).trim().equalsIgnoreCase("echo")) { this.t_clue3 = true; this.say("Correct. You collect notes to confront the culprit."); } else { this.say("No. Think of a mountain's trick."); }

     }
  }
}

private void trainEngine() { if (this.t_clue1 && this.t_clue2 && this.t_clue3) { ChoiceDialog accuse = new ChoiceDialog("Conductor", new String[]{"Conductor", "Porter"}); accuse.setTitle("Engine - Accuse"); accuse.setHeaderText("Who do you accuse?"); accuse.setContentText("Choose suspect:"); Optional pick = accuse.showAndWait(); if (!pick.isPresent()) { this.say("Accusation cancelled."); } else { String suspect = (String)pick.get(); if (suspect.equals("Conductor")) { this.relicTrain = true; this.say("You accused the Conductor. He confesses. Train Relic obtained."); this.returnToHub(); } else { this.say("Porter has an alibi. The train slows too late; the truth slips away. Try another path."); }

     }
  } else {
     this.say("You don't have enough evidence. Search the Dining, Sleeper and Luggage cars first.");
  }
}

private void autoCompleteTrain() { this.t_clue1 = this.t_clue2 = this.t_clue3 = true; this.relicTrain = true; this.say("[Tester] Train puzzles auto-completed. Train Relic obtained."); }

private void returnToHub() { this.combined = false; this.output.appendText("\n--- Corridor Hub ---\n"); String var10001 = this.relicMansion ? "Obtained" : "Not yet"; this.output.appendText("Progress: Mansion relic: " + var10001 + " | Train relic: " + (this.relicTrain ? "Obtained" : "Not yet") + "\n"); this.actionsBox.getChildren().clear(); switch (this.currentMission) { case MANSION: this.output.appendText("You return from the Mansion mission.\n"); break; case TRAIN: this.output.appendText("You return from the Train mission.\n"); break; default: this.output.appendText("Choose your next mission.\n"); }

  this.actionsBox.getChildren().addAll(new Node[]{this.makeButton("Enter Final Corridor", this::enterFinalCorridor), this.makeButton("Play Old Mansion", () -> {
     this.startMansionMission();
  }), this.makeButton("Play Train Mission", () -> {
     this.startTrainMission();
  }), this.makeButton("Show Walkthrough", this::showWalkthroughDialog)});
  this.currentMission = CorridorEscapeChronicles.Mission.NONE;
}

private void enterFinalCorridor() { if (!this.relicMansion && !this.relicTrain && !this.testerMode) { this.say("Final Corridor locked. Finish at least one mission."); } else { this.output.appendText("\n--- Final Corridor ---\nCollect the four clues (Room1..Room4) then combine them to form the passkey.\n"); this.actionsBox.getChildren().clear(); this.actionsBox.getChildren().addAll(new Node[]{this.makeButton("Room1 - Roman (X+V)", this::finalRoom1), this.makeButton("Room2 - Caesar (MPQF)", this::finalRoom2), this.makeButton("Room3 - Binary (01001100)", this::finalRoom3), this.makeButton("Room4 - Riddle (echo)", this::finalRoom4), this.makeButton("Combine clues", this::combineFinal), this.makeButton("Exit Door", this::finalExit), this.makeButton("Return to Hub", this::returnToHub)}); if (this.testerMode) { this.fRoman = this.fCipher = this.fBinary = this.fRiddle = true; this.combined = true; this.say("[Tester] Final clues auto-collected. Combine -> LOCK."); }

  }
}

private void finalRoom1() { if (this.fRoman) { this.say("You already have the Roman clue."); } else { TextInputDialog d = new TextInputDialog(""); d.setTitle("Final Room1"); d.setHeaderText("What is X + V (Roman)?"); d.setContentText("Answer:"); Optional a = d.showAndWait(); if (!a.isPresent()) { this.say("Cancelled."); } else { if (((String)a.get()).trim().equalsIgnoreCase("XV")) { this.fRoman = true; this.say("Roman clue collected."); } else { this.say("Wrong. Hint: 10 + 5."); }

     }
  }
}

private void finalRoom2() { if (this.fCipher) { this.say("You already have the Cipher clue."); } else { TextInputDialog d = new TextInputDialog(""); d.setTitle("Final Room2"); d.setHeaderText("Decode MPQF (shift back 1):"); d.setContentText("Answer:"); Optional a = d.showAndWait(); if (!a.isPresent()) { this.say("Cancelled."); } else { if (((String)a.get()).trim().equalsIgnoreCase("lope")) { this.fCipher = true; this.say("Cipher clue collected."); } else { this.say("Wrong. Shift back by 1."); }

     }
  }
}

private void finalRoom3() { if (this.fBinary) { this.say("You already have the Binary clue."); } else { TextInputDialog d = new TextInputDialog(""); d.setTitle("Final Room3"); d.setHeaderText("01001100 in ASCII = ?"); d.setContentText("Answer:"); Optional a = d.showAndWait(); if (!a.isPresent()) { this.say("Cancelled."); } else { if (((String)a.get()).trim().equalsIgnoreCase("L")) { this.fBinary = true; this.say("Binary clue collected."); } else { this.say("Wrong. Hint: decimal 76 -> ASCII."); }

     }
  }
}

private void finalRoom4() { if (this.fRiddle) { this.say("You already have the Riddle clue."); } else { TextInputDialog d = new TextInputDialog(""); d.setTitle("Final Room4"); d.setHeaderText("I speak without a mouth and hear without ears. What am I?"); d.setContentText("Answer:"); Optional a = d.showAndWait(); if (!a.isPresent()) { this.say("Cancelled."); } else { if (((String)a.get()).trim().equalsIgnoreCase("echo")) { this.fRiddle = true; this.say("Riddle clue collected."); } else { this.say("Try again. Think mountains."); }

     }
  }
}

private void combineFinal() { if (this.fRoman && this.fCipher && this.fBinary && this.fRiddle) { this.combined = true; this.say("You piece the four clues together… The passkey is: LOCK"); } else { this.say("You don't have all four clues yet."); }

}

private void finalExit() { if (!this.combined && !this.testerMode) { this.say("Combine the clues first."); } else if (this.testerMode) { this.output.appendText("\n[Tester] Bypassing passkey. === ESCAPE ACHIEVED ===\nThe door opens. Pale light pours in. You made it out, " + this.playerName + ".\n"); this.actionsBox.getChildren().clear(); this.actionsBox.getChildren().addAll(new Node[]{this.makeButton("Restart Game", this::restart), this.makeButton("Quit", () -> { this.stage.close(); })}); } else { TextInputDialog d = new TextInputDialog(""); d.setTitle("Exit Keypad"); d.setHeaderText("Enter the passkey to exit:"); d.setContentText("Passkey:"); Optional a = d.showAndWait(); if (!a.isPresent()) { this.say("Cancelled."); } else { if (((String)a.get()).trim().equalsIgnoreCase("LOCK")) { this.output.appendText("\n=== ESCAPE ACHIEVED ===\nThe door opens. Pale light pours in. You made it out, " + this.playerName + ".\n"); this.actionsBox.getChildren().clear(); this.actionsBox.getChildren().addAll(new Node[]{this.makeButton("Restart Game", this::restart), this.makeButton("Quit", () -> { this.stage.close(); })}); } else { this.say("Wrong passkey."); }

     }
  }
}

private Button makeButton(String label, Runnable r) { Button b = new Button(label); b.setMaxWidth(Double.MAX_VALUE); b.setOnAction((e) -> { r.run(); }); return b; }

private void say(String text) { this.output.appendText(text + "\n"); }

private void restart() { this.playerName = "Detective"; this.testerMode = false; this.relicMansion = this.relicTrain = false; this.m_clue1 = this.m_clue2 = this.m_clue3 = this.m_clue4 = false; this.t_clue1 = this.t_clue2 = this.t_clue3 = false; this.fRoman = this.fCipher = this.fBinary = this.fRiddle = this.combined = false; this.currentMission = CorridorEscapeChronicles.Mission.NONE; this.output.clear(); this.showIntroThenName(); }

public static void main(String[] args) { launch(args); } }
