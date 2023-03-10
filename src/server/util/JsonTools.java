package server.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.Optional;

public final class JsonTools {
    private static Logger logger;

    /**
     * Set logger.
     *
     * @param logger Logger to use.
     */
    public static void setLogger(Logger logger) {
        JsonTools.logger = logger;
    }

    /**
     * Log JsonObject in pretty print style.
     *
     * @param jsonObject JsonObject to log.
     */
    public static void logJson(JsonObject jsonObject) {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        logger.debug(gson.toJson(jsonObject));
    }

    /**
     * Get element with key(s).
     *
     * @param keys       Key(s) of element to get.
     * @param jsonObject JsonObject to search.
     * @return Optional element if element was found. Otherwise, returns Optional empty.
     */
    public static Optional<JsonElement> findElement(String[] keys, JsonObject jsonObject) {
        Optional<JsonElement> result = Optional.empty();

        // Loop through keys.
        int lastKeysIndex = keys.length - 1;
        JsonObject currentJsonObject = jsonObject;
        for (int i = 0; i <= lastKeysIndex; i++) {
            // Exit without element if element not found.
            JsonElement element = currentJsonObject.get(keys[i]);
            if (element == null) {
                break;
            }

            // Exit with element if on last key.
            if (i == lastKeysIndex) {
                result = Optional.of(element);
            }

            // Exit without element if not on JsonObject.
            if (!element.isJsonObject()) {
                break;
            }

            // Move on to next key compare.
            currentJsonObject = (JsonObject) element;
        }

        return result;
    }

    /**
     * Update element with key(s) to value.
     *
     * @param keys       Key(s) of element to update.
     * @param value      Value to set for element.
     * @param jsonObject JsonObject to update.
     * @return null if update was successful. Otherwise, returns a string describing problem.
     */
    public static String updateElement(String[] keys, JsonElement value, JsonObject jsonObject) {
        // Get element key to update.
        String elementKey = keys[keys.length - 1];

        // Get location to work upon.
        JsonObject elementLocation;
        if (keys.length == 1) {
            elementLocation = jsonObject;
        } else {
            // Exit if element location not found.
            Optional<JsonElement> elementLocationOptional = findElement(Arrays.copyOfRange(keys, 0, keys.length - 1), jsonObject);
            if (elementLocationOptional.isEmpty()) {
                return "Element location not found.";
            }

            // Exit if element location is not a JsonObject.
            if (!elementLocationOptional.get().isJsonObject()) {
                return "Element location is not a JsonObject.";
            }

            elementLocation = (JsonObject) elementLocationOptional.get();
        }

        // Update element at key with new value.
        elementLocation.remove(elementKey);
        elementLocation.add(elementKey, value);

        return null;
    }

    /**
     * Remove element with key(s).
     *
     * @param keys       Key(s) of element to remove.
     * @param jsonObject JsonObject to update.
     * @return null if remove was successful. Otherwise, returns a string describing problem.
     */
    public static String removeElement(String[] keys, JsonObject jsonObject) {
        // Get element key to remove.
        String elementKey = keys[keys.length - 1];

        // Get location to work upon.
        JsonObject elementLocation;
        if (keys.length == 1) {
            elementLocation = jsonObject;
        } else {
            // Exit if element location not found.
            Optional<JsonElement> elementLocationOptional = findElement(Arrays.copyOfRange(keys, 0, keys.length - 1), jsonObject);
            if (elementLocationOptional.isEmpty()) {
                return "Element location not found.";
            }

            // Exit if element location is not a JsonObject.
            if (!elementLocationOptional.get().isJsonObject()) {
                return "Element location is not a JsonObject.";
            }

            elementLocation = (JsonObject) elementLocationOptional.get();
        }

        // Exit if key not found.
        if (!elementLocation.has(elementKey)) {
            return "Key not found.";
        }

        // Remove element with key.
        elementLocation.remove(elementKey);

        return null;
    }
}
