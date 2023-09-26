/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.webservices11.wscoor.processors;

import org.oasis_open.docs.ws_tx.wscoor._2006._06.CreateCoordinationContextType;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CreateCoordinationContextResponseType;
import org.jboss.ws.api.addressing.MAP;

/**
 * The Activation Coordinator processor.
 * @author kevin
 */
public abstract class ActivationCoordinatorProcessor
{
    /**
     * The coordinator.
     */
    private static ActivationCoordinatorProcessor COORDINATOR  ;
    
    /**
     * Get the coordinator.
     * @return The coordinator.
     */
    public static ActivationCoordinatorProcessor getCoordinator()
    {
        return COORDINATOR ;
    }
    
    /**
     * Set the coordinator.
     * @param coordinator The coordinator.
     * @return The previous coordinator.
     */
    public static ActivationCoordinatorProcessor setCoordinator(final ActivationCoordinatorProcessor coordinator)
    {
        final ActivationCoordinatorProcessor origCoordinator = COORDINATOR ;
        COORDINATOR = coordinator ;
        return origCoordinator ;
    }
    
    /**
     * Create the coordination context.
     * @param createCoordinationContext The create coordination context request.
     * @param map The addressing context.
     * @return a response message containing the desired coordinaton context
     */
    public abstract CreateCoordinationContextResponseType
        createCoordinationContext(final CreateCoordinationContextType createCoordinationContext,
                                  final MAP map,
                                  final boolean isSecure) ;
}