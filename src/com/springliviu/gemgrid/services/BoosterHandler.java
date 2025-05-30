package com.springliviu.gemgrid.services;

import com.springliviu.gemgrid.BoosterType;
import com.springliviu.gemgrid.Main;
import com.springliviu.gemgrid.Tile;
import javafx.application.Platform;
import javafx.scene.paint.Color;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

// Handles logic related to activating boosters on the grid
public class BoosterHandler {

    // Activates a single booster directly (not via swapping)
    public static void activateBooster(Tile tile, Tile[][] grid, Runnable postAction) {
        int row = tile.getRow();
        int col = tile.getCol();

        BoosterType booster = tile.getBooster();
        tile.setTileColor(null);
        tile.setBooster(BoosterType.NONE);

        Set<Tile> affected = new HashSet<>();

        switch (booster) {
            case ROW:
                for (int c = 0; c < grid[0].length; c++) {
                    affected.add(grid[row][c]);
                }
                break;
            case COLUMN:
                for (int r = 0; r < grid.length; r++) {
                    affected.add(grid[r][col]);
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
                                affected.add(grid[r][c]);
                            }
                        }
                    }
                }
                break;
        }

        // Animate and clear all affected tiles
        int[] remaining = {affected.size()};
        for (Tile t : affected) {
            GridAnimator.fadeOutTile(t, () -> {
                remaining[0]--;
                if (remaining[0] == 0) {
                    Platform.runLater(postAction);
                }
            });
        }

        Main.addToScore(affected.size() * 10);
    }

    // Triggers a combo when COLOR_BOMB is swapped with another tile
    public static void triggerColorBombCombo(Tile a, Tile b, Tile[][] grid, Random random) {
        Tile bomb = (a.getBooster() == BoosterType.COLOR_BOMB) ? a : b;
        Tile target = (bomb == a) ? b : a;

        // Case 1: Double color bomb - clear entire grid
        if (a.getBooster() == BoosterType.COLOR_BOMB && b.getBooster() == BoosterType.COLOR_BOMB) {
            Set<Tile> allTiles = new HashSet<>();
            for (int r = 0; r < grid.length; r++) {
                for (int c = 0; c < grid[0].length; c++) {
                    if (grid[r][c].getTileColor() != null) {
                        allTiles.add(grid[r][c]);
                    }
                }
            }

            int[] remaining = {allTiles.size()};
            for (Tile tile : allTiles) {
                GridAnimator.fadeOutTile(tile, () -> {
                    remaining[0]--;
                    if (remaining[0] == 0) {
                        Platform.runLater(() ->
                                GridManipulator.applyGravity(grid, random, () -> {})
                        );
                    }
                });
            }

            bomb.setTileColor(null);
            bomb.setBooster(BoosterType.NONE);
            target.setTileColor(null);
            target.setBooster(BoosterType.NONE);

            Main.addToScore(allTiles.size() * 10);
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
            Set<Tile> toRemove = new HashSet<>();

            for (int r = 0; r < grid.length; r++) {
                for (int c = 0; c < grid[0].length; c++) {
                    if (targetColor.equals(grid[r][c].getTileColor())) {
                        toRemove.add(grid[r][c]);
                    }
                }
            }

            int[] remaining = {toRemove.size()};
            for (Tile tile : toRemove) {
                GridAnimator.fadeOutTile(tile, () -> {
                    remaining[0]--;
                    if (remaining[0] == 0) {
                        Platform.runLater(() ->
                                GridManipulator.applyGravity(grid, random, () -> {})
                        );
                    }
                });
            }

            Main.addToScore(toRemove.size() * 10);
        }

        // Remove the color bomb tiles themselves
        bomb.setTileColor(null);
        bomb.setBooster(BoosterType.NONE);
        target.setTileColor(null);
        target.setBooster(BoosterType.NONE);
    }
}
