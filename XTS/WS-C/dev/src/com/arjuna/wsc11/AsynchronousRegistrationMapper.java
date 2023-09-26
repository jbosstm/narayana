/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.wsc11;

import com.arjuna.webservices.logging.WSCLogger;
import java.util.HashMap;
import java.util.Map;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.RegisterResponseType;
import org.xmlsoap.schemas.soap.envelope.Fault;

/**
 * Class that manages the asynchronous calls when using ReplyTo.
 * 
 * @author rmartinc
 */
public class AsynchronousRegistrationMapper {
    
    // the singleton
    private static final AsynchronousRegistrationMapper mapper = new AsynchronousRegistrationMapper();
    
    private Map<String, FaultOrResponse> responses = new HashMap<>();
    
    public static AsynchronousRegistrationMapper getInstance() {
        return mapper;
    }
    
    /**
     * Adds a new messageID that is going to be sent by the client waiting for response.
     * @param messageId The messageID hat is going to be sent
     */
    public void addClientMessage(String messageId) {
        WSCLogger.logger.tracev("AsynchronousRegistrationMapper addClientMessage {0}", messageId);
        this.responses.put(messageId, new FaultOrResponse());
    }
    
    public FaultOrResponse waitForResponse(String messageId, long millis) {
        FaultOrResponse res = responses.get(messageId);
        if (res == null) {
            throw new IllegalStateException("The messageId " + messageId + " is not waiting for response");
        }
        synchronized (res) {
            if (res.isEmpty()) {
                // wait for the response to arrive
                try {
                    WSCLogger.logger.tracev("AsynchronousRegistrationMapper waitForResponse {0} {1}", messageId, millis);
                    res.wait(millis);
                } catch (InterruptedException e) {
                    WSCLogger.logger.warnv("AsynchronousRegistrationMapper waitForResponse: being interrupted and this should not happen!");
                }
            }
            // remove the response cos it's filled
            responses.remove(messageId);
        }
        WSCLogger.logger.tracev("AsynchronousRegistrationMapper addClientMessage returning isResponse={0} isFault={1}",
                res.isResponse(), res.isFault());
        return res;
    }
    
    public void assignResponse(String messageId, RegisterResponseType response) {
        FaultOrResponse res = responses.get(messageId);
        if (res == null) {
            throw new IllegalStateException("The messageId " + messageId + " is not waiting for response");
        }
        synchronized (res) {
            res.setResponse(response);
            WSCLogger.logger.tracev("AsynchronousRegistrationMapper assignResponse {0}", messageId);
            res.notifyAll();
        }
    }
    
    public void assignFault(String messageId, Fault fault) {
        FaultOrResponse res = responses.get(messageId);
        if (res == null) {
            throw new IllegalStateException("The messageId " + messageId + " is not waiting for response");
        }
        synchronized (res) {
            res.setFault(fault);
            WSCLogger.logger.tracev("AsynchronousRegistrationMapper assignFault {0}", messageId);
            res.notifyAll();
        }
    }
    
    public int size() {
        return responses.size();
    }
}