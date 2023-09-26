/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.arjuna.ats.internal.jts.recovery.recoverycoordinators;

import org.omg.CORBA.SystemException;
import org.omg.CosTransactions.RecoveryCoordinator;

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