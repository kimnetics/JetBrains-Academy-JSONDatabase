package client;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

public final class RequestTypeValidator implements IParameterValidator {
    enum RequestType {
        set,
        get,
        delete,
        exit
    }

    @Override
    public void validate(String name, String value) throws ParameterException {
        boolean requestTypeValid = false;
        for (RequestType requestType : RequestType.values()) {
            if (requestType.name().equalsIgnoreCase(value)) {
                requestTypeValid = true;
                break;
            }
        }
        if (!requestTypeValid) {
            throw new ParameterException(String.format("Parameter %s must have a valid request type.", name));
        }
    }
}
