/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.wsc11;


import org.xmlsoap.schemas.soap.envelope.Fault;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.RegisterResponseType;

/**
 *
 * @author rmartinc
 */
public class FaultOrResponse {

    private RegisterResponseType response;
    private Fault fault;
    
    public FaultOrResponse() {
        this.response = null;
        this.fault = null;
    }

    public void setResponse(RegisterResponseType response) {
        this.response = response;
        this.fault = null;
    }

    public void setFault(Fault fault) {
        this.fault = fault;
        this.response = null;
    }

    public RegisterResponseType getResponse() {
        return response;
    }

    public Fault getFault() {
        return fault;
    }

    public boolean isEmpty() {
        return fault == null && response == null;
    }

    public boolean isResponse() {
        return response != null;
    }

    public boolean isFault() {
        return fault != null;
    }
}