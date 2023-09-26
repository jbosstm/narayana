/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.wst11.messaging.deploy;

import com.arjuna.webservices11.wsat.processors.ParticipantProcessor;
import com.arjuna.webservices11.wsba.processors.CoordinatorCompletionParticipantProcessor;
import com.arjuna.webservices11.wsba.processors.ParticipantCompletionParticipantProcessor;
import com.arjuna.wst11.messaging.*;

/**
 * Initialise the transaction participant services.
 * @author kevin
 */
public class WSTParticipantInitialisation
{
    public static void startup()
    {
        ParticipantProcessor.setProcessor(new ParticipantProcessorImpl()) ;
        CoordinatorCompletionParticipantProcessor.setProcessor(new CoordinatorCompletionParticipantProcessorImpl()) ;
        ParticipantCompletionParticipantProcessor.setProcessor(new ParticipantCompletionParticipantProcessorImpl()) ;
    }

    public static void shutdown()
    {
    }
}