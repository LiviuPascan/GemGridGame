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

    private static final int TILE_SIZE = 50;
    private static final int GRID_SIZE = 8;
    private final Tile[][] tiles = new Tile[GRID_SIZE][GRID_SIZE];
    private Tile selectedTile = null;
    private final Random random = new Random();

    @Override
    public void start(Stage stage) {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(5);
        grid.setVgap(5);

        // Create tile grid
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Color color = randomColor();
                Tile tile = new Tile(row, col, TILE_SIZE, color);
                tiles[row][col] = tile;
                tile.setOnMouseClicked(event -> handleClick(tile, event));
                grid.add(tile, col, row);
            }
        }

        // Clear any initial matches before game starts
        while (hasMatchAnywhere()) {
            checkMatchesAndClear();
        }

        Scene scene = new Scene(grid);
        stage.setTitle("GemGrid");
        stage.setScene(scene);
        stage.show();
    }

    // Handle click on a tile
    private void handleClick(Tile tile, MouseEvent event) {
        if (selectedTile == null) {
            selectedTile = tile;
            highlightTile(tile, true);
        } else {
            highlightTile(selectedTile, false);
            if (areAdjacent(selectedTile, tile)) {
                swapTiles(selectedTile, tile);

                // Only allow swap if it results in a match
                if (checkForMatch(selectedTile) || checkForMatch(tile)) {
                    System.out.println("✅ Match formed, swap accepted.");
                    checkMatchesAndClear();
                } else {
                    System.out.println("❌ No match, swap reverted.");
                    swapTiles(selectedTile, tile); // Undo swap
                }
            }
            selectedTile = null;
        }
    }

    // Highlight selected tile
    private void highlightTile(Tile tile, boolean highlight) {
        tile.setEffect(highlight ? new DropShadow(10, Color.BLACK) : null);
    }

    // Swap the colors between two tiles
    private void swapTiles(Tile a, Tile b) {
        System.out.println("Swapping: " + a.getRow() + "," + a.getCol() + " <-> " + b.getRow() + "," + b.getCol());
        Color temp = a.getTileColor();
        a.setTileColor(b.getTileColor());
        b.setTileColor(temp);
    }

    // Return true if tiles are directly adjacent (4-directional)
    private boolean areAdjacent(Tile a, Tile b) {
        int dr = Math.abs(a.getRow() - b.getRow());
        int dc = Math.abs(a.getCol() - b.getCol());
        return (dr + dc) == 1;
    }

    // Generate random color for tile
    private Color randomColor() {
        Color[] colors = {
                Color.RED, Color.BLUE, Color.GREEN,
                Color.YELLOW, Color.ORANGE, Color.PURPLE
        };
        return colors[random.nextInt(colors.length)];
    }

    // Check entire board for matches (used after gravity)
    private boolean hasMatchAnywhere() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (checkForMatch(tiles[row][col])) {
                    return true;
                }
            }
        }
        return false;
    }

    // Check if a tile is part of a 3+ match (horizontal or vertical)
    private boolean checkForMatch(Tile tile) {
        int row = tile.getRow();
        int col = tile.getCol();
        Color color = tile.getTileColor();

        // Horizontal
        int count = 1;
        for (int i = col - 1; i >= 0 && tiles[row][i].getTileColor().equals(color); i--) count++;
        for (int i = col + 1; i < GRID_SIZE && tiles[row][i].getTileColor().equals(color); i++) count++;
        if (count >= 3) return true;

        // Vertical
        count = 1;
        for (int i = row - 1; i >= 0 && tiles[i][col].getTileColor().equals(color); i--) count++;
        for (int i = row + 1; i < GRID_SIZE && tiles[i][col].getTileColor().equals(color); i++) count++;
        return count >= 3;
    }

    // Find and clear matches, then apply gravity
    private void checkMatchesAndClear() {
        boolean[][] toClear = new boolean[GRID_SIZE][GRID_SIZE];

        // Horizontal scan
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

        // Vertical scan
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

        boolean cleared = false;
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (toClear[row][col]) {
                    tiles[row][col].setTileColor(Color.TRANSPARENT);
                    cleared = true;
                }
            }
        }

        if (cleared) {
            applyGravity();
        }
    }

    // Drop non-empty tiles down and fill blanks
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

            for (int row = emptyRow; row >= 0; row--) {
                tiles[row][col].setTileColor(randomColor());
            }
        }

        // Continue clearing if more matches appear
        if (hasMatchAnywhere()) {
            checkMatchesAndClear();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
