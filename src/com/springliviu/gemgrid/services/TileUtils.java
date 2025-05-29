package com.springliviu.gemgrid.services;

import com.springliviu.gemgrid.BoosterType;
import com.springliviu.gemgrid.Tile;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Utility class for tile match detection and pattern recognition
public class TileUtils {

    // Check if tile is part of a match
    public static boolean isMatch(Tile[][] grid, int row, int col) {
        Tile tile = grid[row][col];
        if (tile.getTileColor() == null) return false;

        Color color = tile.getTileColor();
        int countH = 1;
        for (int i = col - 1; i >= 0 && sameColor(grid[row][i], color); i--) countH++;
        for (int i = col + 1; i < grid[0].length && sameColor(grid[row][i], color); i++) countH++;
        if (countH >= 3) return true;

        int countV = 1;
        for (int i = row - 1; i >= 0 && sameColor(grid[i][col], color); i--) countV++;
        for (int i = row + 1; i < grid.length && sameColor(grid[i][col], color); i++) countV++;
        return countV >= 3;
    }

    // Check for cross, T, L patterns to allow Color Bomb creation
    public static boolean isColorBombPattern(Tile[][] grid, int row, int col) {
        Color center = grid[row][col].getTileColor();
        if (center == null) return false;

        int[][] directions = {{-1,0},{1,0},{0,-1},{0,1}};
        int matchCount = 0;

        for (int[] dir : directions) {
            int dr = dir[0], dc = dir[1];
            int r = row + dr, c = col + dc;
            if (inBounds(grid, r, c) && sameColor(grid[r][c], center)) {
                matchCount++;
            }
        }

        return matchCount >= 3;
    }

    // Find all matched tiles in the grid (horizontal or vertical)
    public static Set<Tile> findMatchedTiles(Tile[][] grid) {
        Set<Tile> matched = new HashSet<>();

        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length - 2; col++) {
                Color color = grid[row][col].getTileColor();
                if (color != null &&
                        sameColor(grid[row][col+1], color) &&
                        sameColor(grid[row][col+2], color)) {

                    matched.add(grid[row][col]);
                    matched.add(grid[row][col+1]);
                    matched.add(grid[row][col+2]);

                    int k = col + 3;
                    while (k < grid[0].length && sameColor(grid[row][k], color)) {
                        matched.add(grid[row][k]);
                        k++;
                    }
                }
            }
        }

        for (int col = 0; col < grid[0].length; col++) {
            for (int row = 0; row < grid.length - 2; row++) {
                Color color = grid[row][col].getTileColor();
                if (color != null &&
                        sameColor(grid[row+1][col], color) &&
                        sameColor(grid[row+2][col], color)) {

                    matched.add(grid[row][col]);
                    matched.add(grid[row+1][col]);
                    matched.add(grid[row+2][col]);

                    int k = row + 3;
                    while (k < grid.length && sameColor(grid[k][col], color)) {
                        matched.add(grid[k][col]);
                        k++;
                    }
                }
            }
        }

        return matched;
    }

    // Determine booster tiles from matched tiles
    public static Map<Tile, BoosterType> classifyMatchesAndBoosters(Tile[][] grid, Set<Tile> matched) {
        Map<Tile, BoosterType> result = new HashMap<>();

        // Horizontal streaks
        for (int row = 0; row < grid.length; row++) {
            int streak = 1;
            for (int col = 1; col < grid[0].length; col++) {
                if (matched.contains(grid[row][col]) &&
                        matched.contains(grid[row][col - 1]) &&
                        sameColor(grid[row][col], grid[row][col - 1].getTileColor())) {
                    streak++;
                } else {
                    if (streak == 4) {
                        result.put(grid[row][col - 2], BoosterType.ROW);
                    } else if (streak >= 5) {
                        result.put(grid[row][col - 3], BoosterType.COLOR_BOMB);
                    }
                    streak = 1;
                }
            }

            if (streak == 4) {
                result.put(grid[row][grid[0].length - 2], BoosterType.ROW);
            } else if (streak >= 5) {
                result.put(grid[row][grid[0].length - 3], BoosterType.COLOR_BOMB);
            }
        }

        // Vertical streaks
        for (int col = 0; col < grid[0].length; col++) {
            int streak = 1;
            for (int row = 1; row < grid.length; row++) {
                if (matched.contains(grid[row][col]) &&
                        matched.contains(grid[row - 1][col]) &&
                        sameColor(grid[row][col], grid[row - 1][col].getTileColor())) {
                    streak++;
                } else {
                    if (streak == 4) {
                        result.put(grid[row - 2][col], BoosterType.COLUMN);
                    } else if (streak >= 5) {
                        result.put(grid[row - 3][col], BoosterType.COLOR_BOMB);
                    }
                    streak = 1;
                }
            }

            if (streak == 4) {
                result.put(grid[grid.length - 2][col], BoosterType.COLUMN);
            } else if (streak >= 5) {
                result.put(grid[grid.length - 3][col], BoosterType.COLOR_BOMB);
            }
        }

        return result;
    }

    // Helper: checks tile color equality
    private static boolean sameColor(Tile tile, Color color) {
        return tile != null && color != null && color.equals(tile.getTileColor());
    }

    private static boolean inBounds(Tile[][] grid, int row, int col) {
        return row >= 0 && row < grid.length && col >= 0 && col < grid[0].length;
    }
}
