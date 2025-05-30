package com.springliviu.gemgrid.services;

import com.springliviu.gemgrid.BoosterType;
import com.springliviu.gemgrid.Tile;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Utility class for tile match detection and booster classification
public class TileUtils {

    // Checks if a single tile is part of a horizontal or vertical match of 3 or more
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

    // Finds all matched tiles in the grid (horizontal and vertical matches of 3 or more)
    public static Set<Tile> findMatchedTiles(Tile[][] grid) {
        Set<Tile> matched = new HashSet<>();

        // Horizontal matches
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length - 2; col++) {
                Color color = grid[row][col].getTileColor();
                if (color != null &&
                        sameColor(grid[row][col + 1], color) &&
                        sameColor(grid[row][col + 2], color)) {

                    matched.add(grid[row][col]);
                    matched.add(grid[row][col + 1]);
                    matched.add(grid[row][col + 2]);

                    int k = col + 3;
                    while (k < grid[0].length && sameColor(grid[row][k], color)) {
                        matched.add(grid[row][k]);
                        k++;
                    }
                }
            }
        }

        // Vertical matches
        for (int col = 0; col < grid[0].length; col++) {
            for (int row = 0; row < grid.length - 2; row++) {
                Color color = grid[row][col].getTileColor();
                if (color != null &&
                        sameColor(grid[row + 1][col], color) &&
                        sameColor(grid[row + 2][col], color)) {

                    matched.add(grid[row][col]);
                    matched.add(grid[row + 1][col]);
                    matched.add(grid[row + 2][col]);

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

    // Classifies matched tiles and determines which ones should become boosters
    public static Map<Tile, BoosterType> classifyMatchesAndBoosters(Tile[][] grid, Set<Tile> matched) {
        Map<Tile, BoosterType> result = new HashMap<>();
        Set<Tile> visited = new HashSet<>();

        // Detect streaks (for ROW/COLUMN/COLOR_BOMB)
        for (int row = 0; row < grid.length; row++) {
            int streak = 1;
            for (int col = 1; col < grid[0].length; col++) {
                if (matched.contains(grid[row][col]) &&
                        matched.contains(grid[row][col - 1]) &&
                        sameColor(grid[row][col], grid[row][col - 1].getTileColor())) {
                    streak++;
                } else {
                    if (streak == 4) {
                        int center = col - 2;
                        result.put(grid[row][center], BoosterType.ROW);
                    } else if (streak >= 5) {
                        int center = col - streak / 2;
                        result.put(grid[row][center], BoosterType.COLOR_BOMB);
                    }
                    streak = 1;
                }
            }
            if (streak == 4) {
                int center = grid[0].length - 2;
                result.put(grid[row][center], BoosterType.ROW);
            } else if (streak >= 5) {
                int center = grid[0].length - streak / 2;
                result.put(grid[row][center], BoosterType.COLOR_BOMB);
            }
        }

        for (int col = 0; col < grid[0].length; col++) {
            int streak = 1;
            for (int row = 1; row < grid.length; row++) {
                if (matched.contains(grid[row][col]) &&
                        matched.contains(grid[row - 1][col]) &&
                        sameColor(grid[row][col], grid[row - 1][col].getTileColor())) {
                    streak++;
                } else {
                    if (streak == 4) {
                        int center = row - 2;
                        result.put(grid[center][col], BoosterType.COLUMN);
                    } else if (streak >= 5) {
                        int center = row - streak / 2;
                        result.put(grid[center][col], BoosterType.COLOR_BOMB);
                    }
                    streak = 1;
                }
            }
            if (streak == 4) {
                int center = grid.length - 2;
                result.put(grid[center][col], BoosterType.COLUMN);
            } else if (streak >= 5) {
                int center = grid.length - streak / 2;
                result.put(grid[center][col], BoosterType.COLOR_BOMB);
            }
        }

        // Detect large clusters (T/L/+ shapes)
        for (Tile tile : matched) {
            if (!visited.contains(tile)) {
                Set<Tile> cluster = new HashSet<>();
                dfsCluster(grid, tile, matched, cluster, tile.getTileColor());

                if (cluster.size() >= 5) {
                    Tile center = cluster.iterator().next();
                    if (!result.containsKey(center)) {
                        result.put(center, BoosterType.COLOR_BOMB);
                        System.out.println("COLOR_BOMB from cluster at " + center.getRow() + "," + center.getCol());
                    }
                }

                visited.addAll(cluster);
            }
        }

        return result;
    }

    // Performs DFS to group adjacent matched tiles of the same color
    private static void dfsCluster(Tile[][] grid, Tile tile, Set<Tile> matched, Set<Tile> cluster, Color color) {
        if (tile == null || cluster.contains(tile) || tile.getTileColor() == null) return;
        if (!matched.contains(tile)) return;
        if (!tile.getTileColor().equals(color)) return;

        cluster.add(tile);

        int row = tile.getRow();
        int col = tile.getCol();

        for (int[] d : new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}}) {
            int newRow = row + d[0];
            int newCol = col + d[1];
            if (inBounds(grid, newRow, newCol)) {
                dfsCluster(grid, grid[newRow][newCol], matched, cluster, color);
            }
        }
    }

    // Checks if row/col are inside grid bounds
    private static boolean inBounds(Tile[][] grid, int row, int col) {
        return row >= 0 && row < grid.length && col >= 0 && col < grid[0].length;
    }

    // Compares tile color to target color
    private static boolean sameColor(Tile tile, Color color) {
        return tile != null && color != null && color.equals(tile.getTileColor());
    }
}
