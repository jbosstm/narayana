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
package com.arjuna.webservices11.wsba;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import org.oasis_open.docs.ws_tx.wsba._2006._06.NotificationType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.ExceptionType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.StatusType;

import javax.xml.ws.addressing.AddressingProperties;


/**
 * The Participant events.
 */
public interface CoordinatorCompletionCoordinatorInboundEvents
{
    /**
     * Handle the cancelled event.
     * @param cancelled The cancelled notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void cancelled(final NotificationType cancelled, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext) ;

    /**
     * Handle the closed event.
     * @param closed The closed notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void closed(final NotificationType closed, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext) ;

    /**
     * Handle the compensated event.
     * @param compensated The compensated notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void compensated(final NotificationType compensated, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext) ;

    /**
     * Handle the completed event.
     * @param completed The completed notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void completed(final NotificationType completed, final AddressingProperties addressingProperties,
        final ArjunaContext arjunaContext) ;

    /**
     * Handle the exit event.
     * @param exit The exit notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void exit(final NotificationType exit, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext) ;

    /**
     * Handle the fail event.
     * @param fail The fail exception.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void fail(final ExceptionType fail, final AddressingProperties addressingProperties,
        final ArjunaContext arjunaContext) ;

    /**
     * Handle the cannotComplete event.
     * @param cannotComplete The cannotComplete notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void cannotComplete(final NotificationType cannotComplete, final AddressingProperties addressingProperties,
        final ArjunaContext arjunaContext) ;

    /**
     * Handle the getStatus event.
     * @param getStatus The getStatus notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void getStatus(final NotificationType getStatus, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext) ;

    /**
     * Handle the status event.
     * @param status The status.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void status(final StatusType status, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext) ;

    /**
     * Handle the soap fault event.
     * @param soapFault The soap fault.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void soapFault(final SoapFault soapFault, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext) ;
}