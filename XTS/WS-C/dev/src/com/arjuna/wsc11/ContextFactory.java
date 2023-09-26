/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.wsc11;

import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContext;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;
import com.arjuna.wsc.InvalidCreateParametersException;

public interface ContextFactory
{
    /**
     * Called when a context factory is added to a context factory mapper. This method will be called multiple times
     * if the context factory is added to multiple context factory mappers or to the same context mapper with different
     * protocol identifiers.
     *
     * @param coordinationTypeURI the coordination type uri
     */
	
    public void install(final String coordinationTypeURI);

    /**
     * Creates a coordination context.
     *
     * @param coordinationTypeURI the coordination type uri
     * @param expires the expire date/time for the returned context, can be null
     * @param currentContext the current coordination context, can be null
     *
     * @return the created coordination context
     *
     * @throws com.arjuna.wsc.InvalidCreateParametersException if a parameter passed is invalid
     *         this activity identifier
     */
	
    public CoordinationContext create(final String coordinationTypeURI,
            final Long expires, final CoordinationContextType currentContext, final boolean isSecure)
        throws InvalidCreateParametersException;

    /**
     * Called when a context factory is removed from a context factory mapper. This method will be called multiple
     * times if the context factory is removed from multiple context factory mappers or from the same context factory
     * mapper with different coordination type uris.
     *
     * @param coordinationTypeURI the coordination type uri
     */
	
    public void uninstall(final String coordinationTypeURI);
}