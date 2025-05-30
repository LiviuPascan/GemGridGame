package com.springliviu.gemgrid;

import com.springliviu.gemgrid.services.BoosterHandler;
import com.springliviu.gemgrid.services.GridAnimator;
import com.springliviu.gemgrid.services.GridManipulator;
import com.springliviu.gemgrid.services.TileUtils;
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
    private static Main instance;
    @Override
    public void start(Stage stage) {
        grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(10));

        initGrid();

        Label logoLabel = new Label("GemGrid");
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
            menu.setVisible(false);
            Platform.runLater(this::startGame);
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
        instance = this;


        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                menu.setVisible(!menu.isVisible());
            }
        });

        startGame();
    }

    public static void addToScore(int points) {
        if (instance != null) {
            instance.score += points;
            instance.updateScore();
        }
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
                tiles[row][col].setSelected(false);
            }
        }

        removeInitialMatches();
    }

    private void removeInitialMatches() {
        while (TileUtils.findMatchedTiles(tiles).size() > 0) {
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

    private void handleClick(Tile tile, MouseEvent event) {
        if (menu.isVisible()) return;

        if (selectedTile == null) {
            selectedTile = tile;
            tile.setSelected(true);
        } else {
            if (tile == selectedTile) {
                tile.setSelected(false);
                selectedTile = null;
                return;
            }

            if (areAdjacent(selectedTile, tile)) {
                Tile first = selectedTile;
                Tile second = tile;

                BoosterType boosterA = first.getBooster();
                BoosterType boosterB = second.getBooster();

                selectedTile.setSelected(false);
                selectedTile = null;

                if (boosterA == BoosterType.COLOR_BOMB || boosterB == BoosterType.COLOR_BOMB) {
                    BoosterHandler.triggerColorBombCombo(first, second, tiles, random);
                    endAction();
                }
                // Swap and activate booster AFTER it moved
                else if (boosterA != BoosterType.NONE || boosterB != BoosterType.NONE) {
                    swapTiles(first, second);

                    Tile triggered = (boosterA != BoosterType.NONE) ? second : first;
                    BoosterHandler.activateBooster(triggered, tiles, this::endAction);
                }
                // Normal tiles: check for match
                else {
                    swapTiles(first, second);
                    if (TileUtils.isMatch(tiles, first.getRow(), first.getCol()) ||
                            TileUtils.isMatch(tiles, second.getRow(), second.getCol())) {
                        endAction();
                    } else {
                        swapTiles(first, second); // swap back if no match
                    }
                }
            } else {
                selectedTile.setSelected(false);
                selectedTile = tile;
                tile.setSelected(true);
            }
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

    private void endAction() {
        GridManipulator.applyGravity(tiles, random, () -> {
            Set<Tile> matched = TileUtils.findMatchedTiles(tiles);

            if (!matched.isEmpty()) {
                Map<Tile, BoosterType> boosters = TileUtils.classifyMatchesAndBoosters(tiles, matched);

                // Count how many tiles will fade out (non-booster matches)
                int totalToFade = 0;
                for (Tile tile : matched) {
                    if (!boosters.containsKey(tile)) {
                        totalToFade++;
                    }
                }

                int[] remaining = {totalToFade};

                // Start fading out matched tiles
                for (Tile tile : matched) {
                    if (boosters.containsKey(tile)) {
                        // Booster tiles stay; will be updated visually after fades
                        continue;
                    }

                    GridAnimator.fadeOutTile(tile, () -> {
                        remaining[0]--;
                        if (remaining[0] == 0) {
                            Platform.runLater(this::endAction); // Trigger again after all fades complete
                        }
                    });

                    score += 10;
                }

                // Apply boosters after fade begins to avoid removing them prematurely
                for (Map.Entry<Tile, BoosterType> entry : boosters.entrySet()) {
                    Tile tile = entry.getKey();
                    tile.setBooster(entry.getValue());

                    // Use black as neutral marker color; visual cue
                    tile.setTileColor(Color.BLACK);
                }

                updateScore();

                // If nothing needed to fade, continue loop immediately
                if (totalToFade == 0) {
                    Platform.runLater(this::endAction);
                }
            }
        });
    }



    public static void main(String[] args) {
        launch(args);
    }
}
