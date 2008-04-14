package com.arjuna.wst11.stub;

import com.arjuna.wst.Volatile2PCParticipant;
import com.arjuna.wst11.stub.ParticipantStub;

import javax.xml.ws.wsaddressing.W3CEndpointReference;

public class Volatile2PCStub extends ParticipantStub implements Volatile2PCParticipant
{
    public Volatile2PCStub(final String id, final W3CEndpointReference twoPCParticipant)
        throws Exception
    {
        super(id, false, twoPCParticipant) ;
    }
}
