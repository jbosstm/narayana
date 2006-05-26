/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.arjuna.webservices;

import java.io.IOException;
import java.io.Reader;

/**
 * Interface for soap version processors.
 * @author kevin
 */
public interface SoapProcessor
{
    /**
     * Process the input stream and generate a response.
     * @param messageContext The message context for the request.
     * @param responseMessageContext The message context for the response.
     * @param action The transport SOAP action.
     * @param reader The input reader.
     * @return The SOAP response.
     * @throws IOException For errors reading the input stream.
     */
    public SoapMessage process(final MessageContext messageContext, final MessageContext responseMessageContext,
        final String action, final Reader reader)
        throws IOException ;
}
