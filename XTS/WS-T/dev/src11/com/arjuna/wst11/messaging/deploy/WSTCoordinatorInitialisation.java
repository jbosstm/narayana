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
package com.arjuna.wst11.messaging.deploy;

import com.arjuna.webservices11.wsarjtx.processors.TerminationCoordinatorProcessor;
import com.arjuna.webservices11.wsarjtx.processors.TerminationCoordinatorRPCProcessor;
import com.arjuna.webservices11.wsat.processors.CompletionCoordinatorProcessor;
import com.arjuna.webservices11.wsat.processors.CompletionCoordinatorRPCProcessor;
import com.arjuna.webservices11.wsat.processors.CoordinatorProcessor;
import com.arjuna.webservices11.wsba.processors.CoordinatorCompletionCoordinatorProcessor;
import com.arjuna.webservices11.wsba.processors.ParticipantCompletionCoordinatorProcessor;
import com.arjuna.wst11.messaging.*;

/**
 * Initialise the transaction coordinator services.
 * @author kevin
 */
public class WSTCoordinatorInitialisation
{
    /**
     * The context has been initialized.
     * @param servletContextEvent The servlet context event.
     */
    public static void startup()
    {
        CompletionCoordinatorProcessor.setProcessor(new CompletionCoordinatorProcessorImpl()) ;
        CompletionCoordinatorRPCProcessor.setProcessor(new CompletionCoordinatorRPCProcessorImpl()) ;
        CoordinatorProcessor.setProcessor(new CoordinatorProcessorImpl()) ;
        TerminationCoordinatorProcessor.setProcessor(new TerminationCoordinatorProcessorImpl()) ;
        TerminationCoordinatorRPCProcessor.setProcessor(new TerminationCoordinatorRPCProcessorImpl()) ;
        CoordinatorCompletionCoordinatorProcessor.setProcessor(new CoordinatorCompletionCoordinatorProcessorImpl()) ;
        ParticipantCompletionCoordinatorProcessor.setProcessor(new ParticipantCompletionCoordinatorProcessorImpl()) ;
    }

    /**
     * The context is about to be destroyed.
     * @param servletContextEvent The servlet context event.
     */
    public static void shutdown()
    {
    }
}