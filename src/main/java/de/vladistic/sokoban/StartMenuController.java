package de.vladistic.sokoban;

import de.vladistic.sokoban.utils.ParseLevels;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class StartMenuController {

    /**
     * Lädt das ReflectionLevel per Reflection und konvertiert es in einen Level-String,
     * der von setLevelData wie gewohnt verarbeitet werden kann.
     */
    private String loadReflectionLevelString() {
        try {
            Class<?> clazz = Class.forName("de.vladistic.sokoban.reflection.ReflectionLevel");
            Object reflectionLevel = clazz.getDeclaredConstructor().newInstance();

            // Spielfeld-Zeilen sammeln
            StringBuilder sb = new StringBuilder();
            sb.append("+++\n"); // Startmarker wie im Dateiformat
            int rows = 10; // wie in ReflectionLevel definiert
            int cols = 16;
            sb.append(cols + "x" + rows + "\n");

            // Spielerposition
            int playerRow = (int) clazz.getMethod("getPlayerPosX").invoke(reflectionLevel);
            int playerCol = (int) clazz.getMethod("getPlayerPosY").invoke(reflectionLevel);
            sb.append(playerRow + "," + playerCol + "\n");

            // Zeilen des Spielfelds
            for (int i = 0; i < rows; i++) {
                String[] line = (String[]) clazz.getMethod("getLine", int.class).invoke(reflectionLevel, i);
                for (String cell : line) {
                    sb.append(cell);
                }
                sb.append("\n");
            }
            sb.append("---"); // Endmarker
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "+++\n16x10\n1,8\nwgwgwgwgwgwgwgwg\n---"; // Fallback
        }
    }


    @FXML private Button btnNewGame;
    @FXML private Button btnOptions;
    @FXML private Button btnExit;
    @FXML private Button btnLevelUp;
    @FXML private Button btnLevelDown;
    @FXML private Label lblLevel;

    private int currentLevel = 1;
    private final int minLevel = 1;
    private int maxLevel = 1;
    private String[] levels;

    @FXML
    public void initialize() {
        try {
            // Load levels from file
            File levelsFile = new File(getClass().getResource("projekt/levels.txt").getFile());
            String[] fileLevels = ParseLevels.parseLevels(levelsFile);

            // Load ReflectionLevel via Reflection
            String reflectionLevelString = loadReflectionLevelString();

            // Combine file levels and reflection level
            levels = new String[fileLevels.length + 1];
            System.arraycopy(fileLevels, 0, levels, 0, fileLevels.length);
            levels[fileLevels.length] = reflectionLevelString;
            maxLevel = levels.length;

            updateLevelLabel();
        } catch (Exception e) {
            e.printStackTrace();
            maxLevel = 1;
        }

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

    /**
     * Updates the level label.
     */
    private void updateLevelLabel() {
        lblLevel.setText("Level: " + currentLevel + " / " + maxLevel);
    }

    /**
     * Starts the game.
     */ 
    private void startGame() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("starter.fxml"));
            Parent root = loader.load();
            LevelController controller = loader.getController();
            
            // Pass the current level data to the controller
            controller.setLevelData(levels[currentLevel - 1]);
            
            Stage stage = (Stage) btnNewGame.getScene().getWindow();
            Scene scene = new Scene(root, 1280, 768);
            stage.setTitle("Sokoban - Level " + currentLevel);
            stage.setScene(scene);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Shows the options menu.
     * Added for future use.
     * 
     */
    private void showOptions() {
        System.out.println("Options clicked");
    }
}
