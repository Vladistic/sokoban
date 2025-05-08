package de.vladistic.sokoban;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

public class LevelController {
    private int COLS = 16;
    private int ROWS = 10;
    private static final int TILE = Field.TILE; // 64
    private boolean isAnimating = false;

    private Field[][] grid;
    private Player player;
    private GraphicsContext gc;
    private Timeline timer;
    private int seconds = 0;

    @FXML private Canvas canvas;
    @FXML private MenuItem menuClose;
    @FXML private MenuItem menuDelete;
    @FXML private MenuItem menuAbout;
    @FXML private HBox statusBar;
    @FXML private Label versionLabel;
    @FXML private Label timerLabel;

    /**
     * Sets the level data for the game.
     * @param levelData The level data to set.
     */
    public void setLevelData(String levelData) {
        String[] lines = levelData.split("\n");
        
        // Parse dimensions (second line)
        String[] dimensions = lines[1].trim().split("x");
        ROWS = Integer.parseInt(dimensions[1]);
        COLS = Integer.parseInt(dimensions[0]);

        // Parse player position (third line)
        String[] playerPos = lines[2].trim().split(",");
        int playerRow = Integer.parseInt(playerPos[0]);
        int playerCol = Integer.parseInt(playerPos[1]);

        // Initialize grid with correct dimensions
        grid = new Field[ROWS][COLS];
        
        // Parse level layout (starting from fourth line)
        for (int r = 0; r < ROWS; r++) {
            String line = lines[r + 3];
            for (int c = 0; c < COLS; c++) {
                char tile = line.charAt(c);
                switch (tile) {
                    case 'w' -> grid[r][c] = new Wall();
                    case 'g' -> grid[r][c] = new Ground();
                    case 'c' -> grid[r][c] = new Crate();
                    case '*' -> grid[r][c] = new Goal();
                }
            }
        }
        
        // Create player at specified position
        player = new Player(playerRow, playerCol);
        
        // Update canvas size based on level dimensions
        canvas.setWidth(COLS * TILE);
        canvas.setHeight(ROWS * TILE);
        
        // Start the timer
        startTimer();
        
        // Draw the level
        drawAll();
    }

    @FXML
    public void initialize() {
        gc = canvas.getGraphicsContext2D();

        // Key-Handling
        canvas.setFocusTraversable(true);
        canvas.setOnKeyPressed(evt -> {
            KeyCode kc = evt.getCode();
            if (kc == KeyCode.LEFT) {
                tryMove(0, -1, player.left);
            } else if (kc == KeyCode.RIGHT) {
                tryMove(0, +1, player.right);
            } else if (kc == KeyCode.UP) {
                tryMove(-1, 0, player.up);
            } else if (kc == KeyCode.DOWN) {
                tryMove(+1, 0, player.down);
            }
        });

        // Menu‑Actions
        menuClose.setOnAction(e -> System.exit(0));
        menuAbout.setOnAction(e -> versionLabel.setText("Sokoban v0.2"));
        menuDelete.setOnAction(e -> stopTimer());
    }

    private void startTimer() {
        // Stop existing timer if any
        if (timer != null) {
            timer.stop();
        }
        
        // Reset seconds
        seconds = 0;
        updateTimerLabel();
        
        // Create new timer
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            seconds++;
            updateTimerLabel();
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }
    
