package com.springliviu.gemgrid;

import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

        // Game title / logo
        Label logoLabel = new Label("💎 GemGrid 💎");
        logoLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");

        // Score display
        scoreLabel = new Label("Score: 0");
        scoreLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Pause button to toggle menu overlay
        Button pauseButton = new Button("Pause");
        pauseButton.setOnAction(e -> menu.setVisible(!menu.isVisible()));

        // Horizontal top bar
        HBox topBar = new HBox(20, scoreLabel, pauseButton);
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setPadding(new Insets(0, 20, 0, 20));

        // Header layout
        VBox header = new VBox(logoLabel, topBar);
        header.setSpacing(5);
        header.setPadding(new Insets(10, 0, 0, 0));

        // Game overlay menu
        menu = new MenuOverlay(() -> {
            startGame();
            menu.setVisible(false);
        }, Platform::exit);
        menu.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        menu.setVisible(false);

        // StackPane to allow overlay
        StackPane gameArea = new StackPane(grid, menu);

        // Full layout
        VBox mainLayout = new VBox(header, gameArea);
        mainLayout.setSpacing(10);
        mainLayout.setPadding(new Insets(10));

        Scene scene = new Scene(mainLayout);
        stage.setScene(scene);
        stage.setTitle("GemGrid");
        stage.show();

        // Escape key toggles the pause menu
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                menu.setVisible(!menu.isVisible());
            }
        });
    }

    // Initialize the grid with Tile objects
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

    // Start or restart the game
    private void startGame() {
        score = 0;
        updateScore();

        // Fill grid with random colors
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                tiles[row][col].setTileColor(randomColor());
            }
        }

        // Ensure no initial matches exist
        clearInitialMatches();
    }

    // Regenerate tiles until no 3-in-a-row matches are present
    private void clearInitialMatches() {
        while (hasMatchAnywhere()) {
            for (int row = 0; row < GRID_SIZE; row++) {
                for (int col = 0; col < GRID_SIZE; col++) {
                    tiles[row][col].setTileColor(randomColor());
                }
            }
        }
    }

    // Handle tile clicks for selection and swapping
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

    // Highlight or un-highlight a tile
    private void highlightTile(Tile tile, boolean on) {
        tile.setEffect(on ? new javafx.scene.effect.DropShadow(10, Color.BLACK) : null);
    }

    // Check if two tiles are next to each other
    private boolean areAdjacent(Tile a, Tile b) {
        int dr = Math.abs(a.getRow() - b.getRow());
        int dc = Math.abs(a.getCol() - b.getCol());
        return (dr + dc == 1);
    }

    // Swap two tile colors
    private void swapTiles(Tile a, Tile b) {
        Color temp = a.getTileColor();
        a.setTileColor(b.getTileColor());
        b.setTileColor(temp);
    }

    // Check the entire grid for any match
    private boolean hasMatchAnywhere() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (checkForMatch(tiles[row][col])) return true;
            }
        }
        return false;
    }

    // Check if a tile is part of a match
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

    // Check and remove matches from the board
    private void checkMatchesAndClear() {
        boolean[][] toClear = new boolean[GRID_SIZE][GRID_SIZE];

        // Horizontal match check
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

        // Vertical match check
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

    // Drop tiles downward and fill empty spaces
    private void applyGravity() {
        for (int col = 0; col < GRID_SIZE; col++) {
            int empty = GRID_SIZE - 1;
            for (int row = GRID_SIZE - 1; row >= 0; row--) {
                if (tiles[row][col].getTileColor() != null) {
                    Color color = tiles[row][col].getTileColor();
                    if (empty != row) {
                        tiles[empty][col].setTileColor(color);
                        tiles[row][col].setTileColor(null);
                        animateDrop(tiles[empty][col], row, empty);
                    }
                    empty--;
                }
            }

            for (int row = empty; row >= 0; row--) {
                tiles[row][col].setTileColor(randomColor());
                animateDrop(tiles[row][col], -1, row);
            }
        }

        Platform.runLater(() -> {
            if (hasMatchAnywhere()) {
                checkMatchesAndClear();
            }
        });
    }

    // Animate tile drop
    private void animateDrop(Tile tile, int oldRow, int newRow) {
        double deltaY = (newRow - oldRow) * (TILE_SIZE + 5);
        TranslateTransition tt = new TranslateTransition(Duration.millis(150), tile);
        tt.setFromY(-deltaY);
        tt.setToY(0);
        tt.play();
    }

    // Update the score label
    private void updateScore() {
        scoreLabel.setText("Score: " + score);
    }

    // Generate a random color
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
