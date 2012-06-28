package com.arjuna.wsc11;

import java.util.Map;
import java.util.HashMap;

/**
 * The context factory mapper.
 * @author kevin
 */
public class ContextFactoryMapper
{
    /**
     * The factory singleton.
     */
    private static final ContextFactoryMapper theMapper = new ContextFactoryMapper() ;

    /**
     * The context factory map.
     */
    private final Map contextFactoryMap = new HashMap() ;
    /**
     * Get the context factory mapper singleton.
     * @return The context factory mapper singleton.
     */
    public static ContextFactoryMapper getMapper()
    {
        return theMapper;
    }

    /**
     * Default constructor
     */
    protected ContextFactoryMapper()
    {
    }

    /**
     * Add a context factory for the specified coordination type.
     * @param coordinationTypeURI The coordination type.
     * @param contextFactory The context factory.
     */
    public void addContextFactory(final String coordinationTypeURI, final ContextFactory contextFactory)
    {
        synchronized(contextFactoryMap)
        {
            contextFactoryMap.put(coordinationTypeURI, contextFactory) ;
        }
        contextFactory.install(coordinationTypeURI) ;
    }

    /**
     * Get the context factory for the specified coordination type.
     * @param coordinationTypeURI The coordination type.
     * @return The context factory.
     */
    public ContextFactory getContextFactory(final String coordinationTypeURI)
    {
        synchronized(contextFactoryMap)
        {
            return (ContextFactory)contextFactoryMap.get(coordinationTypeURI) ;
        }
    }

    /**
     * Remove the context factory for the specified coordination type.
     * @param coordinationTypeURI The coordination type.
     */
    public void removeContextFactory(final String coordinationTypeURI)
    {
        final Object localContextFactory ;
        synchronized(contextFactoryMap)
        {
            localContextFactory = contextFactoryMap.remove(coordinationTypeURI) ;
        }
        if (localContextFactory != null)
        {
            ((ContextFactory)localContextFactory).uninstall(coordinationTypeURI) ;
        }
    }
}
