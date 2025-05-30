package com.springliviu.gemgrid;

import javafx.animation.Animation;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

// Represents a single tile in the game grid
public class Tile extends StackPane {
    private final int row;
    private final int col;
    private Color color;
    private BoosterType booster = BoosterType.NONE;
    private boolean selected = false;

    private final Rectangle background;
    private final Label symbolLabel;
    private ScaleTransition pulseEffect;

    public Tile(int row, int col, int size, Color color) {
        this.row = row;
        this.col = col;
        this.color = color;

        setPrefSize(size, size);
        setAlignment(Pos.CENTER);

        background = new Rectangle(size, size);
        background.setArcWidth(10);
        background.setArcHeight(10);
        background.setStroke(Color.DARKGRAY);
        background.setStrokeWidth(2);

        symbolLabel = new Label();
        symbolLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        symbolLabel.setTextFill(Color.WHITE);

        getChildren().addAll(background, symbolLabel);
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
        // Cancel pulse animation if active
        if (pulseEffect != null) {
            pulseEffect.stop();
            pulseEffect = null;
        }

        // Default style
        background.setWidth(50);
        background.setHeight(50);
        background.setStroke(Color.DARKGRAY);
        symbolLabel.setText("");
        symbolLabel.setTextFill(Color.WHITE);

        // Booster styles
        switch (booster) {
            case ROW:
                background.setFill(Color.BLACK);
                symbolLabel.setText("⇔");
                break;
            case COLUMN:
                background.setFill(Color.BLACK);
                symbolLabel.setText("⇕");
                break;
            case COLOR_BOMB:
                background.setFill(Color.WHITE);
                background.setStroke(Color.GRAY);
                symbolLabel.setText("✴");
                symbolLabel.setTextFill(Color.BLACK);

                pulseEffect = new ScaleTransition(Duration.millis(600), this);
                pulseEffect.setFromX(1.0);
                pulseEffect.setToX(1.1);
                pulseEffect.setFromY(1.0);
                pulseEffect.setToY(1.1);
                pulseEffect.setCycleCount(Animation.INDEFINITE);
                pulseEffect.setAutoReverse(true);
                pulseEffect.play();
                break;
            case NONE:
            default:
                background.setFill(color != null ? color : Color.TRANSPARENT);
                break;
        }

        // Selection effect
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
