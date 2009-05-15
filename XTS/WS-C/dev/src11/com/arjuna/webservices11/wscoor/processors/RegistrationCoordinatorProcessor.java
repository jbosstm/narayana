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
package com.arjuna.webservices11.wscoor.processors;

import com.arjuna.webservices11.wsarj.ArjunaContext;
import org.jboss.jbossts.xts.wsaddr.map.MAP;
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
