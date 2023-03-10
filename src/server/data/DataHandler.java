package server.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import server.util.JsonTools;
import server.util.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class DataHandler {
    private static final DataHandler instance = new DataHandler();

    private DataHandler() {
        // Initialize data file if not present.
        try {
            if (Files.notExists(dataPath)) {
                Files.write(dataPath, "{}".getBytes());
            }
        } catch (IOException ignored) {
        }
    }

    public static DataHandler getInstance() {
        return instance;
    }

    private Logger logger;

    /**
     * Set logger.
     *
     * @param logger Logger to use.
     */
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public enum Result {
        OK,
        ERROR
    }

//    private final Path dataPath = Paths.get(System.getProperty("user.dir") + "/JSON Database/task/src/server/data/db.json"); // For development.
    private final Path dataPath = Paths.get(System.getProperty("user.dir") + "/src/server/data/db.json"); // For automated tests.
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();

    private final Type dataType = new TypeToken<HashMap<String, String>>() {
    }.getType();

    /**
     * Set element with key(s) to value.
     *
     * @param keys  Key(s) of element to set.
     * @param value Value to set for element.
     * @return DataHandlerResult.
     */
    public DataHandlerResult set(String[] keys, JsonElement value) {
        DataHandlerResult result;

        writeLock.lock();
        try {
            // Read data from file.
            String dataAsString = new String(Files.readAllBytes(dataPath));

            // Deserialize data to JsonObject.
            try {
                JsonElement jsonObject = new Gson().fromJson(dataAsString, JsonElement.class);

                JsonTools.setLogger(logger);
                logger.debug("Before update with " + value);
                JsonTools.logJson((JsonObject) jsonObject);

                // Update element in JsonObject.
                String editResult = JsonTools.updateElement(keys, value, (JsonObject) jsonObject);
                if (editResult != null) {
                    result = new DataHandlerResult(Result.ERROR, new JsonPrimitive(editResult));
                } else {
                    // Write data to file.
                    try {
                        dataAsString = new Gson().toJson(jsonObject);
                        Files.write(dataPath, dataAsString.getBytes());
                        result = new DataHandlerResult(Result.OK, null);

                        logger.debug("After update with " + value);
                        JsonTools.logJson((JsonObject) jsonObject);
                    } catch (IOException e) {
                        result = new DataHandlerResult(Result.ERROR, new JsonPrimitive(e.toString()));
                    }
                }
            } catch (com.google.gson.JsonParseException e) {
                result = new DataHandlerResult(Result.ERROR, new JsonPrimitive(e.toString()));
            }
        } catch (IOException e) {
            result = new DataHandlerResult(Result.ERROR, new JsonPrimitive(e.toString()));
        } finally {
            writeLock.unlock();
        }

        return result;
    }

    /**
     * Get value for element with key(s).
     *
     * @param keys Key(s) of element to get.
     * @return DataHandlerResult with value for element if found.
     */
    public DataHandlerResult get(String[] keys) {
        DataHandlerResult result;

        readLock.lock();
        try {
            // Read data from file.
            String dataAsString = new String(Files.readAllBytes(dataPath));

            // Deserialize data to JsonObject.
            try {
                JsonElement jsonObject = new Gson().fromJson(dataAsString, JsonElement.class);

                JsonTools.setLogger(logger);
                StringBuilder keyList = new StringBuilder();
                Arrays.stream(keys).forEach(keyList::append);
                logger.debug("Get " + keyList);
                JsonTools.logJson((JsonObject) jsonObject);

                // Get element in JsonObject.
                Optional<JsonElement> element = JsonTools.findElement(keys, (JsonObject) jsonObject);
                if (element.isPresent()) {
                    result = new DataHandlerResult(Result.OK, element.get());
                } else {
                    result = new DataHandlerResult(Result.ERROR, new JsonPrimitive("No such key"));
                }
            } catch (com.google.gson.JsonParseException e) {
                result = new DataHandlerResult(Result.ERROR, new JsonPrimitive(e.toString()));
            }
        } catch (IOException e) {
            result = new DataHandlerResult(Result.ERROR, new JsonPrimitive(e.toString()));
        } finally {
            readLock.unlock();
        }

        return result;
    }

    /**
     * Delete element with key(s).
     *
     * @param keys Key(s) of element to delete.
     * @return DataHandlerResult.
     */
    public DataHandlerResult delete(String[] keys) {
        DataHandlerResult result;

        writeLock.lock();
        try {
            // Read data from file.
            String dataAsString = new String(Files.readAllBytes(dataPath));

            // Deserialize data to JsonObject.
            try {
                JsonElement jsonObject = new Gson().fromJson(dataAsString, JsonElement.class);

                JsonTools.setLogger(logger);
                StringBuilder keyList = new StringBuilder();
                Arrays.stream(keys).forEach(keyList::append);
                logger.debug("Before delete of " + keyList);
                JsonTools.logJson((JsonObject) jsonObject);

                // Remove element from JsonObject.
                String removeResult = JsonTools.removeElement(keys, (JsonObject) jsonObject);
                if (removeResult != null) {
                    result = new DataHandlerResult(Result.ERROR, new JsonPrimitive("No such key"));
                } else {
                    // Write data to file.
                    try {
                        dataAsString = new Gson().toJson(jsonObject);
                        Files.write(dataPath, dataAsString.getBytes());
                        result = new DataHandlerResult(Result.OK, null);

                        logger.debug("After delete of " + keyList);
                        JsonTools.logJson((JsonObject) jsonObject);
                    } catch (IOException e) {
                        result = new DataHandlerResult(Result.ERROR, new JsonPrimitive(e.toString()));
                    }
                }
            } catch (com.google.gson.JsonParseException e) {
                result = new DataHandlerResult(Result.ERROR, new JsonPrimitive(e.toString()));
            }
        } catch (IOException e) {
            result = new DataHandlerResult(Result.ERROR, new JsonPrimitive(e.toString()));
        } finally {
            writeLock.unlock();
        }

        return result;
    }
}
