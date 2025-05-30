# GemGridGame

A JavaFX-based match-3 puzzle game with animated tile interactions, boosters, and scoring. Designed for learning, experimentation, and potential expansion into a full-featured puzzle game.

## Features

- 8x8 grid of colorful tiles
- Match-3 logic with horizontal and vertical detection
- Booster tiles:
  - **ROW**: clears entire row
  - **COLUMN**: clears entire column
  - **COLOR_BOMB**: clears all tiles of a specific color
- Boosters spawn based on:
  - 4 in a row/column → ROW or COLUMN booster
  - 5 in a line or T/L/+ shape → COLOR_BOMB
- Animated tile disappearance and gravity
- Mouse click handling and tile swapping
- Scoring system with UI display
- Pause menu with "New Game" and "Exit"

## Technical Stack

- Java 11+
- JavaFX for GUI
- Maven or manual build
- Designed to run locally via `Main.java`

## Structure

- `Tile`: visual and logical representation of a grid cell
- `BoosterHandler`: applies booster effects and combos
- `TileUtils`: match detection and booster classification
- `GridManipulator`: handles gravity and tile refilling
- `GridAnimator`: runs fade/drop animations
- `Main`: entry point and UI controller

## Running the Game

1. Clone the repository
2. Open in IntelliJ (or any IDE with JavaFX support)
3. Run `Main.java`

JavaFX SDK must be configured if your IDE doesn't include it by default.

## License

MIT License – use freely with attribution.
