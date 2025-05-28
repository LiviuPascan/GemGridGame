package com.springliviu.gemgrid;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Random;

public class Main extends Application {

    private static final int TILE_SIZE = 50;              // Width and height of each tile
    private static final int GRID_SIZE = 8;               // Grid is 8x8
    private final Tile[][] tiles = new Tile[GRID_SIZE][GRID_SIZE]; // Tile matrix
    private Tile selectedTile = null;                     // Currently selected tile
    private final Random random = new Random();           // Random generator

    @Override
    public void start(Stage stage) {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(5);
        grid.setVgap(5);

        // Initialize grid with tiles
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Color color = randomColor();
                Tile tile = new Tile(row, col, TILE_SIZE, color);
                tiles[row][col] = tile;

                // Attach click handler
                tile.setOnMouseClicked(event -> handleClick(tile, event));

                grid.add(tile, col, row);
            }
        }

        Scene scene = new Scene(grid);
        stage.setTitle("GemGrid - Match 3");
        stage.setScene(scene);
        stage.show();
    }

    // Handles click selection and swap logic
    private void handleClick(Tile tile, MouseEvent event) {
        if (selectedTile == null) {
            selectedTile = tile;
            highlightTile(tile, true);
        } else {
            highlightTile(selectedTile, false);
            if (areAdjacent(selectedTile, tile)) {
                swapTiles(selectedTile, tile);
                checkMatchesAndClear();
            }
            selectedTile = null;
        }
    }

    // Highlights or unhighlights a tile
    private void highlightTile(Tile tile, boolean highlight) {
        if (highlight) {
            tile.setEffect(new DropShadow(10, Color.BLACK));
        } else {
            tile.setEffect(null);
        }
    }

    // Checks if two tiles are direct neighbors
    private boolean areAdjacent(Tile a, Tile b) {
        int dr = Math.abs(a.getRow() - b.getRow());
        int dc = Math.abs(a.getCol() - b.getCol());
        return (dr + dc) == 1;
    }

    // Swaps the color of two tiles
    private void swapTiles(Tile a, Tile b) {
        Color temp = a.getTileColor();
        a.setTileColor(b.getTileColor());
        b.setTileColor(temp);
    }

    // Checks for 3-in-a-row matches and clears them
    private void checkMatchesAndClear() {
        boolean[][] toClear = new boolean[GRID_SIZE][GRID_SIZE];

        // Check rows
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE - 2; col++) {
                Color c1 = tiles[row][col].getTileColor();
                Color c2 = tiles[row][col + 1].getTileColor();
                Color c3 = tiles[row][col + 2].getTileColor();

                if (c1.equals(c2) && c2.equals(c3) && !c1.equals(Color.TRANSPARENT)) {
                    toClear[row][col] = true;
                    toClear[row][col + 1] = true;
                    toClear[row][col + 2] = true;
                }
            }
        }

        // Check columns
        for (int col = 0; col < GRID_SIZE; col++) {
            for (int row = 0; row < GRID_SIZE - 2; row++) {
                Color c1 = tiles[row][col].getTileColor();
                Color c2 = tiles[row + 1][col].getTileColor();
                Color c3 = tiles[row + 2][col].getTileColor();

                if (c1.equals(c2) && c2.equals(c3) && !c1.equals(Color.TRANSPARENT)) {
                    toClear[row][col] = true;
                    toClear[row + 1][col] = true;
                    toClear[row + 2][col] = true;
                }
            }
        }

        boolean anyCleared = false;

        // Clear matched tiles
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (toClear[row][col]) {
                    tiles[row][col].setTileColor(Color.TRANSPARENT);
                    anyCleared = true;
                }
            }
        }

        // Apply gravity and check again
        if (anyCleared) {
            applyGravity();
        }
    }

    // Shifts all non-empty tiles down and fills empty spots with new tiles
    private void applyGravity() {
        for (int col = 0; col < GRID_SIZE; col++) {
            int emptyRow = GRID_SIZE - 1;

            for (int row = GRID_SIZE - 1; row >= 0; row--) {
                Color color = tiles[row][col].getTileColor();
                if (!color.equals(Color.TRANSPARENT)) {
                    tiles[emptyRow][col].setTileColor(color);
                    if (emptyRow != row) {
                        tiles[row][col].setTileColor(Color.TRANSPARENT);
                    }
                    emptyRow--;
                }
            }

            // Fill remaining cells at the top with new colors
            for (int row = emptyRow; row >= 0; row--) {
                tiles[row][col].setTileColor(randomColor());
            }
        }

        // Repeat the process if new matches appear
        checkMatchesAndClear();
    }

    // Returns a random color from a fixed set
    private Color randomColor() {
        Color[] colors = {
                Color.RED, Color.BLUE, Color.GREEN,
                Color.YELLOW, Color.ORANGE, Color.PURPLE
        };
        return colors[random.nextInt(colors.length)];
    }

    public static void main(String[] args) {
        launch();
    }
}
