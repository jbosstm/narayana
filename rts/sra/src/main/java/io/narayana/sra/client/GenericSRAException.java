package io.narayana.sra.client;

import javax.ws.rs.WebApplicationException;
import java.net.URL;

public class GenericSRAException extends WebApplicationException {
    private URL lraId;
    private int statusCode;

    public int getStatusCode() {
        return statusCode;
    }

    public URL getLraId() {
        return lraId;
    }

    public GenericSRAException(URL lraId, int statusCode, String message, Throwable cause) {
        super(String.format("%s: %s", lraId, message), cause);

        this.lraId = lraId;
        this.statusCode = statusCode;
    }
}
