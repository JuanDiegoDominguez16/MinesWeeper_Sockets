package minesweeper.connection;

/**
 * Los enum ya son Serializable por definicion,
 * no hace falta declararlo.
 */
public enum MessageType {

    ACTION,
    BOARD,
    INFO,
    ERROR
}
