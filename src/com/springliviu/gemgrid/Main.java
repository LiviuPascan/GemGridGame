package com.springliviu.gemgrid;

import com.springliviu.gemgrid.services.BoosterHandler;
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

        // Обновлено: сначала скрываем меню, затем запускаем игру
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
                tiles[row][col].setSelected(false);
            }
        }

        // Удалить стартовые совпадения
        removeInitialMatches();
    }

    // Удаляет начальные совпадения, чтобы не было автоматических бонусов
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
            // First tile selected
            selectedTile = tile;
            tile.setSelected(true);
        } else {
            if (tile == selectedTile) {
                // Deselect if clicked again
                tile.setSelected(false);
                selectedTile = null;
                return;
            }

            if (areAdjacent(selectedTile, tile)) {
                Tile first = selectedTile;
                Tile second = tile;

                BoosterType boosterA = first.getBooster();
                BoosterType boosterB = second.getBooster();

                // Deselect before executing action
                selectedTile.setSelected(false);
                selectedTile = null;

                if (boosterA == BoosterType.COLOR_BOMB || boosterB == BoosterType.COLOR_BOMB) {
                    BoosterHandler.triggerColorBombCombo(first, second, tiles, random);
                    endAction();
                } else if (boosterA != BoosterType.NONE) {
                    BoosterHandler.activateBooster(first, tiles, this::endAction);
                } else if (boosterB != BoosterType.NONE) {
                    BoosterHandler.activateBooster(second, tiles, this::endAction);
                } else {
                    swapTiles(first, second);
                    if (TileUtils.isMatch(tiles, first.getRow(), first.getCol()) ||
                            TileUtils.isMatch(tiles, second.getRow(), second.getCol())) {
                        endAction();
                    } else {
                        swapTiles(first, second);
                    }
                }
            } else {
                // Switch selected tile
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
                // Назначить бонусы
                Map<Tile, BoosterType> boosters = TileUtils.classifyMatchesAndBoosters(tiles, matched);

                for (Tile tile : matched) {
                    // Пропускаем плитки, которым назначены бустеры
                    if (boosters.containsKey(tile)) continue;

                    tile.setTileColor(null);
                    tile.setBooster(BoosterType.NONE);
                    score += 10;
                }

                // Применяем бустеры после удаления обычных
                for (Map.Entry<Tile, BoosterType> entry : boosters.entrySet()) {
                    Tile tile = entry.getKey();
                    tile.setBooster(entry.getValue());
                    tile.setTileColor(Color.BLACK); // для видимости
                }

                updateScore();

                Platform.runLater(this::endAction); // повторно, пока есть совпадения
            }
        });
    }


    public static void main(String[] args) {
        launch(args);
    }
}
