package client;

import client.util.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public final class Sender {
    private static final Sender instance = new Sender();

    private Sender() {
    }

    public static Sender getInstance() {
        return instance;
    }

    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 20123;

    private Logger logger;

    /**
     * Set logger.
     *
     * @param logger Logger to use.
     */
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * Send a request to server.
     *
     * @param request Request to send.
     */
    public void sendRequest(String request) {
        try (
                var serverSocket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                var inputStream = new DataInputStream(serverSocket.getInputStream());
                var outputStream = new DataOutputStream(serverSocket.getOutputStream())
        ) {
            // Send request to server.
            outputStream.writeUTF(request);
            logger.console(String.format("Sent: %s", request));

            // Receive response from server.
            String response = inputStream.readUTF();
            logger.console(String.format("Received: %s", response));
        } catch (IOException e) {
            String message = "Unexpected error communicating with server. " + e;
            logger.error(message);
            throw new RuntimeException(message);
        }
    }
}
