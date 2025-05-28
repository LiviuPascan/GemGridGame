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
    private static final int GRID_SIZE = 8;               // 8x8 board
    private final Tile[][] tiles = new Tile[GRID_SIZE][GRID_SIZE]; // Grid of all tiles
    private Tile selectedTile = null;                     // Currently selected tile
    private final Random random = new Random();           // Random color generator

    @Override
    public void start(Stage stage) {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(5);
        grid.setVgap(5);

        // Fill the grid with random colored tiles
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Color color = randomColor();
                Tile tile = new Tile(row, col, TILE_SIZE, color);
                tiles[row][col] = tile;

                // Add mouse click listener
                tile.setOnMouseClicked(event -> handleClick(tile, event));

                grid.add(tile, col, row);
            }
        }

        Scene scene = new Scene(grid);
        stage.setTitle("GemGrid - Match 3");
        stage.setScene(scene);
        stage.show();
    }

    // Handles tile selection and swapping
    private void handleClick(Tile tile, MouseEvent event) {
        if (selectedTile == null) {
            // First tile selected
            selectedTile = tile;
            highlightTile(tile, true);
        } else {
            // Second tile clicked
            highlightTile(selectedTile, false);
            if (areAdjacent(selectedTile, tile)) {
                swapTiles(selectedTile, tile);
                checkMatchesAndClear();
            }
            selectedTile = null;
        }
    }

    // Swaps the color of two tiles
    private void swapTiles(Tile a, Tile b) {
        Color temp = a.getTileColor();
        a.setTileColor(b.getTileColor());
        b.setTileColor(temp);
    }

    // Highlights or unhighlights a tile
    private void highlightTile(Tile tile, boolean highlight) {
        if (highlight) {
            tile.setEffect(new DropShadow(10, Color.BLACK));
        } else {
            tile.setEffect(null);
        }
    }

    // Checks if two tiles are adjacent (direct neighbors)
    private boolean areAdjacent(Tile a, Tile b) {
        int dr = Math.abs(a.getRow() - b.getRow());
        int dc = Math.abs(a.getCol() - b.getCol());
        return (dr + dc) == 1;
    }

    // Returns a random color from a fixed palette
    private Color randomColor() {
        Color[] colors = {
                Color.RED, Color.BLUE, Color.GREEN,
                Color.YELLOW, Color.ORANGE, Color.PURPLE
        };
        return colors[random.nextInt(colors.length)];
    }

    // Checks the board for 3-in-a-row (horizontal and vertical) and clears them
    private void checkMatchesAndClear() {
        boolean[][] toClear = new boolean[GRID_SIZE][GRID_SIZE];

        // Horizontal matches
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

        // Vertical matches
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

        // Clear all marked tiles
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (toClear[row][col]) {
                    tiles[row][col].setTileColor(Color.TRANSPARENT);
                }
            }
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
