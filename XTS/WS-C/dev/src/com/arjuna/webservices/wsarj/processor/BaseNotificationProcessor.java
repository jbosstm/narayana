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
package com.arjuna.webservices.wsarj.processor;

import com.arjuna.webservices.base.processors.BaseProcessor;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsarj.ArjunaContext;
import com.arjuna.webservices.wsarj.InstanceIdentifier;

/**
 * Utility class handling common response functionality.
 * @author kevin
 */
public abstract class BaseNotificationProcessor extends BaseProcessor
{
    /**
     * Get the callback ids.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     * @return The callback ids.
     */
    protected String[] getIDs(final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        if (arjunaContext != null)
        {
            final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
            if (instanceIdentifier != null)
            {
                return new String[] {instanceIdentifier.getInstanceIdentifier()} ;
            }
        }
        return null ;
    }
}
