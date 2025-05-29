package com.springliviu.gemgrid.services;

import com.springliviu.gemgrid.BoosterType;
import com.springliviu.gemgrid.Tile;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.Random;

// Handles gravity and tile movements on the grid
public class GridManipulator {

    private static final int TILE_SIZE = 50;
    private static final int TILE_GAP = 5;

    public static void applyGravity(Tile[][] grid, Random random, Runnable after) {
        int gridSize = grid.length;

        for (int col = 0; col < gridSize; col++) {
            int emptyRow = gridSize - 1;

            for (int row = gridSize - 1; row >= 0; row--) {
                if (grid[row][col].getTileColor() != null) {
                    if (emptyRow != row) {
                        swapWithDropAnimation(grid, row, col, emptyRow, col);
                    }
                    emptyRow--;
                }
            }

            for (int row = emptyRow; row >= 0; row--) {
                Color newColor = randomColor(random);
                grid[row][col].setTileColor(newColor);
                grid[row][col].setBooster(BoosterType.NONE);
                animateDrop(grid[row][col], -1, row);
            }
        }

        Platform.runLater(after);
    }

    private static void swapWithDropAnimation(Tile[][] grid, int fromRow, int col, int toRow, int toCol) {
        Color color = grid[fromRow][col].getTileColor();
        BoosterType booster = grid[fromRow][col].getBooster();

        grid[toRow][toCol].setTileColor(color);
        grid[toRow][toCol].setBooster(booster);

        grid[fromRow][col].setTileColor(null);
        grid[fromRow][col].setBooster(BoosterType.NONE);

        animateDrop(grid[toRow][toCol], fromRow, toRow);
    }

    private static void animateDrop(Tile tile, int oldRow, int newRow) {
        double deltaY = (newRow - oldRow) * (TILE_SIZE + TILE_GAP);
        TranslateTransition tt = new TranslateTransition(Duration.millis(150), tile);
        tt.setFromY(-deltaY);
        tt.setToY(0);
        tt.play();

        // Optional: fade in effect for new tiles
        if (oldRow == -1) {
            tile.setOpacity(0);
            tile.setVisible(true);
            javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(Duration.millis(150), tile);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        }
    }

    private static Color randomColor(Random random) {
        Color[] colors = {
                Color.RED, Color.GREEN, Color.BLUE,
                Color.YELLOW, Color.ORANGE, Color.PURPLE
        };
        return colors[random.nextInt(colors.length)];
    }
}
