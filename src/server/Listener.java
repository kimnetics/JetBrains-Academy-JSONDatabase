package server;

import server.util.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;

public final class Listener {
    private static final Listener instance = new Listener();

    private Listener() {
    }

    public static Listener getInstance() {
        return instance;
    }

    private static final int SERVER_PORT = 20123;

    private boolean stopListenerFlag = false;
    private int socketTimeout = 20;

    /**
     * Start listening on port.
     *
     * @param logger Logger to log to.
     * @param port   Port to listen to.
     */
    public void startListening(Logger logger) {
        logger.console("Server started!");

        // Wait for connections.
        while (!stopListenerFlag) {
            try (var server = new ServerSocket(SERVER_PORT)) {
                // Timeout periodically to allow checking stop flag.
                // (The testing system wanted a way to request that the server stop.)
                server.setSoTimeout(socketTimeout);

                // Start a session handler for connection.
                var sessionHandler = new RequestHandler(this, logger, server.accept());
                sessionHandler.start();
            } catch (SocketTimeoutException ignored) {
            } catch (IOException e) {
                String message = "Unexpected error while listening. " + e;
                logger.error(message);
                throw new RuntimeException(message);
            }
        }
    }

    /**
     * Stop listening on port.
     */
    public void stopListening() {
        stopListenerFlag = true;
    }
}
