package de.vladistic.sokoban;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.image.Image;

public class StarterController {
    private static final int COLS = 16;
    private static final int ROWS = 10;
    private static final int TILE = Field.TILE; // 64

    private Field[][] grid = new Field[ROWS][COLS];
    private Player player;
    private GraphicsContext gc;

    @FXML private Canvas canvas;
    @FXML private MenuItem menuClose;
    @FXML private MenuItem menuDelete;
    @FXML private MenuItem menuAbout;
    @FXML private HBox statusBar;
    @FXML private Label    statusLabel1;
    @FXML private Label    statusLabel2;
    @FXML private Label    statusLabel3;

    @FXML
    public void initialize() {
        gc = canvas.getGraphicsContext2D();

        // Spielfeld initialisieren
        initGrid();
        player = new Player(5, 5); // Start in der Mitte

        // Draw initial
        drawAll();

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

        // Menu‑Actions (Beispiel: Close)
        menuClose.setOnAction(e -> System.exit(0));
        menuAbout.setOnAction(e -> statusLabel1.setText("Sokoban v1.0"));
        menuDelete.setOnAction(e -> statusLabel2.setText("Delete clicked"));
    }

    private void initGrid() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (r == 0 || r == ROWS - 1 || c == 0 || c == COLS - 1) {
                    grid[r][c] = new Wall();
                } else {
                    grid[r][c] = new Ground();
                }
            }
        }
    }

    private void tryMove(int dRow, int dCol, Image dirImage) {
        int nr = player.getRow() + dRow;
        int nc = player.getCol() + dCol;
        if (grid[nr][nc] instanceof Ground) {
            player.setDirection(dirImage);
            player.move(dRow, dCol);
            drawAll();
            statusLabel3.setText("Pos: (" + player.getRow()
                    + "," + player.getCol() + ")");
        }
    }

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
}