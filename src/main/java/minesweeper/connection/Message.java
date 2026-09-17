package minesweeper.connection;

import java.io.Serializable;

import minesweeper.game.Board;

public record Message(
        MessageType type,
        Board board,
        Action action,
        String text
) implements Serializable {

    public static Message action(Action action) {
        return new Message(
                MessageType.ACTION,
                null,
                action,
                null
        );
    }

    public static Message board(Board board) {
        return new Message(
                MessageType.BOARD,
                board,
                null,
                null
        );
    }

    public static Message info(String text) {
        return new Message(
                MessageType.INFO,
                null,
                null,
                text
        );
    }

    public static Message error(String text) {
        return new Message(
                MessageType.ERROR,
                null,
                null,
                text
        );
    }
}
