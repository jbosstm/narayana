/*
 * SPDX short identifier: Apache-2.0
 */
package com.arjuna.wst11;

import jakarta.xml.ws.wsaddressing.W3CEndpointReference;

/**
 */

public interface CompletionCoordinatorParticipant extends com.arjuna.wst.CompletionCoordinatorParticipant
{
    public W3CEndpointReference getParticipant();
}
