package de.vladistic.sokoban;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Player extends Field {
    private int row, col; // Tile-Position
    private double x, y;  // Pixel-Position für Animation

    public final Image left, right, up, down;

    public Player(int startRow, int startCol) {
        super("projekt/player_down.png"); // Initialrichtung nach unten
        this.row = startRow;
        this.col = startCol;
        this.x = col * TILE;
        this.y = row * TILE;
        left  = new Image(getClass().getResourceAsStream("projekt/player_left.png"));
        right = new Image(getClass().getResourceAsStream("projekt/player_right.png"));
        up    = new Image(getClass().getResourceAsStream("projekt/player_up.png"));
        down  = new Image(getClass().getResourceAsStream("projekt/player_down.png"));
    }

    public int getRow() { return row; }
    public int getCol() { return col; }

    /** Setzt die Blickrichtung (Wechsel des Bildes). */
    public void setDirection(Image dirImage) {
        this.image = dirImage;
    }

    /**
     * Setzt die Tile-Position (nach Abschluss der Animation).
     */
    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
        this.x = col * TILE;
        this.y = row * TILE;
    }

    /**
     * Setzt die Pixelposition (für Animation).
     */
    public void setPixelPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /** Zeichnet den Spieler an seiner aktuellen Pixelposition. */
    public void draw(GraphicsContext gc) {
        gc.drawImage(image, x, y);
    }
}
