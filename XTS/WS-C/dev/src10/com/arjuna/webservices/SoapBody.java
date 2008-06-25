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
package com.arjuna.webservices;

import com.arjuna.webservices.stax.NamedElement;

/**
 * Class representing a SOAP body
 * @author kevin
 */
public class SoapBody
{
    /**
     * The SOAP body contents.
     */
    private NamedElement contents ;
    /**
     * The associated action.
     */
    private String action ;
    
    /**
     * Default constructor. 
     */
    public SoapBody()
    {
    }
    
    /**
     * Construct a SOAP body with the specified contents and action.
     * @param contents The SOAP body contents.
     */
    public SoapBody(final NamedElement contents)
    {
        this(contents, null) ;
    }
    
    /**
     * Construct a SOAP body with the specified contents and action.
     * @param contents The SOAP body contents.
     * @param action The SOAP body action.
     */
    public SoapBody(final NamedElement contents, final String action)
    {
        this.contents = contents ;
        this.action = action ;
    }
    
    /**
     * Get the SOAP body contents.
     * @return The SOAP body contents.
     */
    public NamedElement getContents()
    {
        return contents ;
    }
    
    /**
     * Set the SOAP body contents.
     * @param contents The SOAP body contents.
     */
    public void setContents(final NamedElement contents)
    {
        this.contents = contents ;
    }
    
    /**
     * Get the body action.
     * @return The body action.
     */
    public String getAction()
    {
        return action ;
    }
    
    /**
     * Set the body action.
     * @param action The body action.
     */
    public void setAction(final String action)
    {
        this.action = action ;
    }
}
