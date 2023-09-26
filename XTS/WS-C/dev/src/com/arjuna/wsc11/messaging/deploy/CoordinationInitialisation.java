/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.wsc11.messaging.deploy;

import com.arjuna.webservices11.wscoor.processors.ActivationCoordinatorProcessor;
import com.arjuna.webservices11.wscoor.processors.RegistrationCoordinatorProcessor;
import com.arjuna.wsc11.messaging.ActivationCoordinatorProcessorImpl;
import com.arjuna.wsc11.messaging.RegistrationCoordinatorProcessorImpl;

/**
 * Initialise the coordination services.
 * @author kevin
 */
public class CoordinationInitialisation
{
    public static void startup()
    {
        ActivationCoordinatorProcessor.setCoordinator(new ActivationCoordinatorProcessorImpl()) ;
        RegistrationCoordinatorProcessor.setCoordinator(new RegistrationCoordinatorProcessorImpl()) ;
    }

    public static void shutdown()
    {
    }
}