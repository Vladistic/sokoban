package de.vladistic.sokoban;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class StartMenuController {

    @FXML private Button btnNewGame;
    @FXML private Button btnOptions;
    @FXML private Button btnExit;
    @FXML private Button btnLevelUp;
    @FXML private Button btnLevelDown;
    @FXML private Label lblLevel;

    private int currentLevel = 1;
    private final int minLevel = 1;
    private final int maxLevel = 10;

    @FXML
    public void initialize() {
        updateLevelLabel();

        btnLevelUp.setOnAction(e -> {
            if (currentLevel < maxLevel) {
                currentLevel++;
                updateLevelLabel();
            }
        });

        btnLevelDown.setOnAction(e -> {
            if (currentLevel > minLevel) {
                currentLevel--;
                updateLevelLabel();
            }
        });

        btnNewGame.setOnAction(e -> startGame());

        btnOptions.setOnAction(e -> showOptions());

        btnExit.setOnAction(e -> System.exit(0));
    }

    private void updateLevelLabel() {
        lblLevel.setText("Level: " + currentLevel);
    }

    private void startGame() {
        // Platzhalter: Level starten
        System.out.println("Starte Level " + currentLevel);

        // Hier werde ich später je nach Level unterschiedliche FXMLs oder Logik laden
        // Beispiel: Für Level 1 das echte Spiel laden, für andere nur print

        if (currentLevel == 1) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("starter.fxml"));
                Stage stage = (Stage) btnNewGame.getScene().getWindow();
                Scene scene = new Scene(root, 1280, 768);
                stage.setTitle("Sokoban - Level " + currentLevel);
                stage.setScene(scene);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            System.out.println("Level " + currentLevel + " ist noch nicht implementiert.");
        }
    }

    private void showOptions() {
        System.out.println("Options clicked");
    }
}
