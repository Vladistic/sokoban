package de.vladistic.sokoban;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Player extends Field {
    private int row, col;
    public final Image left, right, up, down;

    public Player(int startRow, int startCol) {
        super("projekt/player_down.png"); // Initialrichtung nach unten
        this.row = startRow;
        this.col = startCol;
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
     * Bewegt den Spieler um (dRow,dCol) Felder.
     */
    public void move(int dRow, int dCol) {
        this.row += dRow;
        this.col += dCol;
    }

    /** Zeichnet den Spieler an seiner aktuellen Position. */
    public void draw(GraphicsContext gc) {
        gc.drawImage(image, col * TILE, row * TILE);
    }
}
