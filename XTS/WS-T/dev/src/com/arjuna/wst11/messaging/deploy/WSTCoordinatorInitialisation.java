/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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
     */
    public static void shutdown()
    {
    }
}