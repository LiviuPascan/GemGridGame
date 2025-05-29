package com.springliviu.gemgrid.services;

import com.springliviu.gemgrid.BoosterType;
import com.springliviu.gemgrid.Tile;
import javafx.application.Platform;
import javafx.scene.paint.Color;

import java.util.Random;

// Handles logic related to activating boosters on the grid
public class BoosterHandler {

    // Activates a single booster directly (not via swapping)
    public static void activateBooster(Tile tile, Tile[][] grid, Runnable postAction) {
        int row = tile.getRow();
        int col = tile.getCol();

        BoosterType booster = tile.getBooster();
        tile.setTileColor(null);
        tile.setBooster(BoosterType.NONE);

        switch (booster) {
            case ROW:
                for (int c = 0; c < grid[0].length; c++) {
                    grid[row][c].setTileColor(null);
                    grid[row][c].setBooster(BoosterType.NONE);
                }
                break;
            case COLUMN:
                for (int r = 0; r < grid.length; r++) {
                    grid[r][col].setTileColor(null);
                    grid[r][col].setBooster(BoosterType.NONE);
                }
                break;
            case COLOR_BOMB:
                Color targetColor = null;

                // Pick first available color on the board (excluding self)
                for (int r = 0; r < grid.length && targetColor == null; r++) {
                    for (int c = 0; c < grid[0].length; c++) {
                        if (grid[r][c] != tile && grid[r][c].getTileColor() != null) {
                            targetColor = grid[r][c].getTileColor();
                            break;
                        }
                    }
                }

                if (targetColor != null) {
                    for (int r = 0; r < grid.length; r++) {
                        for (int c = 0; c < grid[0].length; c++) {
                            if (targetColor.equals(grid[r][c].getTileColor())) {
                                grid[r][c].setTileColor(null);
                                grid[r][c].setBooster(BoosterType.NONE);
                            }
                        }
                    }
                }
                break;
        }

        Platform.runLater(postAction);
    }

    // Triggers a combo when COLOR_BOMB is swapped with another tile
    public static void triggerColorBombCombo(Tile a, Tile b, Tile[][] grid, Random random) {
        Tile bomb = (a.getBooster() == BoosterType.COLOR_BOMB) ? a : b;
        Tile target = (bomb == a) ? b : a;

        // Case 1: Double color bomb - clear entire grid
        if (a.getBooster() == BoosterType.COLOR_BOMB && b.getBooster() == BoosterType.COLOR_BOMB) {
            for (int r = 0; r < grid.length; r++) {
                for (int c = 0; c < grid[0].length; c++) {
                    grid[r][c].setTileColor(null);
                    grid[r][c].setBooster(BoosterType.NONE);
                }
            }
            return;
        }

        // Case 2: Color bomb + row/column booster - trigger random boosters
        if (target.getBooster() == BoosterType.ROW || target.getBooster() == BoosterType.COLUMN) {
            for (int i = 0; i < 5; i++) {
                int r = random.nextInt(grid.length);
                int c = random.nextInt(grid[0].length);
                BoosterType bonus = random.nextBoolean() ? BoosterType.ROW : BoosterType.COLUMN;
                grid[r][c].setBooster(bonus);
                grid[r][c].setTileColor(Color.BLACK);
            }
        }

        // Case 3: Color bomb + normal tile - clear all tiles of that color
        else if (target.getTileColor() != null) {
            Color targetColor = target.getTileColor();
            for (int r = 0; r < grid.length; r++) {
                for (int c = 0; c < grid[0].length; c++) {
                    if (targetColor.equals(grid[r][c].getTileColor())) {
                        grid[r][c].setTileColor(null);
                        grid[r][c].setBooster(BoosterType.NONE);
                    }
                }
            }
        }

        // Remove the color bomb tile itself
        bomb.setTileColor(null);
        bomb.setBooster(BoosterType.NONE);
    }
}
