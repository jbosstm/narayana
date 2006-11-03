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
/*
 * Copyright (C) 2001
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: RcvCoManager.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.internal.jts.recovery.recoverycoordinators;

import org.omg.CosTransactions.RecoveryCoordinator;
import org.omg.CORBA.SystemException;

import com.arjuna.ats.arjuna.common.Uid;

/**
 * Interface to handle the creation of RecoveryCoordinator IORs.
 * Implementations will be orb-specific. For some orbs this will involve the creation of a
 * RecoveryCoordinator implementation object in the original
 * process (i.e. the JVM where the Coordinator is). For other orbs (including POA orbs)
 * the IOR is created but no servant (Java object) is constructed. In all cases, a 
 * RecoveryCoordinator object will be created (or a servant assigned) in the RecoveryManager
 * if a replay_completion request is received.
 */

public interface RcvCoManager
{
/**
 *  Make a RecoveryCoordinator IOR.
 *  @param RCUid The Uid of this RecoveryCoordinator. Used to identify this branch
 *    when replacing the original Resource reference with one received in replay_completion.
 *  @param rootActionUid The Uid of the transaction.
 *  @param processUid The Uid of the originating process. Used by RecoveryManager to 
 *    determine whether the original process is still working on the transaction.
 *  @param isServerTransaction Flag to indicate whether this is a root or intermediate 
 *    coordinator.
 */
RecoveryCoordinator makeRC( Uid RCUid, Uid rootActionUid,
		Uid processUid, boolean isServerTransaction );
/**
 * The RecoveryCoordinator instance is no longer needed. Will be a null-op in environments
 * where there is no real implementation object.
 */
void destroy (RecoveryCoordinator rc) throws SystemException;

/**
 * The RecoveryCoordinator instances used by the transactions whose Uids are in params are
 * no longer needed. Will be a null-op in environments
 * where there is no real implementation object.
 */
void destroyAll (Object[] params) throws SystemException;

};
