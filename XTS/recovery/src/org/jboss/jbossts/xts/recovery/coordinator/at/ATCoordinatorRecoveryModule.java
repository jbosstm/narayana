/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2007,
 * @author Red Hat Middleware LLC.
 */
package org.jboss.jbossts.xts.recovery.coordinator.at;

import com.arjuna.ats.arjuna.objectstore.StateStatus;
import org.jboss.jbossts.xts.logging.XTSLogger;
import org.jboss.jbossts.xts.recovery.participant.at.XTSATRecoveryManager;

import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.arjuna.recovery.TransactionStatusConnectionManager;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.internal.arjuna.common.UidHelper;

import com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ATCoordinator;

import java.util.Vector;
import java.util.Enumeration;

/**
 * This class is a plug-in module for the recovery manager.
 * It is responsible for recovering failed XTS AT (ACCoordinator) transactions.
 * (instances of com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ACCoordinator)
 *
 * $Id$
 *
 */
public class ATCoordinatorRecoveryModule implements RecoveryModule
{
    public ATCoordinatorRecoveryModule()
    {
        if (XTSLogger.arjLogger.isDebugEnabled()) {
            XTSLogger.arjLogger.debug("ATCoordinatorRecoveryModule created - default");
        }

        if (_transactionStore == null)
        {
            _transactionStore = TxControl.getStore() ;
        }

        _transactionStatusConnectionMgr = new TransactionStatusConnectionManager() ;
    }

    /**
     * called by the service startup code before the recovery module is added to the recovery managers
     * module list
     */
    public void install()
    {
        Implementations.install();
    }

    /**
     * module list in order to allow the implementations list to be purged of this module's implementations
     */
    public void uninstall()
    {
        Implementations.uninstall();
    }

    /**
     * This is called periodically by the RecoveryManager
     */
    public void periodicWorkFirstPass()
    {
        // Transaction type
        boolean ACCoordinators = false ;

        // uids per transaction type
        InputObjectState acc_uids = new InputObjectState() ;

        try
        {
            if (XTSLogger.arjLogger.isDebugEnabled()) {
                XTSLogger.arjLogger.debug("StatusModule: first pass ");
            }

            ACCoordinators = _transactionStore.allObjUids( _transactionType, acc_uids );

        }
        catch ( ObjectStoreException ex )
        {
            XTSLogger.i18NLogger.warn_coordinator_at_ATCoordinatorRecoveryModule_1(ex);
        }

        if ( ACCoordinators )
        {
            _transactionUidVector = processTransactions( acc_uids ) ;
        }
    }

    public void periodicWorkSecondPass()
    {
        if (XTSLogger.arjLogger.isDebugEnabled()) {
            XTSLogger.arjLogger.debug("ATCoordinatorRecoveryModule: Second pass ");
        }

        if (_transactionUidVector != null) {
            processTransactionsStatus() ;
        }

        // ok notify the coordinator processor that recovery processing has completed

    }

    protected ATCoordinatorRecoveryModule(String type)
    {
        if (XTSLogger.arjLogger.isDebugEnabled()) {
            XTSLogger.arjLogger.debug("ATCoordinatorRecoveryModule created " + type);
        }

        if (_transactionStore == null)
        {
            _transactionStore = TxControl.getStore() ;
        }

        _transactionStatusConnectionMgr = new TransactionStatusConnectionManager() ;
        _transactionType = type;

    }

