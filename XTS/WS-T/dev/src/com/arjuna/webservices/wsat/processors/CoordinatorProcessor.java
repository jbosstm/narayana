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
package com.arjuna.webservices.wsat.processors;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.base.processors.Callback;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsarj.ArjunaContext;
import com.arjuna.webservices.wsarj.processor.BaseNotificationProcessor;
import com.arjuna.webservices.wsat.NotificationType;

/**
 * The Coordinator processor.
 * @author kevin
 */
public class CoordinatorProcessor extends BaseNotificationProcessor
{
    /**
     * The coordinator singleton.
     */
    private static final CoordinatorProcessor COORDINATOR = new CoordinatorProcessor() ;
    
    /**
     * Get the coordinator singleton.
     * @return The singleton.
     */
    public static CoordinatorProcessor getCoordinator()
    {
        return COORDINATOR ;
    }

    /**
     * Handle an aborted response.
     * @param aborted The aborted notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void handleAborted(final NotificationType aborted, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        handleCallbacks(new CallbackExecutorAdapter() {
            public void execute(final Callback callback) {
                ((CoordinatorCallback)callback).aborted(aborted, addressingContext, arjunaContext) ;
            }
        }, getIDs(addressingContext, arjunaContext)) ;
    }
    
    /**
     * Handle a committed response.
     * @param committed The committed notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void handleCommitted(final NotificationType committed, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        handleCallbacks(new CallbackExecutorAdapter() {
            public void execute(final Callback callback) {
                ((CoordinatorCallback)callback).committed(committed, addressingContext, arjunaContext) ;
            }
        }, getIDs(addressingContext, arjunaContext)) ;
    }
    
    /**
     * Handle a prepared response.
     * @param prepared The prepared notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void handlePrepared(final NotificationType prepared, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        handleCallbacks(new CallbackExecutorAdapter() {
            public void execute(final Callback callback) {
                ((CoordinatorCallback)callback).prepared(prepared, addressingContext, arjunaContext) ;
            }
        }, getIDs(addressingContext, arjunaContext)) ;
    }
    
    /**
     * Handle a read only response.
     * @param readOnly The read only notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void handleReadOnly(final NotificationType readOnly, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        handleCallbacks(new CallbackExecutorAdapter() {
            public void execute(final Callback callback) {
                ((CoordinatorCallback)callback).readOnly(readOnly, addressingContext, arjunaContext) ;
            }
        }, getIDs(addressingContext, arjunaContext)) ;
    }
    
    /**
     * Handle a replay response.
     * @param replay The replay notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void handleReplay(final NotificationType replay, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        handleCallbacks(new CallbackExecutorAdapter() {
            public void execute(final Callback callback) {
                ((CoordinatorCallback)callback).replay(replay, addressingContext, arjunaContext) ;
            }
        }, getIDs(addressingContext, arjunaContext)) ;
    }
    
    /**
     * Handle a SOAP fault response.
     * @param soapFault The SOAP fault.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void handleSoapFault(final SoapFault soapFault, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        handleCallbacks(new CallbackExecutorAdapter() {
            public void execute(final Callback callback) {
                ((CoordinatorCallback)callback).soapFault(soapFault, addressingContext, arjunaContext) ;
            }
        }, getIDs(addressingContext, arjunaContext)) ;
    }

    /**
     * Register a callback for the specific instance identifier.
     * @param instanceIdentifier The instance identifier.
     * @param callback The callback for the response.
     */
    public void registerCallback(final String instanceIdentifier, final CoordinatorCallback callback)
    {
        register(instanceIdentifier, callback) ;
    }
}
