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
package com.arjuna.webservices.wsat;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsarj.ArjunaContext;


/**
 * The Coordinator events.
 */
public interface CoordinatorInboundEvents
{
    /**
     * Handle the aborted event.
     * @param aborted The aborted notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void aborted(final NotificationType aborted, final AddressingContext addressingContext, final ArjunaContext arjunaContext) ;
    
    /**
     * Handle the committed event.
     * @param committed The committed notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void committed(final NotificationType committed, final AddressingContext addressingContext, final ArjunaContext arjunaContext) ;
    
    /**
     * Handle the prepared event.
     * @param prepared The prepared notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void prepared(final NotificationType prepared, final AddressingContext addressingContext, final ArjunaContext arjunaContext) ;
    
    /**
     * Handle the readOnly event.
     * @param readOnly The readOnly notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void readOnly(final NotificationType readOnly, final AddressingContext addressingContext, final ArjunaContext arjunaContext) ;
    
    /**
     * Handle the replay event.
     * @param replay The replay notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void replay(final NotificationType aborted, final AddressingContext addressingContext, final ArjunaContext arjunaContext) ;
    
    /**
     * Handle the soap fault event.
     * @param soapFault The soap fault.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void soapFault(final SoapFault soapFault, final AddressingContext addressingContext, final ArjunaContext arjunaContext) ;
}
