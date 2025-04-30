package de.vladistic.sokoban;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.image.Image;
import javafx.util.Duration;

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
        String[] dimensions = lines[1].trim().split(" ");
        ROWS = Integer.parseInt(dimensions[0]);
        COLS = Integer.parseInt(dimensions[1]);
        
        // Parse player position (third line)
        String[] playerPos = lines[2].trim().split(" ");
        int playerRow = Integer.parseInt(playerPos[0]);
        int playerCol = Integer.parseInt(playerPos[1]);
        
        // Initialize grid with correct dimensions
        grid = new Field[ROWS][COLS];
        
        // Parse level layout (starting from fourth line)
        for (int r = 0; r < ROWS; r++) {
            String line = lines[r + 3];
            for (int c = 0; c < COLS; c++) {
                char tile = line.charAt(c);
                grid[r][c] = (tile == 'w') ? new Wall() : new Ground();
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
        if (grid[nr][nc] instanceof Ground) {
            player.setDirection(dirImage);
            animateMove(dRow, dCol);
        }
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