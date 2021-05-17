package org.jboss.jbossts.star.client;

import javax.ws.rs.WebApplicationException;
import java.net.URL;

public class GenericSRAException extends WebApplicationException {
    private URL sraId;
    private int statusCode;

    public int getStatusCode() {
        return statusCode;
    }

    public URL getLraId() {
        return sraId;
    }

    public GenericSRAException(URL sraId, int statusCode, String message, Throwable cause) {
        super(String.format("%s: %s", sraId, message), cause);

        this.sraId = sraId;
        this.statusCode = statusCode;
    }
}
