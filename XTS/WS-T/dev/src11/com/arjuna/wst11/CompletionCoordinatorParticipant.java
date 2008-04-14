package com.arjuna.wst11;

import javax.xml.ws.wsaddressing.W3CEndpointReference;

/**
 */

public interface CompletionCoordinatorParticipant extends com.arjuna.wst.CompletionCoordinatorParticipant
{
    public W3CEndpointReference getParticipant();
}
