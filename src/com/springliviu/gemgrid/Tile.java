package com.springliviu.gemgrid;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

// A single tile on the game board
public class Tile extends Rectangle {
    private final int row;         // Row index
    private final int col;         // Column index
    private Color color;           // Current color of the tile

    public Tile(int row, int col, int size, Color color) {
        super(size, size, color);
        this.row = row;
        this.col = col;
        this.color = color;
        setArcWidth(10);   // Rounded corners
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
