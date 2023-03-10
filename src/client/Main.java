package client;

import client.util.Logger;
import com.beust.jcommander.JCommander;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public final class Main {
    public static Logger logger;

    private static final Sender sender = Sender.getInstance();

    private static final String inputPath = System.getProperty("user.dir") + "/src/client/data/";

    public static void main(String[] args) {
        // Initialize logger.
        logger = new Logger(Main.class.getName(), "%h/json-database-client.log");

        logger.info("JSON Database client started.");

        logger.console("Client started!");

        // Create client data directory if it does not exist.
        Path clientDataDirectory = Paths.get(inputPath);
        if (Files.notExists(clientDataDirectory)) {
            try {
                Files.createDirectory(clientDataDirectory);
                logger.info("Created client data directory.");
            } catch (IOException e) {
                String message = "Unexpected error creating client data directory. " + e;
                logger.console(message, Logger.Severity.ERROR);
                throw new RuntimeException(message);
            }
        }

        // Parse command line arguments.
        var argsParsed = new ArgsParsed();
        JCommander.newBuilder()
                .addObject(argsParsed)
                .build()
                .parse(args);

        String requestAsString;

        // Was a request input file specified?
        if ((argsParsed.requestInputFile() != null) && (!argsParsed.requestInputFile().isEmpty())) {
            try {
                String requestInputFile = inputPath + argsParsed.requestInputFile();
                logger.info(String.format("Using request input file %s", requestInputFile));
                Path inputFilePath = Paths.get(requestInputFile);
                requestAsString = new String(Files.readAllBytes(inputFilePath));
            } catch (IOException e) {
                String message = "Unexpected error reading request input file. " + e;
                logger.console(message, Logger.Severity.ERROR);
                throw new RuntimeException(message);
            }
        } else {
            // Build request.
            Map<String, String> requestAsMap = new HashMap<>();
            switch (argsParsed.requestType().toLowerCase()) {
                case ("set") -> {
                    // Combine multiple word values into single string.
                    StringJoiner value = new StringJoiner(" ");
                    argsParsed.values().forEach(value::add);

                    requestAsMap.put("type", "set");
                    requestAsMap.put("key", argsParsed.key());
                    requestAsMap.put("value", value.toString());
                }
                case ("get") -> {
                    requestAsMap.put("type", "get");
                    requestAsMap.put("key", argsParsed.key());
                }
                case ("delete") -> {
                    requestAsMap.put("type", "delete");
                    requestAsMap.put("key", argsParsed.key());
                }
                case ("exit") -> requestAsMap.put("type", "exit");
            }
            var gson = new Gson();
            requestAsString = gson.toJson(requestAsMap);
        }

        // Send request.
        sender.setLogger(logger);
        sender.sendRequest(requestAsString);

        logger.info("JSON Database client ended.");
    }
}
