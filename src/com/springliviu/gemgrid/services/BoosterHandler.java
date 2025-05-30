package com.springliviu.gemgrid.services;

import com.springliviu.gemgrid.BoosterType;
import com.springliviu.gemgrid.Main;
import com.springliviu.gemgrid.Tile;
import javafx.application.Platform;
import javafx.scene.paint.Color;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class BoosterHandler {

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

    public static void triggerLineBoosterCombo(Tile center, Tile[][] grid, Runnable postAction) {
        int row = center.getRow();
        int col = center.getCol();
        Set<Tile> affected = new HashSet<>();

        for (int c = 0; c < grid[0].length; c++) {
            affected.add(grid[row][c]);
        }
        for (int r = 0; r < grid.length; r++) {
            affected.add(grid[r][col]);
        }

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

    public static void triggerColorBombCombo(Tile a, Tile b, Tile[][] grid, Random random) {
        Tile bomb = (a.getBooster() == BoosterType.COLOR_BOMB) ? a : b;
        Tile target = (bomb == a) ? b : a;

        if (a.getBooster() == BoosterType.COLOR_BOMB && b.getBooster() == BoosterType.COLOR_BOMB) {
            Set<Tile> all = new HashSet<>();
            for (int r = 0; r < grid.length; r++) {
                for (int c = 0; c < grid[0].length; c++) {
                    if (grid[r][c].getTileColor() != null) {
                        all.add(grid[r][c]);
                    }
                }
            }

            int[] remaining = {all.size()};
            for (Tile tile : all) {
                GridAnimator.fadeOutTile(tile, () -> {
                    remaining[0]--;
                    if (remaining[0] == 0) {
                        Platform.runLater(() ->
                                GridManipulator.applyGravity(grid, random, Main::triggerEndAction)
                        );
                    }
                });
            }

            Main.addToScore(all.size() * 10);
            a.setTileColor(null);
            a.setBooster(BoosterType.NONE);
            b.setTileColor(null);
            b.setBooster(BoosterType.NONE);
            return;
        }

        if (target.getBooster() == BoosterType.ROW || target.getBooster() == BoosterType.COLUMN) {
            int count = 5 + random.nextInt(11);
            for (int i = 0; i < count; i++) {
                int r = random.nextInt(grid.length);
                int c = random.nextInt(grid[0].length);
                BoosterType booster = random.nextBoolean() ? BoosterType.ROW : BoosterType.COLUMN;
                grid[r][c].setBooster(booster);
                grid[r][c].setTileColor(Color.BLACK);
            }

            Main.addToScore(count * 5);

            bomb.setTileColor(null);
            bomb.setBooster(BoosterType.NONE);
            target.setTileColor(null);
            target.setBooster(BoosterType.NONE);

            Platform.runLater(() ->
                    GridManipulator.applyGravity(grid, random, Main::triggerEndAction)
            );
            return;
        }

        if (target.getTileColor() != null) {
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
                                GridManipulator.applyGravity(grid, random, Main::triggerEndAction)
                        );
                    }
                });
            }

            Main.addToScore(toRemove.size() * 10);
            bomb.setTileColor(null);
            bomb.setBooster(BoosterType.NONE);
        }
    }
}
