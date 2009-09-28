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
package com.arjuna.webservices11.wsba.processors;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsba.CoordinatorCompletionCoordinatorInboundEvents;
import org.jboss.wsf.common.addressing.MAP;
import org.oasis_open.docs.ws_tx.wsba._2006._06.ExceptionType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.NotificationType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.StatusType;

/**
 * The Coordinator Completion Coordinator processor.
 * @author kevin
 */
public abstract class CoordinatorCompletionCoordinatorProcessor
{
    /**
     * The coordinator processor.
     */
    private static CoordinatorCompletionCoordinatorProcessor PROCESSOR ;

    /**
     * Get the processor.
     * @return The singleton.
     */
    public static synchronized CoordinatorCompletionCoordinatorProcessor getProcessor()
    {
        return PROCESSOR ;
    }

    /**
     * Set the processor.
     * @param processor The processor.
     * @return The previous processor.
     */
    public static synchronized CoordinatorCompletionCoordinatorProcessor setProcessor(final CoordinatorCompletionCoordinatorProcessor processor)
    {
        final CoordinatorCompletionCoordinatorProcessor origProcessor = PROCESSOR ;
        PROCESSOR = processor ;
        return origProcessor ;
    }

    /**
     * Activate the coordinator.
     * @param coordinator The coordinator.
     * @param identifier The identifier.
     */
    public abstract void activateCoordinator(final CoordinatorCompletionCoordinatorInboundEvents coordinator, final String identifier) ;

    /**
     * Deactivate the coordinator.
     * @param coordinator The coordinator.
     */
    public abstract void deactivateCoordinator(final CoordinatorCompletionCoordinatorInboundEvents coordinator) ;

    /**
     * Locate a coordinator by name.
     * @param identifier The name of the coordinator.
     */
    public abstract CoordinatorCompletionCoordinatorInboundEvents getCoordinator(final String  identifier) ;

    /**
     * Cancelled.
     * @param cancelled The cancelled notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void cancelled(final NotificationType cancelled, final MAP map, final ArjunaContext arjunaContext) ;

    /**
     * Closed.
     * @param closed The closed notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void closed(final NotificationType closed, final MAP map, final ArjunaContext arjunaContext) ;

    /**
     * Compensated.
     * @param compensated The compensated notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void compensated(final NotificationType compensated, final MAP map, final ArjunaContext arjunaContext) ;

    /**
     * Fail.
     * @param fail The fail exception.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void fail(final ExceptionType fail, final MAP map, final ArjunaContext arjunaContext) ;

    /**
     * Completed.
     * @param completed The completed notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void completed(final NotificationType completed, final MAP map,
        final ArjunaContext arjunaContext) ;

    /**
     * Exit.
     * @param exit The exit notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void exit(final NotificationType exit, final MAP map, final ArjunaContext arjunaContext) ;

    /**
     * Cannot complete.
     * @param cannotComplete The cannot complete notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void cannotComplete(final NotificationType cannotComplete, final MAP map,
        final ArjunaContext arjunaContext) ;

    /**
     * Get Status.
     * @param getStatus The get status notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void getStatus(final NotificationType getStatus, final MAP map, final ArjunaContext arjunaContext) ;

    /**
     * Status.
     * @param status The status.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void status(final StatusType status, final MAP map, final ArjunaContext arjunaContext) ;

    /**
     * SOAP fault.
     * @param soapFault The SOAP fault.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void soapFault(final SoapFault soapFault, final MAP map,
        final ArjunaContext arjunaContext) ;
}