package minesweeper.connection;

import java.io.Serializable;

public record Action(
        ActionType type,
        int row,
        int column
) implements Serializable {
}
