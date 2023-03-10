package client;

import com.beust.jcommander.Parameter;

import java.util.ArrayList;
import java.util.List;

public final class ArgsParsed {
    @Parameter(names = "-t", description = "Request type", validateWith = RequestTypeValidator.class)
    private String requestType;

    @Parameter(names = "-k", description = "Key")
    private String key;

    @Parameter(names = "-v", description = "Value", variableArity = true)
    private List<String> values = new ArrayList<>();

    @Parameter(names = "-in", description = "Request input file")
    private String requestInputFile;

    public String requestType() {
        return requestType;
    }

    public String key() {
        return key;
    }

    public List<String> values() {
        return values;
    }

    public String requestInputFile() {
        return requestInputFile;
    }
}
