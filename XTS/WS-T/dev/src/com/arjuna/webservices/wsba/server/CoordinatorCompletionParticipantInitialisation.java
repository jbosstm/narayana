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
package com.arjuna.webservices.wsba.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.arjuna.webservices.HandlerRegistry;
import com.arjuna.webservices.SoapRegistry;
import com.arjuna.webservices.SoapService;
import com.arjuna.webservices.wsaddr.policy.AddressingPolicy;
import com.arjuna.webservices.wsarj.policy.ArjunaPolicy;
import com.arjuna.webservices.wsba.BusinessActivityConstants;
import com.arjuna.webservices.wsba.policy.CoordinatorCompletionParticipantPolicy;

/**
 * Activate the Coordinator Completion Participant service
 * @author kevin
 */
public class CoordinatorCompletionParticipantInitialisation implements ServletContextListener
{
    /**
     * The context has been initialized.
     * @param servletContextEvent The servlet context event.
     */
    public void contextInitialized(final ServletContextEvent servletContextEvent)
    {
        final HandlerRegistry handlerRegistry = new HandlerRegistry() ;
        
        // Add WS-Addressing
        AddressingPolicy.register(handlerRegistry) ;
        // Add Arjuna handlers
        ArjunaPolicy.register(handlerRegistry) ;
        // Add Coordinator Completion participant.
        CoordinatorCompletionParticipantPolicy.register(handlerRegistry) ;
        
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        soapRegistry.registerSoapService(BusinessActivityConstants.SERVICE_COORDINATOR_COMPLETION_PARTICIPANT,
            new SoapService(handlerRegistry)) ;
    }

    /**
     * The context is about to be destroyed.
     * @param servletContextEvent The servlet context event.
     */
    public void contextDestroyed(final ServletContextEvent servletContextEvent)
    {
    }
}
