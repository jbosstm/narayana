package com.arjuna.wsc11;

import com.arjuna.wsc11.Registrar;

import java.util.Map;
import java.util.HashMap;

/**
 * The registrar mapper.
 * @author kevin
 */
public class RegistrarMapper
{
    /**
     * The factory singleton.
     */
    private static final RegistrarMapper FACTORY = new RegistrarMapper() ;

    /**
     * The registrar map.
     */
    private final Map registrarMap = new HashMap() ;

    /**
     * Get the registrar mapper singleton.
     * @return The registrar mapper singleton.
     */
    public static RegistrarMapper getFactory()
    {
        return FACTORY;
    }

    /**
     * Default constructor
     */
    protected RegistrarMapper()
    {
    }

    /**
     * Add a registrar for the specified protocol identifier.
     * @param protocolIdentifier The protocol identifier.
     * @param registrar The registrar.
     */
    public void addRegistrar(final String protocolIdentifier, final Registrar registrar)
    {
        synchronized(registrarMap)
        {
            registrarMap.put(protocolIdentifier, registrar) ;
        }
        registrar.install(protocolIdentifier) ;
    }

    /**
     * Get the registrar for the specified protocol identifier.
     * @param protocolIdentifier The protocol identifier.
     * @return The registrar.
     */
    public Registrar getRegistrar(final String protocolIdentifier)
    {
        final Object localRegistrar ;
        synchronized(registrarMap)
        {
            localRegistrar = registrarMap.get(protocolIdentifier) ;
        }
        return (Registrar)localRegistrar ;
    }

    /**
     * Remove the registrar for the specified protocol identifier.
     * @param protocolIdentifier The protocol identifier.
     */
    public void removeRegistrar(final String protocolIdentifier)
    {
        final Object localRegistrar ;
        synchronized(registrarMap)
        {
            localRegistrar = registrarMap.remove(protocolIdentifier) ;
        }
        if (localRegistrar != null)
        {
            ((Registrar)localRegistrar).uninstall(protocolIdentifier) ;
        }
    }
}
