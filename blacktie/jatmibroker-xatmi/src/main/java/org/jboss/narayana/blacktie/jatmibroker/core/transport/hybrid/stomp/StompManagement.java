package org.jboss.narayana.blacktie.jatmibroker.core.transport.hybrid.stomp;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;

/**
 * This class could be extended to support connection reconnection.
 */
public class StompManagement {
    private static final Logger log = LogManager.getLogger(StompManagement.class);

    private static List<Socket> disconnectedConnections = new ArrayList<Socket>();

    private static final byte[] COLON = {':'};
    private static final byte[] EOL = {'\n'};
    private static final byte[] EOM = {'\0', '\n', '\n'};

    public static void close(Socket socket, OutputStream outputStream, InputStream inputStream) throws IOException {
        log.debug("close");
        Message message = new Message();
        message.setCommand("DISCONNECT");
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("receipt", "disconnect");
        message.setHeaders(headers);
        send(message, outputStream);
        log.debug("Sent disconnect");
        synchronized (socket) {
            if (!disconnectedConnections.remove(socket)) {
                Message received = receive(socket, inputStream);
                if (received != null && received.getCommand().equals("ERROR")) {
                    log.error("Did not receive the receipt for the disconnect:" + new String(received.getBody()));
                }
            }
            disconnectedConnections.remove(socket);
        }
    }

    public static Socket connect(String host, int port, String username, String password) throws IOException,
            ConnectionException {
        Socket socket = new Socket(host, port);
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("login", username);
        headers.put("passcode", password);
        Message message = new Message();
        message.setCommand("CONNECT");
        message.setHeaders(headers);
        send(message, outputStream);
        Message received = receive(socket, inputStream);
        if (received.getCommand().equals("ERROR")) {
            throw new ConnectionException(Connection.TPESYSTEM, new String(received.getBody()));
        }

        log.debug("Created socket: " + socket + " input: " + inputStream + "output: " + outputStream);
        return socket;
    }

    public static void send(Message message, OutputStream outputStream) throws IOException {
        log.trace("Writing on: " + outputStream);
        synchronized (outputStream) {
            outputStream.write(message.getCommand().getBytes());
            outputStream.write(EOL);

            for (Map.Entry<String, String> header : message.getHeaders().entrySet()) {
                outputStream.write(header.getKey().getBytes());
                outputStream.write(COLON);
                outputStream.write(header.getValue().getBytes());
                outputStream.write(EOL);
            }
            outputStream.write(EOL);
            
            if(message.getBody() != null) {
                outputStream.write(message.getBody());
            }
            outputStream.write(EOM);
        }

        log.trace("Wrote on: " + outputStream);
    }

    public static Message receive(Socket socket, InputStream inputStream) throws IOException {
        synchronized (socket) {
            log.trace("Reading from: " + inputStream);
            Message message = new Message();
            message.setCommand(readLine(inputStream));
            log.trace(message.getCommand());
            Map<String, String> headers = new HashMap<String, String>();
            String header;
            while ((header = readLine(inputStream)).length() > 0) {
                int sep = header.indexOf(':');
                if (sep > 0) {
                    String key = header.substring(0, sep);
                    String value = header.substring(sep + 1, header.length());
                    headers.put(key.trim(), value.trim());
                    log.trace("Header: " + key + ":" + value);
                }
            }
            message.setHeaders(headers);

            if (message.getCommand() != null) {
                if (message.getCommand().equals("RECEIPT")) {
                    if (message.getHeaders().get("receipt-id") != null
                            && message.getHeaders().get("receipt-id").equals("disconnect")) {
                        log.debug("Read disconnect receipt from: " + inputStream);
                        disconnectedConnections.add(socket);
                        message = null;
                    } else {
                        log.trace("Read from: " + inputStream + " command was: " + message.getCommand());
                    }
                    readLine(inputStream);
                    readLine(inputStream);
                } else if (!message.getCommand().equals("ERROR")) {
                    String contentLength = headers.get("content-length");
                    if (contentLength != null) {
                        byte[] body = new byte[Integer.valueOf(contentLength)];
                        int offset = 0;
                        while (offset != body.length) {
                            offset = inputStream.read(body, offset, body.length - offset);
                        }
                        message.setBody(body);
                        log.trace("Read error: " + body);
                    }
                    readLine(inputStream);
                    readLine(inputStream);
                    log.trace("Read from: " + inputStream + " command was: " + message.getCommand());
                } else {
                    message.setBody(headers.get("message").getBytes());
                    // Drain off the error message
                    String read = null;
                    do {
                        read = readLine(inputStream);
                        if (read != null)
                            log.debug(read);
                    } while (read != null);
                    readLine(inputStream);
                    log.trace("Read from: " + inputStream + " command was: " + message.getCommand());
                }
            } else {
                log.trace("Read from: " + inputStream + " null");
                message = null;
            }

            return message;
        }
    }

    private static String readLine(InputStream inputStream) throws IOException {
        String toReturn = null;
        char[] read = new char[0];
        char c = (char) inputStream.read();
        while (c != '\n' && c != '\000' && c != -1) {
            char[] tmp = new char[read.length + 1];
            System.arraycopy(read, 0, tmp, 0, read.length);
            tmp[read.length] = c;
            read = tmp;
            c = (char) inputStream.read();
        }
        if (c == -1) {
            throw new EOFException("Read the end of the stream");
        }
        if (c == '\000') {
            log.trace("returning null");
        } else {
            toReturn = new String(read);
            log.trace("returning: " + toReturn);
        }
        return toReturn;
    }
}
