/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2018, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
