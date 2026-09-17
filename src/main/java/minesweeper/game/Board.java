package minesweeper.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Board implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int row;
    private final int columns;
    private final int totalMines;

    private final boolean[][] mines;
    private final boolean[][] reveal;
    private final int[][] nextTo;

    private boolean finishGame = false;
    private boolean win = false;

    // Observadores del tablero
    private transient List<BoardObserver> observers = new ArrayList<>();

    public Board(int row, int columns, int totalMines) {

        if (row <= 0 || columns <= 0) {
            throw new IllegalArgumentException(
                    "Rows and columns must be greater than zero."
            );
        }

        if (totalMines < 0 || totalMines >= row * columns) {
            throw new IllegalArgumentException(
                    "Invalid number of mines."
            );
        }

        this.row = row;
        this.columns = columns;
        this.totalMines = totalMines;

        this.mines = new boolean[row][columns];
        this.reveal = new boolean[row][columns];
        this.nextTo = new int[row][columns];

        // Crear tablero
        putMines();
        calculateNextTo();
    }

    /**
     * Comprueba si una posición está dentro del tablero.
     */
    private boolean inside(int r, int c) {

        return r >= 0 && r < row
                && c >= 0 && c < columns;
    }

    /**
     * Coloca las minas aleatoriamente.
     */
    private void putMines() {

        Random random = new Random();

        int put = 0;

        while (put < totalMines) {

            int r = random.nextInt(row);
            int c = random.nextInt(columns);

            if (!mines[r][c]) {

                mines[r][c] = true;
                put++;
            }
        }
    }

    /**
     * Calcula cuántas minas hay alrededor de cada casilla.
     */
    private void calculateNextTo() {

        for (int r = 0; r < row; r++) {

            for (int c = 0; c < columns; c++) {

                // Las minas no necesitan número
                if (mines[r][c]) {
                    continue;
                }

                int count = 0;

                for (int df = -1; df <= 1; df++) {

                    for (int dc = -1; dc <= 1; dc++) {

                        int nf = r + df;
                        int nc = c + dc;

                        if (inside(nf, nc) && mines[nf][nc]) {
                            count++;
                        }
                    }
                }

                nextTo[r][c] = count;
            }
        }
    }

    /**
     * Devuelve la lista de observadores.
     *
     * El campo es transient, asi que en una copia del tablero
     * recibida por la red llega en null y hay que recrearla.
     */
    private List<BoardObserver> observers() {

        if (observers == null) {
            observers = new ArrayList<>();
        }

        return observers;
    }

    /**
     * Registra un observador.
     */
    public synchronized void addObserver(BoardObserver observer) {

        if (observer != null && !observers().contains(observer)) {
            observers().add(observer);
        }
    }

    /**
     * Elimina un observador.
     */
    public synchronized void removeObserver(BoardObserver observer) {

        observers().remove(observer);
    }

    /**
     * Notifica a todos los observadores que el tablero cambió.
     */
    private void notifyObservers() {

        for (BoardObserver observer : observers()) {
            observer.boardUpdated(this);
        }
    }

    /**
     * Revela una posición del tablero.
     */
    public synchronized String reveal(int r, int c) {

        if (finishGame) {
            return "The game is finished.";
        }

        if (!inside(r, c)) {
            return "Position out of the board.";
        }

        if (reveal[r][c]) {
            return "That cell was already exposed.";
        }

        // El jugador encontró una mina
        if (mines[r][c]) {

            reveal[r][c] = true;
            finishGame = true;
            win = false;

            notifyObservers();

            return "Boom! You stepped on a mine. Game over.";
        }

        // Revelar la casilla y, si es cero,
        // revelar las casillas vecinas.
        waterfall(r, c);

        // Comprobar victoria
        if (haveWin()) {

            finishGame = true;
            win = true;

            notifyObservers();

            return "You WIN! All safe cells have been revealed.";
        }

        // El tablero cambió
        notifyObservers();

        return "Exposed cell.";
    }

    /**
     * Revela una casilla y realiza la cascada cuando
     * la casilla no tiene minas alrededor.
     */
    private void waterfall(int r, int c) {

        if (!inside(r, c)
                || reveal[r][c]
                || mines[r][c]) {

            return;
        }

        reveal[r][c] = true;

        // Si no hay minas alrededor, revelar vecinos
        if (nextTo[r][c] == 0) {

            for (int df = -1; df <= 1; df++) {

                for (int dc = -1; dc <= 1; dc++) {

                    // No volver a llamar a la misma casilla
                    if (df != 0 || dc != 0) {

                        waterfall(
                                r + df,
                                c + dc
                        );
                    }
                }
            }
        }
    }

    /**
     * Comprueba si todas las casillas que no son minas
     * ya fueron reveladas.
     */
    private boolean haveWin() {

        for (int r = 0; r < row; r++) {

            for (int c = 0; c < columns; c++) {

                if (!mines[r][c] && !reveal[r][c]) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Devuelve una representación del tablero.
     *
     * .  = casilla oculta
     * *  = mina (solo cuando terminó el juego)
     *   = casilla vacía
     * 1-8 = cantidad de minas cercanas
     */
    public synchronized String view() {

        StringBuilder sb = new StringBuilder();

        // Encabezado de columnas
        sb.append("    ");

        for (int c = 0; c < columns; c++) {
            sb.append(String.format("%2d ", c));
        }

        sb.append("\n");

        // Filas
        for (int r = 0; r < row; r++) {

            sb.append(String.format("%2d  ", r));

            for (int c = 0; c < columns; c++) {

                if (mines[r][c] && finishGame) {

                    sb.append("*  ");

                } else if (!reveal[r][c]) {

                    sb.append(".  ");

                } else if (nextTo[r][c] == 0) {

                    sb.append("   ");

                } else {

                    sb.append(
                            String.format(
                                    "%d  ",
                                    nextTo[r][c]
                            )
                    );
                }
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Indica si el juego terminó.
     */
    public synchronized boolean isFinished() {

        return finishGame;
    }

    /**
     * Indica si el jugador ganó.
     */
    public synchronized boolean isWin() {

        return win;
    }

    /**
     * Devuelve el número de filas.
     */
    public int getRows() {

        return row;
    }

    /**
     * Devuelve el número de columnas.
     */
    public int getColumns() {

        return columns;
    }

    /**
     * Devuelve el número total de minas.
     */
    public int getTotalMines() {

        return totalMines;
    }
}