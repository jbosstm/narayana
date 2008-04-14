/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
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
package com.arjuna.webservices.wsat.processors;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsarj.ArjunaContext;
import com.arjuna.webservices.wsat.CoordinatorInboundEvents;
import com.arjuna.webservices.wsat.NotificationType;

/**
 * The Coordinator processor.
 * @author kevin
 */
public abstract class CoordinatorProcessor
{
    /**
     * The coordinator processor.
     */
    private static CoordinatorProcessor PROCESSOR ;
    
    /**
     * Get the processor singleton.
     * @return The singleton.
     */
    public static synchronized CoordinatorProcessor getProcessor()
    {
        return PROCESSOR ;
    }
    
    /**
     * Set the processor singleton.
     * @param processor The processor.
     * @return The previous singleton.
     */
    public static synchronized CoordinatorProcessor setProcessor(final CoordinatorProcessor processor)
    {
        final CoordinatorProcessor origProcessor = PROCESSOR ;
        PROCESSOR = processor ;
        return origProcessor ;
    }
    
    /**
     * Activate the coordinator.
     * @param coordinator The coordinator.
     * @param identifier The identifier.
     */
    public abstract void activateCoordinator(final CoordinatorInboundEvents coordinator, final String identifier) ;

    /**
     * Deactivate the coordinator.
     * @param coordinator The coordinator.
     */
    public abstract void deactivateCoordinator(final CoordinatorInboundEvents coordinator) ;
    
    /**
     * Aborted.
     * @param aborted The aborted notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void aborted(final NotificationType aborted, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext) ;
    
    /**
     * Committed.
     * @param committed The committed notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void committed(final NotificationType committed, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext) ;
    
    /**
     * Prepared.
     * @param prepared The prepared notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void prepared(final NotificationType prepared, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext) ;
    
    /**
     * Read only.
     * @param readOnly The read only notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void readOnly(final NotificationType readOnly, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext) ;
    
    /**
     * Replay.
     * @param replay The replay notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void replay(final NotificationType replay, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext) ;
    
    /**
     * SOAP fault.
     * @param soapFault The SOAP fault.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void soapFault(final SoapFault soapFault, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext) ;
}
