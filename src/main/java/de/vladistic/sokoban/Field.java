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
     * @param gc Das GraphicsContext, auf dem das Feld gezeichnet werden soll.
     * @param x Die x-Koordinate des Feldes.
     * @param y Die y-Koordinate des Feldes.
     * @throws IllegalArgumentException Wenn die Koordinaten nicht gültig sind.
     */
    public void draw(GraphicsContext gc, int x, int y) {
        gc.drawImage(image, x, y);
    }
}
