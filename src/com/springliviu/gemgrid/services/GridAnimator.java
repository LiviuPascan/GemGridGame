package com.springliviu.gemgrid.services;

import com.springliviu.gemgrid.Tile;
import com.springliviu.gemgrid.BoosterType;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

// Utility class for animating tile behavior
public class GridAnimator {

    // Fades out a tile before clearing it
    public static void fadeOutTile(Tile tile, Runnable onFinished) {
        FadeTransition fade = new FadeTransition(Duration.millis(200), tile);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);

        fade.setOnFinished(e -> {
            tile.setOpacity(1.0); // Reset opacity for future use
            tile.setTileColor(null);
            tile.setBooster(BoosterType.NONE);
            onFinished.run();
        });

        fade.play();
    }
}
