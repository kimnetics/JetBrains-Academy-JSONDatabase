package server.data;

import com.google.gson.JsonElement;

public record DataHandlerResult(
        DataHandler.Result result,
        JsonElement value
) {
}
