/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.wst11;

import jakarta.xml.ws.wsaddressing.W3CEndpointReference;

/**
 * Not in the 1.1 specification. Supposed to use participant interface.
 */

public interface BusinessActivityTerminator extends com.arjuna.wst.BusinessActivityTerminator
{
    /**
     * @return either the terminator or participant endpoint depending upon what type of terminator strub this is
     */
    public W3CEndpointReference getEndpoint() ;
}