package org.jboss.narayana.blacktie.jatmibroker.core.transport.hybrid.stomp;

import java.util.Map;

public class Message {
    private String command;

    private Map<String, String> headers;

    private byte[] body;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
