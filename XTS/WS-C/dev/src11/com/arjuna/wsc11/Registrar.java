package com.arjuna.wsc11;

import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.wsc.AlreadyRegisteredException;
import com.arjuna.wsc.InvalidProtocolException;
import com.arjuna.wsc.InvalidStateException;
import com.arjuna.wsc.NoActivityException;

import javax.xml.ws.wsaddressing.W3CEndpointReference;

public interface Registrar
{
    /**
     * Called when a registrar is added to a register mapper. This method will be called multiple times if the
     * registrar is added to multiple register mappers or to the same register mapper with different protocol
     * identifiers.
     *
     * @param protocolIdentifier the protocol identifier
     */
    public void install(final String protocolIdentifier);

    /**
     * Registers the interest of participant in a particular protocol.
     *
     * @param participantProtocolService the port reference of the participant protocol service
     * @param protocolIdentifier the protocol identifier
     * @param instanceIdentifier the instance identifier, this may be null
     *
     * @return the port reference of the coordinator protocol service
     *
     * @throws com.arjuna.wsc.AlreadyRegisteredException if the participant is already registered for this coordination protocol under
     *         this activity identifier
     * @throws com.arjuna.wsc.InvalidProtocolException if the coordination protocol is not supported
     * @throws com.arjuna.wsc.InvalidStateException if the state of the coordinator no longer allows registration for this
     *         coordination protocol
     * @throws com.arjuna.wsc.NoActivityException if the actvity does not exist
     */
    public W3CEndpointReference register(final W3CEndpointReference participantProtocolService,
        final String protocolIdentifier, final InstanceIdentifier instanceIdentifier)
        throws AlreadyRegisteredException, InvalidProtocolException, InvalidStateException, NoActivityException;

    /**
     * Called when a registrar is removed from a register mapper. This method will be called multiple times if the
     * registrar is removed from multiple register mappers or from the same register mapper with different protocol
     * identifiers.
     *
     * @param protocolIdentifier the protocol identifier
     */
    public void uninstall(final String protocolIdentifier);
}
