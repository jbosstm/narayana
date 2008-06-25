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
package com.arjuna.webservices.wsaddr2005.processor;

import com.arjuna.webservices.base.processors.BaseProcessor;
import com.arjuna.webservices.wsaddr2005.AddressingContext;
import com.arjuna.webservices.wsaddr2005.RelatesToType;;

/**
 * Utility class handling common response functionality.
 * @author kevin
 */
public abstract class BaseWSAddr2005ResponseProcessor extends BaseProcessor
{
    /**
     * Get the callback ids.
     * @param addressingContext The addressing context.
     * @return The callback ids.
     */
    protected String[] getIDs(final AddressingContext addressingContext)
    {
        final RelatesToType[] relationships = addressingContext.getRelatesTo() ;
        if (relationships != null)
        {
            final int numRelationships = relationships.length ;
            final String[] ids = new String[numRelationships] ;
            for(int count = 0 ; count < numRelationships ; count++)
            {
                ids[count] = relationships[count].getValue() ;
            }
            return ids ;
        }
        return null ;
    }
}