    private void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }
    
    private void updateTimerLabel() {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, remainingSeconds));
    }

    /**
     * Draws all the fields and the player on the canvas.
     */
    private void drawAll() {
        // Felder zeichnen
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                grid[r][c].draw(gc, c * TILE, r * TILE);
            }
        }
        // Spieler darüber
        player.draw(gc);
    }

    /**
     * Tries to move the player in the given direction.
     * @param dRow The row to move the player in.
     * @param dCol The column to move the player in.
     * @param dirImage The image to set the player's direction to.
     */
    private void tryMove(int dRow, int dCol, Image dirImage) {
        if (isAnimating) return;

        int nr = player.getRow() + dRow;
        int nc = player.getCol() + dCol;
        Field targetField = grid[nr][nc];

        if (targetField instanceof Ground) {
            player.setDirection(dirImage);
            animateMove(dRow, dCol);
        } else if (targetField instanceof Crate) {
            // Check if we can move the crate
            int crateNr = nr + dRow;
            int crateNc = nc + dCol;
            if (crateNr >= 0 && crateNr < ROWS && crateNc >= 0 && crateNc < COLS) {
                Field behindCrate = grid[crateNr][crateNc];
                if (behindCrate instanceof Ground || behindCrate instanceof Goal) {
                    player.setDirection(dirImage);
                    animateCrateMove(dRow, dCol, nr, nc, crateNr, crateNc);
                }
            }
        } else if (targetField instanceof Goal) {
            // Check if all crates are on goals
            if (checkVictory()) {
                showVictoryMessage();
            } else {
                player.setDirection(dirImage);
                animateMove(dRow, dCol);
            }
        }
    }

    /**
     * Checks if the player has won the game.
     * Added in case we want to add more complex victory conditions in the future.
     * @return true if the player has won, false otherwise.
     */
    private boolean checkVictory() {
        return true; 
    }

    /**
     * Shows the victory message and starts a countdown to return to the main menu.
     */
    private void showVictoryMessage() {
        isAnimating = true;
        
        // Clear the canvas
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        // Draw victory message
        gc.setFill(javafx.scene.paint.Color.BLACK);
        gc.setFont(javafx.scene.text.Font.font(48));
        String message = "You've won!";
        Text text = new Text(message);
        gc.fillText(message, 
            (canvas.getWidth() - text.getLayoutBounds().getWidth()) / 3,
            canvas.getHeight() / 2);
        
        // Draw timer
        gc.setFont(javafx.scene.text.Font.font(24));
        
        // Create countdown timer
        Timeline countdown = new Timeline();
        int seconds = 10;
        for (int i = 0; i < seconds; i++) {
            String timerText = "Returning to menu in " + (seconds - i) + " seconds...";
            Text timer = new Text(timerText);
            KeyFrame kf = new KeyFrame(Duration.seconds(i), e -> {
                gc.clearRect(0, canvas.getHeight() / 2, canvas.getWidth(), canvas.getHeight() / 2);
                gc.fillText(timerText, 
                    (canvas.getWidth() - timer.getLayoutBounds().getWidth()) / 3,
                    canvas.getHeight() * 3/4);
            });
            countdown.getKeyFrames().add(kf);
        }
        
        // Add final frame to return to menu
        KeyFrame finalFrame = new KeyFrame(Duration.seconds(seconds), e -> {
            returnToMainMenu();
        });
        countdown.getKeyFrames().add(finalFrame);
        
        countdown.play();
    }

    /**
     * Returns to the main menu.
     * Will probably be moved to its own controller in the future.
     */
    private void returnToMainMenu() {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass()
                    .getResource("startmenu.fxml")));
            Scene scene = new Scene(root, canvas.getScene().getWidth(), canvas.getScene().getHeight());
            Stage stage = (Stage) canvas.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Animates the crate's move.
     * @param dRow The row to move the crate in.
     * @param dCol The column to move the crate in.
     * @param crateRow The row of the crate.
     * @param crateCol The column of the crate.
     * @param targetRow The row to move the crate to.
     * @param targetCol The column to move the crate to.
     */
    private void animateCrateMove(int dRow, int dCol, int crateRow, int crateCol, int targetRow, int targetCol) {
        isAnimating = true;

        final int steps = 8;
        final double stepDurationMs = 100;
        
        Timeline timeline = new Timeline();

        // First move the crate
        for (int i = 1; i <= steps; i++) {
            final int step = i;
            KeyFrame kf = new KeyFrame(Duration.millis(step * stepDurationMs), e -> {
                
                // Update the crate's position
                grid[crateRow][crateCol] = new Ground();
                grid[targetRow][targetCol] = new Crate();
                
                // Draw everything
                drawAll();

                if (step == steps) {
                    // Now move the player
                    animateMove(dRow, dCol);
                }
            });
            timeline.getKeyFrames().add(kf);
        }
        timeline.play();
    }

    /**
     * Animates the player's move.
     * @param dRow The row to move the player in.
     * @param dCol The column to move the player in.
     */
    private void animateMove(int dRow, int dCol) {
        isAnimating = true;

        final int steps = 8;
        final double stepDurationMs = 25; // 8 * 25ms = 200ms Gesamtzeit
        final double startX = player.getCol() * TILE;
        final double startY = player.getRow() * TILE;
        final double deltaX = dCol * TILE / steps;
        final double deltaY = dRow * TILE / steps;

        Timeline timeline = new Timeline();

        for (int i = 1; i <= steps; i++) {
            final int step = i;
            KeyFrame kf = new KeyFrame(Duration.millis(step * stepDurationMs), e -> {
                double newX = startX + deltaX * step;
                double newY = startY + deltaY * step;
                player.setPixelPosition(newX, newY);
                drawAll();

                if (step == steps) {
                    // Animation fertig: Tile-Position aktualisieren
                    player.setPosition(player.getRow() + dRow, player.getCol() + dCol);
                    isAnimating = false;
                }
            });
            timeline.getKeyFrames().add(kf);
        }

        timeline.play();
    }
}