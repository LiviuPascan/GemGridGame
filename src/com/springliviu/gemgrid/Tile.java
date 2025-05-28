package com.springliviu.gemgrid;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

// Represents a single tile in the game
public class Tile extends Rectangle {
    private final int row;
    private final int col;
    private Color color;

    public Tile(int row, int col, int size, Color color) {
        super(size, size, color);
        this.row = row;
        this.col = col;
        this.color = color;
        setArcWidth(10);
        setArcHeight(10);
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public Color getTileColor() {
        return color;
    }

    public void setTileColor(Color color) {
        this.color = color;
        setFill(color);
    }
}
