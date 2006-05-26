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
import java.io.Writer;

import com.arjuna.webservices.soap.SoapDetails;

/**
 * Interface representing a SOAP response.
 * @author kevin
 */
public interface SoapMessage
{
    /**
     * Does the response represent a fault?
     * @return true if a fault, false otherwise.
     */
    public boolean isFault() ;
    /**
     * Get the action URI for the response.
     * @return The action URI for the response.
     */
    public String getAction() ;
    
    /**
     * Output the response to the output stream.
     * @param writer The output writer.
     * @throws IOException If errors occur during output.
     */
    public void output(final Writer writer)
        throws IOException ;
    
    /**
     * Get the SOAP details associated with the message.
     * @return The SOAP details.
     */
    public SoapDetails getSoapDetails() ;
    
    /**
     * Get the SOAP service associated with the message.
     * @return The SOAP service or null if not known.
     */
    public SoapService getSoapService() ;
    
    /**
     * Get the message context associated with this message.
     * @return The message context or null if not known.
     */
    public MessageContext getMessageContext() ;
}
