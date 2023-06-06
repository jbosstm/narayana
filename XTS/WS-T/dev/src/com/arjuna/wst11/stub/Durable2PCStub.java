/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */
package com.arjuna.wst11.stub;

import com.arjuna.wst.Durable2PCParticipant;

import jakarta.xml.ws.wsaddressing.W3CEndpointReference;

public class Durable2PCStub extends ParticipantStub implements Durable2PCParticipant
{
    // default ctor for crash recovery
    public Durable2PCStub() throws Exception {
        super(null, true, null);
    }

    public Durable2PCStub(final String id, final W3CEndpointReference twoPCParticipant)
        throws Exception
    {
        super(id, true, twoPCParticipant) ;
    }
}
