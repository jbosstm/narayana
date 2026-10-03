/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (c) 2002, 2003, Arjuna Technologies Limited.
 *
 * ContextFactory.java
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
