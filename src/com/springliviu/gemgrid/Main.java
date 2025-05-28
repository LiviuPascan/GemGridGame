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

import java.util.*;

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

        Label logoLabel = new Label("💎 GemGrid 💎");
        logoLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        scoreLabel = new Label("Score: 0");
        scoreLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button pauseButton = new Button("Pause");
        pauseButton.setOnAction(e -> menu.setVisible(!menu.isVisible()));

        HBox topBar = new HBox(20, scoreLabel, pauseButton);
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setPadding(new Insets(0, 20, 0, 20));

        VBox header = new VBox(logoLabel, topBar);
        header.setSpacing(5);
        header.setPadding(new Insets(10, 0, 0, 0));

        menu = new MenuOverlay(() -> {
            startGame();
            menu.setVisible(false);
        }, Platform::exit);
        menu.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        menu.setVisible(false);

        StackPane gameArea = new StackPane(grid, menu);
        VBox mainLayout = new VBox(header, gameArea);
        mainLayout.setSpacing(10);
        mainLayout.setPadding(new Insets(10));

        Scene scene = new Scene(mainLayout);
        stage.setScene(scene);
        stage.setTitle("GemGrid");
        stage.show();

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                menu.setVisible(!menu.isVisible());
            }
        });

        startGame();
    }

    private void initGrid() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Tile tile = new Tile(row, col, TILE_SIZE, randomColor());
                tiles[row][col] = tile;
                tile.setOnMouseClicked(e -> handleClick(tile, e));
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
                tiles[row][col].setBooster(BoosterType.NONE);
            }
        }

        clearInitialMatches();
    }

    private void clearInitialMatches() {
        while (hasMatchAnywhere()) {
            for (int row = 0; row < GRID_SIZE; row++) {
                for (int col = 0; col < GRID_SIZE; col++) {
                    tiles[row][col].setTileColor(randomColor());
                    tiles[row][col].setBooster(BoosterType.NONE);
                }
            }
        }
    }

    private void updateScore() {
        scoreLabel.setText("Score: " + score);
    }

    private Color randomColor() {
        Color[] colors = {
                Color.RED, Color.GREEN, Color.BLUE,
                Color.YELLOW, Color.ORANGE, Color.PURPLE
        };
        return colors[random.nextInt(colors.length)];
    }

    private boolean hasMatchAnywhere() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (checkForMatch(tiles[row][col])) return true;
            }
        }
        return false;
    }

    private void handleClick(Tile tile, MouseEvent event) {
        if (menu.isVisible()) return;

        // Activate booster if selected
        if (tile.getBooster() != BoosterType.NONE) {
            activateBooster(tile);
            return;
        }

        if (selectedTile == null) {
            selectedTile = tile;
        } else {
            if (areAdjacent(selectedTile, tile)) {
                swapTiles(selectedTile, tile);
                if (checkForMatch(selectedTile) || checkForMatch(tile)) {
                    checkMatchesAndClear();
                } else {
                    swapTiles(selectedTile, tile);
                }
            }
            selectedTile = null;
        }
    }


    private boolean areAdjacent(Tile a, Tile b) {
        int dr = Math.abs(a.getRow() - b.getRow());
        int dc = Math.abs(a.getCol() - b.getCol());
        return (dr + dc == 1);
    }

    private void swapTiles(Tile a, Tile b) {
        Color tempColor = a.getTileColor();
        BoosterType tempBooster = a.getBooster();
        a.setTileColor(b.getTileColor());
        a.setBooster(b.getBooster());
        b.setTileColor(tempColor);
        b.setBooster(tempBooster);
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
        Map<Tile, BoosterType> boostersToAdd = new HashMap<>();

        // Horizontal matches
        for (int row = 0; row < GRID_SIZE; row++) {
            int count = 1;
            for (int col = 1; col <= GRID_SIZE; col++) {
                boolean match = col < GRID_SIZE &&
                        tiles[row][col] != null &&
                        tiles[row][col - 1] != null &&
                        tiles[row][col].getTileColor() != null &&
                        tiles[row][col].getTileColor().equals(tiles[row][col - 1].getTileColor());

                if (match) {
                    count++;
                } else {
                    if (count >= 3) {
                        for (int i = col - count; i < col; i++) {
                            toClear[row][i] = true;
                        }

                        if (count == 4) {
                            Tile bonusTile = tiles[row][col - 2];
                            boostersToAdd.put(bonusTile, BoosterType.ROW);
                            System.out.println("Created booster: ROW at (" + row + "," + (col - 2) + ")");
                        } else if (count >= 5) {
                            Tile bonusTile = tiles[row][col - 3];
                            boostersToAdd.put(bonusTile, BoosterType.COLOR_BOMB);
                            System.out.println("Created booster: COLOR_BOMB at (" + row + "," + (col - 3) + ")");
                        }
                    }
                    count = 1;
                }
            }
        }

        // Vertical matches
        for (int col = 0; col < GRID_SIZE; col++) {
            int count = 1;
            for (int row = 1; row <= GRID_SIZE; row++) {
                boolean match = row < GRID_SIZE &&
                        tiles[row][col] != null &&
                        tiles[row - 1][col] != null &&
                        tiles[row][col].getTileColor() != null &&
                        tiles[row][col].getTileColor().equals(tiles[row - 1][col].getTileColor());

                if (match) {
                    count++;
                } else {
                    if (count >= 3) {
                        for (int i = row - count; i < row; i++) {
                            toClear[i][col] = true;
                        }

                        if (count == 4) {
                            Tile bonusTile = tiles[row - 2][col];
                            boostersToAdd.put(bonusTile, BoosterType.COLUMN);
                            System.out.println("Created booster: COLUMN at (" + (row - 2) + "," + col + ")");
                        } else if (count >= 5) {
                            Tile bonusTile = tiles[row - 3][col];
                            boostersToAdd.put(bonusTile, BoosterType.COLOR_BOMB);
                            System.out.println("Created booster: COLOR_BOMB at (" + (row - 3) + "," + col + ")");
                        }
                    }
                    count = 1;
                }
            }
        }

        // Preserve boosters BEFORE clearing
        for (Map.Entry<Tile, BoosterType> entry : boostersToAdd.entrySet()) {
            Tile tile = entry.getKey();
            if (tile.getTileColor() != null) {
                tile.setBooster(entry.getValue());
                // Prevent it from being cleared
                int r = tile.getRow();
                int c = tile.getCol();
                toClear[r][c] = false;
            }
        }

        int cleared = 0;
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (toClear[row][col]) {
                    tiles[row][col].setTileColor(null);
                    tiles[row][col].setBooster(BoosterType.NONE);
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
    private void activateBooster(Tile tile) {
        int row = tile.getRow();
        int col = tile.getCol();

        BoosterType booster = tile.getBooster();
        tile.setTileColor(null);
        tile.setBooster(BoosterType.NONE);

        switch (booster) {
            case ROW:
                for (int c = 0; c < GRID_SIZE; c++) {
                    tiles[row][c].setTileColor(null);
                    tiles[row][c].setBooster(BoosterType.NONE);
                }
                break;
            case COLUMN:
                for (int r = 0; r < GRID_SIZE; r++) {
                    tiles[r][col].setTileColor(null);
                    tiles[r][col].setBooster(BoosterType.NONE);
                }
                break;
            case COLOR_BOMB:
                Color targetColor = null;
                if (selectedTile != null && selectedTile != tile) {
                    targetColor = selectedTile.getTileColor();
                }
                if (targetColor != null) {
                    for (int r = 0; r < GRID_SIZE; r++) {
                        for (int c = 0; c < GRID_SIZE; c++) {
                            if (tiles[r][c].getTileColor().equals(targetColor)) {
                                tiles[r][c].setTileColor(null);
                                tiles[r][c].setBooster(BoosterType.NONE);
                            }
                        }
                    }
                }
                break;
        }

        score += 80;
        updateScore();
        Platform.runLater(this::applyGravity);
    }


    private void applyGravity() {
        for (int col = 0; col < GRID_SIZE; col++) {
            int empty = GRID_SIZE - 1;
            for (int row = GRID_SIZE - 1; row >= 0; row--) {
                if (tiles[row][col].getTileColor() != null) {
                    Color color = tiles[row][col].getTileColor();
                    BoosterType booster = tiles[row][col].getBooster();
                    if (empty != row) {
                        tiles[empty][col].setTileColor(color);
                        tiles[empty][col].setBooster(booster);
                        tiles[row][col].setTileColor(null);
                        tiles[row][col].setBooster(BoosterType.NONE);
                        animateDrop(tiles[empty][col], row, empty);
                    }
                    empty--;
                }
            }

            for (int row = empty; row >= 0; row--) {
                tiles[row][col].setTileColor(randomColor());
                tiles[row][col].setBooster(BoosterType.NONE);
                animateDrop(tiles[row][col], -1, row);
            }
        }

        Platform.runLater(() -> {
            if (hasMatchAnywhere()) {
                checkMatchesAndClear();
            }
        });
    }

    private void animateDrop(Tile tile, int oldRow, int newRow) {
        double deltaY = (newRow - oldRow) * (TILE_SIZE + 5);
        TranslateTransition tt = new TranslateTransition(Duration.millis(150), tile);
        tt.setFromY(-deltaY);
        tt.setToY(0);
        tt.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
