package minesweeper.client;

import minesweeper.connection.Action;
import minesweeper.connection.ActionType;
import minesweeper.connection.Message;
import minesweeper.connection.MessageType;
import minesweeper.game.Board;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private static final String HOST = "localhost";
    private static final int PORT = 5000;

    // La pone en false el hilo receptor al terminar la partida,
    // o el hilo principal cuando el jugador escribe "get out".
    private static volatile boolean running = true;

    public static void main(String[] args) {

        try (
                Socket socket = new Socket(HOST, PORT);

                ObjectOutputStream out =
                        new ObjectOutputStream(socket.getOutputStream());

                ObjectInputStream in =
                        new ObjectInputStream(socket.getInputStream());

                Scanner scanner = new Scanner(System.in)
        ) {

            out.flush();

            System.out.println("Connected to Minesweeper!");

            Thread receiver = new Thread(() -> receiveMessages(in));

            receiver.setDaemon(true);
            receiver.start();

            while (running) {

                System.out.println(
                        "Enter move: row,column (or 'get out')"
                );

                // Si se cierra la entrada, salir sin excepcion
                if (!scanner.hasNextLine()) {
                    break;
                }

                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("get out")) {
                    break;
                }

                String[] parts = input.split(",");

                if (parts.length != 2) {

                    System.out.println(
                            "Invalid format. Use row,column"
                    );

                    continue;
                }

                try {

                    int row = Integer.parseInt(parts[0].trim());
                    int column = Integer.parseInt(parts[1].trim());

                    Action action = new Action(
                            ActionType.REVEAL,
                            row,
                            column
                    );

                    out.writeObject(
                            Message.action(action)
                    );

                    out.flush();

                } catch (NumberFormatException e) {

                    System.out.println(
                            "Row and column must be numbers."
                    );
                }
            }

        } catch (IOException e) {

            System.out.println(
                    "Connection error: " + e.getMessage()
            );

        } finally {

            // Para que el hilo receptor no avise de una
            // desconexion que en realidad pedimos nosotros.
            running = false;
        }
    }

    private static void receiveMessages(
            ObjectInputStream in
    ) {

        try {

            while (running) {

                Object object = in.readObject();

                if (!(object instanceof Message message)) {
                    continue;
                }

                if (message.type() == MessageType.BOARD) {

                    printBoard(message.board());

                } else if (message.type() == MessageType.INFO
                        || message.type() == MessageType.ERROR) {

                    System.out.println(message.text());
                }
            }

        } catch (IOException | ClassNotFoundException e) {

            if (running) {
                System.out.println("Disconnected from server.");
            }
        }
    }

    private static void printBoard(Board board) {

        if (board == null) {
            return;
        }

        System.out.println();
        System.out.println(board.view());

        if (board.isFinished()) {

            if (board.isWin()) {

                System.out.println(
                        "YOU WIN! All safe cells have been revealed."
                );

            } else {

                System.out.println(
                        "GAME OVER. A mine was revealed."
                );
            }

            running = false;

            // El hilo principal esta bloqueado leyendo teclado,
            // asi que cerramos el proceso desde aqui.
            System.exit(0);
        }
    }
}
