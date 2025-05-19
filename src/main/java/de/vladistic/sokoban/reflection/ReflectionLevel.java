package de.vladistic.sokoban.reflection;

public class ReflectionLevel {
    private String[][] gameField = new String[][] {
            {"w","w","w","w","w","w","w","w","w","w","w","w","w","w","w","w"},
            {"w","w","w","w","w","w","w","g","g","w","w","w","w","w","w","w"},
            {"w","w","w","w","w","w","w","c","c","g","g","w","w","w","w","w"},
            {"w","w","w","w","w","w","w","g","c","*","g","w","w","w","w","w"},
            {"w","w","w","w","w","*","*","*","g","w","w","w","w","w","w","w"},
            {"w","w","w","w","w","w","w","c","g","w","w","w","w","w","w","w"},
            {"w","w","w","w","w","w","w","g","g","w","w","w","w","w","w","w"},
            {"w","w","w","w","w","w","w","w","w","w","w","w","w","w","w","w"},
            {"w","w","w","w","w","w","w","w","w","w","w","w","w","w","w","w"},
            {"w","w","w","w","w","w","w","w","w","w","w","w","w","w","w","w"},
    };

    private final int playerPosX = 1;
    private final int playerPosY = 8;

    public String[] getLine(int line) {
        if (line <= gameField.length) {
            return gameField[line];
        } else {
            return null;
        }
    }

    public int getPlayerPosX() {
        return playerPosX;
    }

    public int getPlayerPosY() {
        return playerPosY;
    }
}
