/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors. 
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
package com.arjuna.ats.arjuna.recovery ;

/**
 * Interface for Recovery manager plug-in module.
 * RecoveryActivators are registered via the properties mechanisms.
 * The startRCservice of each Activator is called to create the appropriate 
 * Recovery Component able to receive recovery requests according to a particular
 * transaction protocol. For instance, when used with OTS, the RecoveryActivitor 
 * has the responsibility to create a RecoveryCoordinator object able to respond
 * to the replay_completion operation.
 *
 * @author Malik Saheb
 * @since ArjunaTS 3.0
 */

public interface RecoveryActivator
{
    /**
     * Called to create appropriate instance(s), specific to a standard transaction protocol,
     * able to receive inquiries for recovery
     */
    public boolean startRCservice();
    /*
     * For the moment let's say that this operation doesn't take any arguments.
     * If let without arguments we should obtain the Recoverymanager Tag within the class that 
     * load the RecoveryActivators
     */

}
