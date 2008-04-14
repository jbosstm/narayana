/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
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
package com.arjuna.webservices11.wscoor.processors;

import javax.xml.ws.addressing.AddressingProperties;

import org.oasis_open.docs.ws_tx.wscoor._2006._06.CreateCoordinationContextType;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CreateCoordinationContextResponseType;

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
     * @param addressingContext The addressing context.
     * @return a response message containing the desired coordinaton context
     */
    public abstract CreateCoordinationContextResponseType
        createCoordinationContext(final CreateCoordinationContextType createCoordinationContext,
                                  final AddressingProperties addressingProperties) ;
}
