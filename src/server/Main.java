package server;

import server.util.Logger;

public final class Main {
    public static Logger logger;

    private static final Listener listener = Listener.getInstance();

    public static void main(String[] args) {
        // Initialize logger.
        logger = new Logger(Main.class.getName(), "%h/json-database-server.log");

        logger.info("JSON Database server started.");

        listener.startListening(logger);

        logger.info("JSON Database server ended.");
    }
}
