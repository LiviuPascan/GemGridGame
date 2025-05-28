package com.springliviu.gemgrid;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Random;

public class Main extends Application {

    private static final int TILE_SIZE = 50;
    private static final int GRID_SIZE = 8;

    private Tile[][] tiles = new Tile[GRID_SIZE][GRID_SIZE];
    private Tile selectedTile = null;
    private GridPane grid;
    private MenuOverlay menu;
    private Label scoreLabel;
    private int score = 0;
    private final Random random = new Random();

    @Override
    public void start(Stage stage) {
        grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(10));

        initGrid();

        scoreLabel = new Label("Score: 0");
        scoreLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        StackPane.setAlignment(scoreLabel, Pos.TOP_RIGHT);
        StackPane.setMargin(scoreLabel, new Insets(10));

        menu = new MenuOverlay(() -> {
            startGame();
            menu.setVisible(false);
        }, Platform::exit);
        menu.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        StackPane root = new StackPane(grid, scoreLabel, menu);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("GemGrid");
        stage.show();

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                menu.setVisible(!menu.isVisible());
            }
        });
    }

    private void initGrid() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Tile tile = new Tile(row, col, TILE_SIZE, randomColor());
                tiles[row][col] = tile;
                tile.setOnMouseClicked(event -> handleClick(tile, event));
                grid.add(tile, col, row);
            }
        }
    }

    private void startGame() {
        score = 0;
        updateScore();

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                tiles[row][col].setTileColor(randomColor());
            }
        }

        Platform.runLater(this::checkMatchesAndClear);
    }

    private void handleClick(Tile tile, MouseEvent event) {
        if (menu.isVisible()) return;

        if (selectedTile == null) {
            selectedTile = tile;
            highlightTile(tile, true);
        } else {
            highlightTile(selectedTile, false);
            if (areAdjacent(selectedTile, tile)) {
                swapTiles(selectedTile, tile);
                if (checkForMatch(selectedTile) || checkForMatch(tile)) {
                    checkMatchesAndClear();
                } else {
                    swapTiles(selectedTile, tile); // revert swap
                }
            }
            selectedTile = null;
        }
    }

    private void highlightTile(Tile tile, boolean on) {
        tile.setEffect(on ? new DropShadow(10, Color.BLACK) : null);
    }

    private boolean areAdjacent(Tile a, Tile b) {
        int dr = Math.abs(a.getRow() - b.getRow());
        int dc = Math.abs(a.getCol() - b.getCol());
        return (dr + dc == 1);
    }

    private void swapTiles(Tile a, Tile b) {
        Color temp = a.getTileColor();
        a.setTileColor(b.getTileColor());
        b.setTileColor(temp);
    }

    private boolean hasMatchAnywhere() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (checkForMatch(tiles[row][col])) return true;
            }
        }
        return false;
    }

    private boolean checkForMatch(Tile tile) {
        int row = tile.getRow();
        int col = tile.getCol();
        Color color = tile.getTileColor();

        int count = 1;
        for (int i = col - 1; i >= 0 && tiles[row][i].getTileColor().equals(color); i--) count++;
        for (int i = col + 1; i < GRID_SIZE && tiles[row][i].getTileColor().equals(color); i++) count++;
        if (count >= 3) return true;

        count = 1;
        for (int i = row - 1; i >= 0 && tiles[i][col].getTileColor().equals(color); i--) count++;
        for (int i = row + 1; i < GRID_SIZE && tiles[i][col].getTileColor().equals(color); i++) count++;
        return count >= 3;
    }

    private void checkMatchesAndClear() {
        boolean[][] toClear = new boolean[GRID_SIZE][GRID_SIZE];

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE - 2; col++) {
                Color a = tiles[row][col].getTileColor();
                Color b = tiles[row][col + 1].getTileColor();
                Color c = tiles[row][col + 2].getTileColor();
                if (a.equals(b) && b.equals(c)) {
                    toClear[row][col] = toClear[row][col + 1] = toClear[row][col + 2] = true;
                }
            }
        }

        for (int col = 0; col < GRID_SIZE; col++) {
            for (int row = 0; row < GRID_SIZE - 2; row++) {
                Color a = tiles[row][col].getTileColor();
                Color b = tiles[row + 1][col].getTileColor();
                Color c = tiles[row + 2][col].getTileColor();
                if (a.equals(b) && b.equals(c)) {
                    toClear[row][col] = toClear[row + 1][col] = toClear[row + 2][col] = true;
                }
            }
        }

        int cleared = 0;

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (toClear[row][col]) {
                    tiles[row][col].setTileColor(null);
                    cleared++;
                }
            }
        }

        if (cleared > 0) {
            score += cleared * 10;
            updateScore();

            Platform.runLater(this::applyGravity);
        }
    }

    private void applyGravity() {
        for (int col = 0; col < GRID_SIZE; col++) {
            int empty = GRID_SIZE - 1;
            for (int row = GRID_SIZE - 1; row >= 0; row--) {
                if (tiles[row][col].getTileColor() != null) {
                    Color color = tiles[row][col].getTileColor();
                    tiles[empty][col].setTileColor(color);
                    if (empty != row) {
                        tiles[row][col].setTileColor(null);
                    }
                    empty--;
                }
            }

            for (int row = empty; row >= 0; row--) {
                tiles[row][col].setTileColor(randomColor());
            }
        }

        Platform.runLater(() -> {
            if (hasMatchAnywhere()) {
                checkMatchesAndClear();
            }
        });
    }

    private void updateScore() {
        scoreLabel.setText("Score: " + score);
    }

    private Color randomColor() {
        Color[] colors = {
                Color.RED, Color.BLUE, Color.GREEN,
                Color.YELLOW, Color.ORANGE, Color.PURPLE
        };
        return colors[random.nextInt(colors.length)];
    }

    public static void main(String[] args) {
        launch(args);
    }
}
