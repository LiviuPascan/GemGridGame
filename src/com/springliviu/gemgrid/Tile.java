package com.springliviu.gemgrid;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

// Represents a single tile in the game grid
public class Tile extends Rectangle {
    private final int row;
    private final int col;
    private Color color;
    private BoosterType booster = BoosterType.NONE;

    public Tile(int row, int col, int size, Color color) {
        super(size, size);
        this.row = row;
        this.col = col;
        this.color = color;
        updateAppearance();
        setArcWidth(10);
        setArcHeight(10);
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

    // Update visual based on booster type
    private void updateAppearance() {
        if (booster == BoosterType.ROW) {
            setFill(Color.BLACK);
            setHeight(15);
            setWidth(50);
        } else if (booster == BoosterType.COLUMN) {
            setFill(Color.BLACK);
            setWidth(15);
            setHeight(50);
        } else if (booster == BoosterType.COLOR_BOMB) {
            setFill(Color.WHITE);
            setWidth(50);
            setHeight(50);
        } else {
            setWidth(50);
            setHeight(50);
            setFill(color);
        }
    }
}
