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
package com.arjuna.webservices.adapters;

import javax.xml.stream.Location;

/**
 * An empty location.
 * @author kevin
 */
public class EmptyLocation implements Location
{
    /**
     * Get the current line number.
     * @return the current line number.
     */
    public int getLineNumber()
    {
        return -1 ;
    }

    /**
     * Get the current column number.
     * @return the current column number.
     */
    public int getColumnNumber()
    {
        return -1 ;
    }

    /**
     * Get the current character offset.
     * @return the current character offset.
     */
    public int getCharacterOffset()
    {
        return -1 ;
    }

    /**
     * Get the public ID.
     * @return the public ID.
     */
    public String getPublicId()
    {
        return null ;
    }

    /**
     * Get the system ID.
     * @return the system ID.
     */
    public String getSystemId()
    {
        return null ;
    }
}
