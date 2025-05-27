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
private boolean[][] isGoal; // Speichert, ob an dieser Position ein Ziel ist
private Image crateOnTargetImg = new Image(Objects.requireNonNull(getClass().getResourceAsStream("projekt/crate_on_target.png")));
private Image crateTargetImg = new Image(Objects.requireNonNull(getClass().getResourceAsStream("projekt/crate_target.png")));

    private Player player;
    private GraphicsContext gc;
    private int seconds = 0;

    @FXML private Canvas canvas;
    @FXML private MenuItem menuClose;
    @FXML private MenuItem menuDelete;
    @FXML private MenuItem menuAbout;
    @FXML private HBox statusBar;
    @FXML private Label versionLabel;
    @FXML private Label timerLabel;
    @FXML private Label moveCountLabel;
    
    private volatile boolean running = true;
    private int moveCount = 0;
    private Thread timerThread;
    private Timeline cooldownTimeline;

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

        // Initialize grid and isGoal with correct dimensions
        grid = new Field[ROWS][COLS];
        isGoal = new boolean[ROWS][COLS];

        // Parse level layout (starting from fourth line)
        for (int r = 0; r < ROWS; r++) {
            String line = lines[r + 3];
            for (int c = 0; c < COLS; c++) {
                char tile = line.charAt(c);
                switch (tile) {
                    case 'w' -> grid[r][c] = new Wall();
                    case 'g' -> grid[r][c] = new Ground();
                    case 'c' -> grid[r][c] = new Crate();
                    case '*' -> {
                        grid[r][c] = new Goal();
                        isGoal[r][c] = true;
                    }
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

    /**
     * Starts or restarts the game timer.
     */
    public void startTimer() {
        startTimerThread();
    }
    
    /**
     * Stops the game timer.
     */
    public void stopTimer() {
        stopTimerThread();
    }
    
    /**
     * Resets the game timer and move counter.
     */
    public void resetTimer() {
        seconds = 0;
        moveCount = 0;
        updateTimerLabel();
        updateMoveCount();
    }
    
    @FXML
    public void initialize() {
        gc = canvas.getGraphicsContext2D();
        
        // Initialize move counter
        updateMoveCount();
        
        // Start the timer thread
        startTimerThread();

        // Key-Handling
        canvas.setFocusTraversable(true);
        canvas.setOnKeyPressed(evt -> {
            if (isAnimating) {
                evt.consume();
                return;
            }
            
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
            
            evt.consume();
        });

        // Menu‑Actions
        menuClose.setOnAction(e -> System.exit(0));
        menuAbout.setOnAction(e -> versionLabel.setText("Sokoban v0.2"));
        menuDelete.setOnAction(e -> stopTimer());
    }

    private void startTimerThread() {
        // Stop any existing timer thread
        stopTimerThread();
        
        // Reset seconds and update display
        seconds = 0;
        updateTimerLabel();
        
        // Create and start new timer thread
        running = true;
        timerThread = new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(1000);
                    seconds++;
                    javafx.application.Platform.runLater(this::updateTimerLabel);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        timerThread.setDaemon(true);
        timerThread.start();
    }
    
    private void stopTimerThread() {
        running = false;
        if (timerThread != null) {
            timerThread.interrupt();
            timerThread = null;
        }
    }
    
    private void updateTimerLabel() {
        if (timerLabel != null) {
            int minutes = seconds / 60;
            int remainingSeconds = seconds % 60;
            timerLabel.setText(String.format("Time: %02d:%02d", minutes, remainingSeconds));
        }
    }
    
    private void updateMoveCount() {
        if (moveCountLabel != null) {
            moveCountLabel.setText(String.format("Moves: %d", moveCount));
        }
    }
    
    private void incrementMoveCount() {
        moveCount++;
        updateMoveCount();
    }

    private boolean gameWon = false;

    /**
     * Draws all the fields and the player on the canvas.
     */
    private void drawAll() {
        if (gameWon) {
            return; // Don't redraw the game if we've won
        }
        
        // Clear the entire canvas first to remove any previous frame
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        // Draw all tiles
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                // Draw ground or goal tile first
                if (isGoal[r][c]) {
                    gc.drawImage(crateTargetImg, c * TILE, r * TILE);
                } else {
                    grid[r][c].draw(gc, c * TILE, r * TILE);
                }
                
                // Draw crates if present
                if (grid[r][c] instanceof GreenCrate) {
                    gc.drawImage(crateOnTargetImg, c * TILE, r * TILE);
                } else if (grid[r][c] instanceof Crate) {
                    grid[r][c].draw(gc, c * TILE, r * TILE);
                }
            }
        }
        
        // Draw player on top of everything
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
            incrementMoveCount();
            animateMove(dRow, dCol);
        } else if (targetField instanceof Crate || targetField instanceof GreenCrate) {
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
                incrementMoveCount();
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
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (isGoal[r][c] && !(grid[r][c] instanceof GreenCrate)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Shows the victory message and starts a countdown to return to the main menu.
     */
    private void showVictoryMessage() {
        gameWon = true;
        isAnimating = true;
        
        // Clear the entire canvas
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
        incrementMoveCount();

        // Update the grid immediately
        boolean targetIsGoal = isGoal[targetRow][targetCol];
        boolean oldIsGoal = isGoal[crateRow][crateCol];
        
        // Remove crate from old position
        if (oldIsGoal) {
            grid[crateRow][crateCol] = new Goal();
        } else {
            grid[crateRow][crateCol] = new Ground();
        }
        
        // Add crate to new position
        if (targetIsGoal) {
            grid[targetRow][targetCol] = new GreenCrate();
        } else {
            grid[targetRow][targetCol] = new Crate();
        }
        
        // Animate the movement
        final int steps = 8;
        final double stepDurationMs = 50; // Faster than player movement for better feel
        
        // Calculate start and end positions in pixels
        final double startX = crateCol * TILE;
        final double startY = crateRow * TILE;
        final double endX = targetCol * TILE;
        final double endY = targetRow * TILE;
        
        // Create a temporary crate for animation
        Field animatingCrate = targetIsGoal ? new GreenCrate() : new Crate();
        
        Timeline timeline = new Timeline();

        for (int i = 1; i <= steps; i++) {
            final int step = i;
            KeyFrame kf = new KeyFrame(Duration.millis(step * stepDurationMs), e -> {
                // Calculate smooth movement using easing function
                double progress = (double)step / steps;
                double easedProgress = progress < 0.5 ? 2 * progress * progress : 1 - Math.pow(-2 * progress + 2, 2) / 2;
                
                double newX = startX + (endX - startX) * easedProgress;
                double newY = startY + (endY - startY) * easedProgress;
                
                // Draw everything
                drawAll();
                
                // Draw the animating crate at the current position (cast to int for drawing)
                animatingCrate.draw(gc, (int)newX, (int)newY);
                
                if (step == steps) {
                    // Final position - redraw to ensure everything is clean
                    drawAll();
                    // Now animate the player movement
                    animateMove(dRow, dCol);
                    // Check for victory condition
                    if (checkVictory()) {
                        showVictoryMessage();
                    }
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
        
        // Update player's grid position immediately
        player.setPosition(player.getRow() + dRow, player.getCol() + dCol);
        
        // Calculate animation parameters
        final int steps = 8;
        final double stepDurationMs = 25; // 8 * 25ms = 200ms total
        final double startX = player.getCol() * TILE - dCol * TILE; // Start from previous position
        final double startY = player.getRow() * TILE - dRow * TILE; // Start from previous position
        final double targetX = player.getCol() * TILE;
        final double targetY = player.getRow() * TILE;

        Timeline timeline = new Timeline();

        for (int i = 1; i <= steps; i++) {
            final int step = i;
            KeyFrame kf = new KeyFrame(Duration.millis(step * stepDurationMs), e -> {
                // Calculate smooth movement using easing function (quadratic)
                double progress = (double)step / steps;
                double easedProgress = progress < 0.5 ? 2 * progress * progress : 1 - Math.pow(-2 * progress + 2, 2) / 2;
                
                double newX = startX + (targetX - startX) * easedProgress;
                double newY = startY + (targetY - startY) * easedProgress;
                
                player.setPixelPosition(newX, newY);
                drawAll();

                if (step == steps) {
                    // Ensure final position is exact
                    player.setPixelPosition(targetX, targetY);
                    drawAll();
                    isAnimating = false;
                }
            });
            timeline.getKeyFrames().add(kf);
        }
        
        // Set what happens when the animation is finished
        timeline.setOnFinished(e -> {
            // Start cooldown period
            isAnimating = true;
            
            // Clear any existing cooldown
            if (cooldownTimeline != null) {
                cooldownTimeline.stop();
            }
            
            // Create new cooldown
            cooldownTimeline = new Timeline(
                new KeyFrame(Duration.millis(100), event -> {
                    isAnimating = false;
                })
            );
            cooldownTimeline.setCycleCount(1);
            cooldownTimeline.play();
        });
        
        timeline.play();
    }
}