package com.springliviu.gemgrid;

import javafx.animation.Animation;
import javafx.animation.ScaleTransition;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

// Represents a single tile in the game grid
public class Tile extends Rectangle {
    private final int row;
    private final int col;
    private Color color;
    private BoosterType booster = BoosterType.NONE;
    private boolean selected = false;
    private ScaleTransition pulseEffect;

    public Tile(int row, int col, int size, Color color) {
        super(size, size);
        this.row = row;
        this.col = col;
        this.color = color;

        setArcWidth(10);
        setArcHeight(10);
        setStroke(Color.DARKGRAY);
        setStrokeWidth(2);
        updateAppearance();
    }

    public int getRow() { return row; }
    public int getCol() { return col; }
    public Color getTileColor() { return color; }
    public BoosterType getBooster() { return booster; }

    public void setTileColor(Color color) {
        this.color = color;
        updateAppearance();
    }

    public void setBooster(BoosterType booster) {
        this.booster = booster;
        updateAppearance();
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        updateAppearance();
    }

    private void updateAppearance() {
        // Cancel any existing animation
        if (pulseEffect != null) {
            pulseEffect.stop();
            pulseEffect = null;
        }

        if (booster == BoosterType.ROW) {
            setFill(Color.BLACK);
            setHeight(15);
            setWidth(50);
            setScaleX(1.0);
            setScaleY(1.0);
        } else if (booster == BoosterType.COLUMN) {
            setFill(Color.BLACK);
            setWidth(15);
            setHeight(50);
            setScaleX(1.0);
            setScaleY(1.0);
        } else if (booster == BoosterType.COLOR_BOMB) {
            setFill(Color.WHITE);
            setWidth(50);
            setHeight(50);

            pulseEffect = new ScaleTransition(Duration.millis(600), this);
            pulseEffect.setFromX(1.0);
            pulseEffect.setToX(1.1);
            pulseEffect.setFromY(1.0);
            pulseEffect.setToY(1.1);
            pulseEffect.setCycleCount(Animation.INDEFINITE);
            pulseEffect.setAutoReverse(true);
            pulseEffect.play();
        } else {
            setWidth(50);
            setHeight(50);
            setFill(color != null ? color : Color.TRANSPARENT);
            setScaleX(1.0);
            setScaleY(1.0);
        }

        if (selected) {
            DropShadow glow = new DropShadow();
            glow.setColor(Color.GOLD);
            glow.setRadius(15);
            setEffect(glow);
        } else {
            setEffect(null);
        }
    }
}
