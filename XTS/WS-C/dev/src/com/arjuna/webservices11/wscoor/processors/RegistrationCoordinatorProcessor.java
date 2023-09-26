/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.webservices11.wscoor.processors;

import com.arjuna.webservices11.wsarj.ArjunaContext;
import org.jboss.ws.api.addressing.MAP;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.RegisterResponseType;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.RegisterType;

/**
 * The Registration Coordinator processor.
 * @author kevin
 */
public abstract class RegistrationCoordinatorProcessor
{
    /**
     * The coordinator.
     */
    private static RegistrationCoordinatorProcessor COORDINATOR ;
    
    /**
     * Get the coordinator.
     * @return The coordinator.
     */
    public static RegistrationCoordinatorProcessor getCoordinator()
    {
        return COORDINATOR ;
    }
    
    /**
     * Set the coordinator.
     * @param coordinator The coordinator.
     * @return The orig coordinator.
     */
    public static RegistrationCoordinatorProcessor setCoordinator(final RegistrationCoordinatorProcessor coordinator)
    {
        final RegistrationCoordinatorProcessor origCoordinator = COORDINATOR ;
        COORDINATOR = coordinator ;
        return origCoordinator ;
    }

    /**
     * Register the participant in the protocol.
     * @param register The register request.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract RegisterResponseType register(final RegisterType register, final MAP map,
        final ArjunaContext arjunaContext, final boolean isSecure) ;
}