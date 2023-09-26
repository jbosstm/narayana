/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.recovery;

import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import org.jboss.logging.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

public class RecoveryDriver {
    public static final String SCAN = "SCAN";
    public static final String VERBOSE_SCAN = "VERBOSE_" + SCAN;
    public static final String ASYNC_SCAN = "ASYNC_" + SCAN;
    public static final String VERBOSE_ASYNC_SCAN = "VERBOSE_ASYNC_" + SCAN;

    public static final String PING = "PING";
    public static final String PONG = "PONG";

    // allow time for one complete scan (which is dominated by the backoff wait) plus a fudge factor for actual work + comms delay
    public static final int DEFAULT_SYNC_TIMEOUT = 1000 + (recoveryPropertyManager.getRecoveryEnvironmentBean().getRecoveryBackoffPeriod() * 1000); // in milliseconds
    public static final int DEFAULT_SYNC_RETRY = 5;

    public static final int DEFAULT_SO_TIMEOUT = 20000;

    public RecoveryDriver(int port) {
        this(port, null, DEFAULT_SO_TIMEOUT);
    }

    public RecoveryDriver(int port, String hostName) {
        this(port, hostName, DEFAULT_SO_TIMEOUT);
    }

    public RecoveryDriver(int port, String hostName, int timeout) {
        _port = port;
        _hostName = hostName;
        _timeout = timeout;
    }

    public static boolean isScan(String request) {
        return request != null && request.endsWith(SCAN);
    }

    public static boolean isAsyncScan(String request) {
        return request != null && (request.equals(ASYNC_SCAN) || request.equals((VERBOSE_ASYNC_SCAN)));
    }

    public static boolean isVerboseScan(String request) {
        return request != null && (request.equals(VERBOSE_SCAN) || request.equals((VERBOSE_ASYNC_SCAN)));
    }

    public final boolean synchronousScan() throws java.net.UnknownHostException, java.net.SocketException, java.io.IOException {
        return synchronousScan(DEFAULT_SYNC_TIMEOUT, DEFAULT_SYNC_RETRY);
    }

    public final boolean synchronousVerboseScan() throws java.io.IOException {
        return synchronousVerboseScan(DEFAULT_SYNC_TIMEOUT, DEFAULT_SYNC_RETRY);
    }

    public final boolean synchronousScan(int timeout, int retry) throws java.net.UnknownHostException, java.net.SocketException, java.io.IOException {
        return scan(SCAN, timeout, retry);
    }

    public final boolean synchronousVerboseScan(int timeout, int retry) throws java.io.IOException {
        return scan(VERBOSE_SCAN, timeout, retry);
    }

    public final boolean asynchronousScan() throws java.net.UnknownHostException, java.net.SocketException, java.io.IOException {
        /*
         * For async the timeout is the socket timeout and number of attempts on call is 1.
         */
        return scan(ASYNC_SCAN, _timeout, 1);
    }

    public final boolean asynchronousVerboseScan () throws java.io.IOException
    {
        /*
         * For async the timeout is the socket timeout and number of attempts on call is 1.
         */
        return scan(VERBOSE_ASYNC_SCAN, _timeout, 1);
    }

    /*
     * Be sure to ignore timeout/retry for async.
     */
    private final boolean scan(String scanType, int timeout, int retry) throws java.net.UnknownHostException, java.net.SocketException, java.io.IOException {
        if (_hostName == null)
            _hostName = InetAddress.getLocalHost().getHostName();

        boolean success = false;
        Socket connectorSocket = null;

        for (int i = 0; i < retry && !success; i++) {
            connectorSocket = new Socket(_hostName, _port);

            connectorSocket.setSoTimeout(timeout);

            try {
                // streams to and from the RecoveryManager

                BufferedReader fromServer = new BufferedReader(new InputStreamReader(connectorSocket.getInputStream(), StandardCharsets.UTF_8));

                PrintWriter toServer = new PrintWriter(new OutputStreamWriter(connectorSocket.getOutputStream(), StandardCharsets.UTF_8));

                toServer.println(scanType);

                toServer.flush();

                String response = fromServer.readLine();

                if (response.equals("DONE")) {
                    success = true;
                }
            } catch (final SocketTimeoutException ex) {
                // ignore as it will be retried
            } finally {
                if (connectorSocket != null)
                    connectorSocket.close();
            }
        }

        return success;
    }

    private String _hostName = null;
    private int _port = 0;
    private int _timeout = 20000;
}