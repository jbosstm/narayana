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
package com.arjuna.webservices.soap;

/**
 * Common constants for SOAP.
 * @author kevin
 */
public interface SoapConstants
{
    /**
     * The local name for a SOAP envelope.
     */
    public String SOAP_ENVELOPE_NAME = "Envelope" ;
    /**
     * The local name for a SOAP header.
     */
    public String SOAP_HEADER_NAME = "Header" ;
    /**
     * The local name for a SOAP body.
     */
    public String SOAP_BODY_NAME = "Body" ;
    /**
     * The local name for a SOAP mustUnderstand attribute.
     */
    public String SOAP_MUST_UNDERSTAND_NAME = "mustUnderstand" ;
    /**
     * The prefix for SOAP elements.
     */
    public String SOAP_PREFIX = "soap" ;
}
