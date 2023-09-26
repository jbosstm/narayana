/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.wsc.tests;

import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.wsc.AlreadyRegisteredException;
import com.arjuna.wsc.InvalidProtocolException;
import com.arjuna.wsc.InvalidStateException;
import com.arjuna.wsc.NoActivityException;
import com.arjuna.wsc11.Registrar;

import jakarta.xml.ws.wsaddressing.W3CEndpointReference;

public class TestRegistrar implements Registrar
{
    public void install(String protocolIdentifier)
    {
    }

    public W3CEndpointReference register(W3CEndpointReference participantProtocolService, String protocolIdentifier, InstanceIdentifier instanceIdentifier, boolean isSecure)
        throws AlreadyRegisteredException, InvalidProtocolException, InvalidStateException, NoActivityException
    {
        if (protocolIdentifier.equals(TestUtil.ALREADY_REGISTERED_PROTOCOL_IDENTIFIER))
            throw new AlreadyRegisteredException();
        else if (protocolIdentifier.equals(TestUtil.INVALID_PROTOCOL_PROTOCOL_IDENTIFIER))
            throw new InvalidProtocolException();
        else if (protocolIdentifier.equals(TestUtil.INVALID_STATE_PROTOCOL_IDENTIFIER))
            throw new InvalidStateException();
        else if (protocolIdentifier.equals(TestUtil.NO_ACTIVITY_PROTOCOL_IDENTIFIER))
            throw new NoActivityException();

        return TestUtil11.getProtocolCoordinatorEndpoint(instanceIdentifier.getInstanceIdentifier());
    }

    public void uninstall(String protocolIdentifier)
    {
    }
}