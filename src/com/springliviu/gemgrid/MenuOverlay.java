package com.springliviu.gemgrid;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

// Fullscreen pause menu with New Game and Exit options
public class MenuOverlay extends StackPane {

    public MenuOverlay(Runnable onNewGame, Runnable onExit) {
        Rectangle bg = new Rectangle();
        bg.setFill(Color.WHITE);
        bg.widthProperty().bind(widthProperty());
        bg.heightProperty().bind(heightProperty());

        Button newGameBtn = new Button("New Game");
        Button exitBtn = new Button("Exit");

        newGameBtn.setOnAction(e -> onNewGame.run());
        exitBtn.setOnAction(e -> onExit.run());

        VBox box = new VBox(20, newGameBtn, exitBtn);
        box.setAlignment(Pos.CENTER);

        getChildren().addAll(bg, box);
    }
}
