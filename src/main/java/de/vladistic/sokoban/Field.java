package de.vladistic.sokoban;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.util.Objects;

abstract class Field {
    public static final int TILE = 64;
    protected Image image;

    /**
     * @param resource Pfad zur Bilddatei im resources-Ordner, z.B. "/wall.png"
     */
    public Field(String resource) {
        image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(resource)));
    }

    /**
     * Zeichnet das Feld an Pixelkoordinate (x,y).
     */
    public void draw(GraphicsContext gc, int x, int y) {
        gc.drawImage(image, x, y);
    }
}
