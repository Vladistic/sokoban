package de.vladistic.sokoban.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ParseLevels {
    /*
     * Parses the levels from the file and returns an array of strings.
     * The first line contains three '+', the last line of each level section contains three '-',
     * the second line is the level dimensions, and the third line is the player position,
     * the rest is the level map containing either 'w' for walls or 'g' for ground.
     * 
     * @param file The file to parse the levels from.
     * @return An array of strings containing the levels.
     * @throws FileNotFoundException If the file is not found.
     */
    public static String[] parseLevels(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);
        StringBuilder currentLevel = new StringBuilder();
        String[] levels = new String[10];
        int levelCount = 0;
        
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            
            if (line.equals("+++")) {
                // Start of a new level
                currentLevel = new StringBuilder();
                currentLevel.append(line).append("\n");
            } else if (line.equals("---")) {
                // End of current level
                currentLevel.append(line);
                levels[levelCount++] = currentLevel.toString();
            } else {
                // Level content (dimensions, player position, or map)
                currentLevel.append(line).append("\n");
            }
        }
        
        scanner.close();
        
        // Trim array to actual number of levels
        String[] trimmedLevels = new String[levelCount];
        System.arraycopy(levels, 0, trimmedLevels, 0, levelCount);
        
        return trimmedLevels;
    }
}
