package minesweeper.server;

import minesweeper.game.Board;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static final int PORT = 5000;

    private final Board board;

    public Server(Board board) {
        this.board = board;
    }

    public void start() {

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println("Minesweeper server started on port " + PORT);

            while (!board.isFinished()) {

                Socket socket = serverSocket.accept();

                System.out.println("New client connected.");

                HandlerClient handler =
                        new HandlerClient(socket, board);

                Thread thread = new Thread(handler);
                thread.start();
            }

        } catch (IOException e) {

            System.out.println(
                    "Error starting server: " + e.getMessage()
            );
        }
    }

    public static void main(String[] args) {

        Board board = new Board(10, 10, 15);

        Server server = new Server(board);

        server.start();
    }
}
