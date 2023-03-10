package server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import server.data.DataHandler;
import server.data.DataHandlerResult;
import server.util.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class RequestHandler extends Thread {
    private final Listener listener;
    private final Logger logger;
    private final Socket clientSocket;

    private DataHandler dataHandler = DataHandler.getInstance();

    /**
     * Set data handler instance to use.
     *
     * @param dataHandler Data handler instance to use.
     */
    public void setDataHandler(DataHandler dataHandler) {
        this.dataHandler = dataHandler;
    }

    public RequestHandler(Listener listener, Logger logger, Socket clientSocket) {
        this.listener = listener;
        this.logger = logger;
        this.clientSocket = clientSocket;
    }

    enum Command {
        SET("Set value", 2),
        GET("Get value", 1),
        DELETE("Delete value", 1),
        EXIT("Exit", 0);

        private final String label;
        private final int inputElementsCount;

        Command(String label, int inputElementsCount) {
            this.label = label;
            this.inputElementsCount = inputElementsCount;
        }

        public String label() {
            return label;
        }

        public int inputElementsCount() {
            return inputElementsCount;
        }
    }

    record CommandWithFields(
            Command command,
            String[] keys,
            JsonElement value
    ) {
    }

    enum Result {
        OK,
        ERROR
    }

    /**
     * Handle incoming requests.
     */
    @Override
    public void run() {
        try (
                var inputStream = new DataInputStream(clientSocket.getInputStream());
                var outputStream = new DataOutputStream(clientSocket.getOutputStream())
        ) {
            dataHandler.setLogger(logger);
            boolean stopListenerFlag = false;

            // Receive request from client.
            String request = inputStream.readUTF();
            logger.console(String.format("Received: %s", request));

            // Parse request into a command and fields.
            Optional<CommandWithFields> commandWithFieldsOptional = parseRequest(request);

            // Could we not parse request?
            JsonElement responseAsElement = new Gson().fromJson("{}", JsonElement.class);
            JsonObject responseAsObject = (JsonObject) responseAsElement;
            if (commandWithFieldsOptional.isEmpty()) {
                responseAsObject.addProperty("response", Result.ERROR.name());
            } else {
                // Handle command.
                CommandWithFields commandWithFields = commandWithFieldsOptional.get();
                switch (commandWithFields.command()) {
                    // Set value.
                    case SET -> {
                        DataHandlerResult dataHandlerResult = dataHandler.set(commandWithFields.keys(), commandWithFields.value());
                        responseAsObject.addProperty("response", dataHandlerResult.result().name());
                        if (dataHandlerResult.result() == DataHandler.Result.ERROR) {
                            responseAsObject.add("reason", dataHandlerResult.value());
                        }
                    }

                    // Get value.
                    case GET -> {
                        DataHandlerResult dataHandlerResult = dataHandler.get(commandWithFields.keys());
                        responseAsObject.addProperty("response", dataHandlerResult.result().name());
                        if (dataHandlerResult.result() == DataHandler.Result.ERROR) {
                            responseAsObject.add("reason", dataHandlerResult.value());
                        } else {
                            responseAsObject.add("value", dataHandlerResult.value());
                        }
                    }

                    // Delete value.
                    case DELETE -> {
                        DataHandlerResult dataHandlerResult = dataHandler.delete(commandWithFields.keys());
                        responseAsObject.addProperty("response", dataHandlerResult.result().name());
                        if (dataHandlerResult.result() == DataHandler.Result.ERROR) {
                            responseAsObject.add("reason", dataHandlerResult.value());
                        }
                    }

                    // Tell listener to stop listening.
                    case EXIT -> {
                        stopListenerFlag = true;
                        responseAsObject.addProperty("response", Result.OK.name());
                    }
                }
            }

            // Send response to client.
            String responseAsString = new Gson().toJson(responseAsObject);
            outputStream.writeUTF(responseAsString);
            logger.console(String.format("Sent: %s", responseAsString));

            // Close client connection.
            clientSocket.close();

            // Let listener know if client requested exit.
            if (stopListenerFlag) listener.stopListening();
        } catch (IOException e) {
            String message = "Unexpected error handling request. " + e;
            logger.console(message, Logger.Severity.ERROR);
            throw new RuntimeException(message);
        }
    }

    /**
     * Parse request into a command and fields.
     *
     * @param requestAsString Request to parse.
     * @return Optional with command and fields parsed from request. Returns empty
     * Optional if request was not successfully parsed.
     */
    Optional<CommandWithFields> parseRequest(String requestAsString) {
        final Type listType = new TypeToken<ArrayList<String>>() {
        }.getType();

        Optional<CommandWithFields> commandWithFields = Optional.empty();

        // Parse request.
        JsonElement requestAsElement;
        try {
            requestAsElement = new Gson().fromJson(requestAsString, JsonElement.class);
        } catch (com.google.gson.JsonParseException e) {
            return commandWithFields;
        }

        var requestAsObject = (JsonObject) requestAsElement;

        // Exit if request type is not present.
        if (!requestAsObject.has("type")) {
            return commandWithFields;
        }

        String requestType = requestAsObject.get("type").getAsString();

        // Check if known command was entered with valid fields.
        for (Command option : Command.values()) {
            if (option.name().equalsIgnoreCase(requestType)) {
                String[] keys = null;
                JsonElement value = null;

                // Are keys required?
                if (option.inputElementsCount() >= 1) {
                    // Exit if keys are not present.
                    if (!requestAsObject.has("key")) {
                        return commandWithFields;
                    }

                    // Prepare key.
                    JsonElement keyAsElement = requestAsObject.get("key");
                    if (keyAsElement.isJsonArray()) {
                        List<String> keyList = new Gson().fromJson(keyAsElement, listType);
                        keys = keyList.toArray(String[]::new);
                    } else {
                        keys = new String[]{keyAsElement.getAsString()};
                    }

                    // Is a value required?
                    if (option.inputElementsCount() >= 2) {
                        // Exit if value is not present.
                        if (!requestAsObject.has("value")) {
                            return commandWithFields;
                        }

                        // Prepare value.
                        value = requestAsObject.get("value");
                    }
                }

                commandWithFields = Optional.of(new CommandWithFields(option, keys, value));
                break;
            }
        }

        return commandWithFields;
    }
}
