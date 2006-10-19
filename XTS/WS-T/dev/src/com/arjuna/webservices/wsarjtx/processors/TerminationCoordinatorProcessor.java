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
package com.arjuna.webservices.wsarjtx.processors;

import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsarj.ArjunaContext;
import com.arjuna.webservices.wsarjtx.NotificationType;
import com.arjuna.wst.BusinessActivityTerminator;

/**
 * The Terminator Participant processor.
 * @author kevin
 */
public abstract class TerminationCoordinatorProcessor
{
    /**
     * The participant processor.
     */
    private static TerminationCoordinatorProcessor PROCESSOR ;
    
    /**
     * Get the processor.
     * @return The processor.
     */
    public static TerminationCoordinatorProcessor getProcessor()
    {
        return PROCESSOR ;
    }
    
    /**
     * Set the processor.
     * @param processor The processor.
     * @return The previous processor.
     */
    public static TerminationCoordinatorProcessor setProcessor(final TerminationCoordinatorProcessor processor)
    {
        final TerminationCoordinatorProcessor origProcessor = PROCESSOR ;
        PROCESSOR = processor ;
        return origProcessor ;
    }

    /**
     * Activate the participant.
     * @param participant The participant.
     * @param identifier The identifier.
     */
    public abstract void activateParticipant(final BusinessActivityTerminator participant, final String identifier) ;

    /**
     * Deactivate the participant.
     * @param participant The participant.
     */
    public abstract void deactivateParticipant(final BusinessActivityTerminator participant) ;
    
    /**
     * Cancel.
     * @param cancel The cancel notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void cancel(final NotificationType cancel, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext) ;
    
    /**
     * Close.
     * @param close The close notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void close(final NotificationType close, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext) ;
    
    /**
     * Complete.
     * @param complete The complete notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void complete(final NotificationType complete, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext) ;
}
