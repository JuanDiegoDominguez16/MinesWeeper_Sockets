package minesweeper.server;

import minesweeper.connection.Action;
import minesweeper.connection.Message;
import minesweeper.connection.MessageType;
import minesweeper.game.Board;
import minesweeper.game.BoardObserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class HandlerClient implements Runnable, BoardObserver {

    private final Socket socket;
    private final Board board;

    private ObjectInputStream in;
    private ObjectOutputStream out;

    public HandlerClient(Socket socket, Board board) {
        this.socket = socket;
        this.board = board;
    }

    @Override
    public void run() {

        try {

            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();

            in = new ObjectInputStream(socket.getInputStream());

            // Este cliente se convierte en Observer
            board.addObserver(this);

            // Enviar tablero inicial
            sendMessage(Message.board(board));

            while (!board.isFinished()) {

                Object object = in.readObject();

                if (!(object instanceof Message message)) {
                    continue;
                }

                if (message.type() == MessageType.ACTION) {

                    handleAction(message.action());
                }
            }

        } catch (IOException | ClassNotFoundException e) {

            System.out.println("Client disconnected.");

        } finally {

            board.removeObserver(this);

            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void handleAction(Action action) {

        if (action == null) {
            return;
        }

        switch (action.type()) {

            case REVEAL:

                // reveal() avisa a los observadores por su cuenta,
                // aqui solo devolvemos el texto al jugador que jugo.
                String result = board.reveal(
                        action.row(),
                        action.column()
                );

                sendMessage(Message.info(result));

                break;

            case FLAG:

                // Lo implementaremos después
                sendMessage(
                        Message.error("Flags are not supported yet.")
                );

                break;
        }
    }

    @Override
    public synchronized void boardUpdated(Board board) {

        sendMessage(Message.board(board));
    }

    private synchronized void sendMessage(Message message) {

        if (out == null) {
            return;
        }

        try {

            // Sin reset() el stream reenvia la primera version
            // del tablero, porque lo cachea por identidad.
            out.reset();

            out.writeObject(message);

            out.flush();

        } catch (IOException e) {

            System.out.println(
                    "Error sending message to client."
            );
        }
    }
}