    private void doRecoverTransaction( Uid recoverUid )
    {
        boolean commitThisTransaction = true ;

        // Retrieve the transaction status from its original process.
        // n.b. for a non-active XTS TX this status wil l always be committed even
        // if it aborted or had a heuristic outcome. in that case we need to use
        // the logged action status which can only be retrieved after activation

        int theStatus = _transactionStatusConnectionMgr.getTransactionStatus( _transactionType, recoverUid ) ;

        boolean inFlight = isTransactionInMidFlight( theStatus ) ;

        String Status = ActionStatus.stringForm( theStatus ) ;

        if (XTSLogger.arjLogger.isDebugEnabled()) {
            XTSLogger.arjLogger.debug("transaction type is " + _transactionType + " uid is " +
                    recoverUid.toString() + "\n ActionStatus is " + Status +
                    " in flight is " + inFlight);
        }

        if ( ! inFlight )
        {
            try {
                XTSLogger.arjLogger.debug("jjh doing revovery here for " + recoverUid);
                // TODO jjh
                RecoveryATCoordinator rcvACCoordinator =
                        new RecoveryATCoordinator(recoverUid);
//                RecoverAtomicAction rcvAtomicAction =
//                        new RecoverAtomicAction( recoverUid, theStatus ) ;

//                rcvAtomicAction.replayPhase2() ;
                rcvACCoordinator.replayPhase2();
            }
            catch ( Exception ex )
            {
                XTSLogger.i18NLogger.warn_coordinator_at_ATCoordinatorRecoveryModule_2(recoverUid, ex);
            }
        }
    }

    private boolean isTransactionInMidFlight( int status )
    {
        boolean inFlight = false ;

        switch ( status )
        {
            // these states can only come from a process that is still alive
            case ActionStatus.RUNNING    :
            case ActionStatus.ABORT_ONLY :
            case ActionStatus.PREPARING  :
            case ActionStatus.COMMITTING :
            case ActionStatus.ABORTING   :
            case ActionStatus.PREPARED   :
                inFlight = true ;
                break ;

                // the transaction is apparently still there, but has completed its
                // phase2. should be safe to redo it.
            case ActionStatus.COMMITTED  :
            case ActionStatus.H_COMMIT   :
            case ActionStatus.H_MIXED    :
            case ActionStatus.H_HAZARD   :
            case ActionStatus.ABORTED    :
            case ActionStatus.H_ROLLBACK :
                inFlight = false ;
                break ;

                // this shouldn't happen
            case ActionStatus.INVALID :
            default:
                inFlight = false ;
        }

        return inFlight ;
    }

    private Vector processTransactions( InputObjectState uids )
    {
        Vector uidVector = new Vector() ;

        if (XTSLogger.arjLogger.isDebugEnabled()) {
            XTSLogger.arjLogger.debug("processing " + _transactionType
                    + " transactions");
        }

        final Uid NULL_UID = Uid.nullUid();
        Uid theUid = null;

        while (true)
        {
            try
            {
                theUid = UidHelper.unpackFrom( uids ) ;

            }
            catch ( Exception ex )
            {
                break;
            }
            if (theUid.equals(NULL_UID))
            {
                break;
            }
            if (XTSLogger.arjLogger.isDebugEnabled()) {
                XTSLogger.arjLogger.debug("found transaction " + theUid);
            }

            uidVector.addElement( theUid ) ;
        }

        return uidVector ;
    }

    private void processTransactionsStatus()
    {
        // Process the Vector of transaction Uids
        Enumeration transactionUidEnum = _transactionUidVector.elements() ;

        while ( transactionUidEnum.hasMoreElements() )
        {
            Uid currentUid = (Uid) transactionUidEnum.nextElement();

            try
            {
                if ( _transactionStore.currentState( currentUid, _transactionType ) != StateStatus.OS_UNKNOWN )
                {
                    doRecoverTransaction( currentUid ) ;
                }
            }
            catch ( ObjectStoreException ex )
            {
                XTSLogger.i18NLogger.warn_coordinator_at_ATCoordinatorRecoveryModule_3(currentUid, ex);
            }
        }

        XTSATRecoveryManager.getRecoveryManager().setCoordinatorRecoveryStarted();
    }

    // 'type' within the Object Store for ACCoordinator.
    private String _transactionType = new ATCoordinator().type() ;

    // Array of transactions found in the object store of the
    // ACCoordinator type.
    private Vector _transactionUidVector = null ;

    // Reference to the Object Store.
    private static ObjectStore _transactionStore = null ;

    // This object manages the interface to all TransactionStatusManagers
    // processes(JVMs) on this system/node.
    private TransactionStatusConnectionManager _transactionStatusConnectionMgr ;

}

