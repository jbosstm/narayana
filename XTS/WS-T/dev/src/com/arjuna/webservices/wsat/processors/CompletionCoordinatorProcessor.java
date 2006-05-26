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

import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsarj.ArjunaContext;
import com.arjuna.webservices.wsat.NotificationType;
import com.arjuna.wst.CompletionCoordinatorParticipant;

/**
 * The Completion Coordinator processor.
 * @author kevin
 */
public abstract class CompletionCoordinatorProcessor
{
    /**
     * The coordinator.
     */
    private static CompletionCoordinatorProcessor COORDINATOR ;
    
    /**
     * Get the coordinator.
     * @return The coordinator.
     */
    public static synchronized CompletionCoordinatorProcessor getCoordinator()
    {
        return COORDINATOR ;
    }

    /**
     * Set the coordinator.
     * @param coordinator The coordinator.
     * @return The previous coordinator.
     */
    public static synchronized CompletionCoordinatorProcessor setCoordinator(final CompletionCoordinatorProcessor coordinator)
    {
        final CompletionCoordinatorProcessor origCoordinator = COORDINATOR ;
        COORDINATOR = coordinator ;
        return origCoordinator ;
    }

    /**
     * Activate the participant.
     * @param participant The participant.
     * @param identifier The identifier.
     */
    public abstract void activateParticipant(final CompletionCoordinatorParticipant participant, final String identifier) ;

    /**
     * Deactivate the participant.
     * @param participant The participant.
     */
    public abstract void deactivateParticipant(final CompletionCoordinatorParticipant participant) ;

    /**
     * Commit.
     * @param commit The commit notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void commit(final NotificationType commit, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext) ;
    
    /**
     * Rollback.
     * @param rollback The rollback notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void rollback(final NotificationType rollback, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext) ;
}
