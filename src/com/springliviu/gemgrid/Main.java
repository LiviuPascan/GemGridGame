package com.springliviu.gemgrid;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        // Создание текста
        Label label = new Label("Добро пожаловать в GemGrid");

        // Центровка текста
        StackPane root = new StackPane(label);

        // Сцена с размерами
        Scene scene = new Scene(root, 400, 300);
        stage.setTitle("GemGrid");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        // Запуск JavaFX
        launch();
    }
}
