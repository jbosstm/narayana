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
package com.arjuna.webservices.wsba;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsarj.ArjunaContext;


/**
 * The Participant events.
 */
public interface ParticipantCompletionParticipantInboundEvents
{
    /**
     * Handle the cancel event.
     * @param cancel The cancel notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void cancel(final NotificationType cancel, final AddressingContext addressingContext, final ArjunaContext arjunaContext) ;
    
    /**
     * Handle the close event.
     * @param close The close notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void close(final NotificationType close, final AddressingContext addressingContext, final ArjunaContext arjunaContext) ;
    
    /**
     * Handle the compensate event.
     * @param compensate The compensate notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void compensate(final NotificationType compensate, final AddressingContext addressingContext, final ArjunaContext arjunaContext) ;
    
    /**
     * Handle the exited event.
     * @param exited The exited notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void exited(final NotificationType exited, final AddressingContext addressingContext, final ArjunaContext arjunaContext) ;
    
    /**
     * Handle the faulted event.
     * @param faulted The faulted notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void faulted(final NotificationType faulted, final AddressingContext addressingContext, final ArjunaContext arjunaContext) ;
    
    /**
     * Handle the getStatus event.
     * @param getStatus The getStatus notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void getStatus(final NotificationType getStatus, final AddressingContext addressingContext, final ArjunaContext arjunaContext) ;
    
    /**
     * Handle the status event.
     * @param status The status type.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void status(final StatusType status, final AddressingContext addressingContext, final ArjunaContext arjunaContext) ;
    
    /**
     * Handle the soap fault event.
     * @param soapFault The soap fault.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void soapFault(final SoapFault soapFault, final AddressingContext addressingContext, final ArjunaContext arjunaContext) ;
}
