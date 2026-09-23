package com.arjuna.wst11.stub;

import com.arjuna.wst.*;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst.SystemException;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.mwlabs.wscf.model.sagas.arjunacore.subordinate.SubordinateBACoordinator;
import com.arjuna.wst11.*;
import com.arjuna.wst11.BAParticipantManager;
import com.arjuna.webservices11.wsba.BusinessActivityConstants;
import org.jboss.jbossts.xts.recovery.participant.ba.PersistableBAParticipant;
import org.jboss.jbossts.xts.recovery.participant.ba.XTSBARecoveryManager;

import java.io.IOException;

/**
 * A coordinator completion  participant registered on behalf of an interposed WS-BA coordinator in order
 * to ensure that durable participants in the interposed transaction are completed, closed or cancelled
 * when requested from the parent transaction.
 */
public class SubordinateCoordinatorCompletionParticipantStub
        implements BusinessAgreementWithCoordinatorCompletionParticipant, PersistableParticipant, PersistableBAParticipant
{
    public SubordinateCoordinatorCompletionParticipantStub(SubordinateBACoordinator coordinator)
    {
        this.coordinator = coordinator;
        this.coordinatorId = coordinator.get_uid().stringForm();
        this.recovered = false;
        this.manager = null;
    }

    public SubordinateCoordinatorCompletionParticipantStub()
    {
        this.coordinator = null;
        this.coordinatorId = null;
        this.recovered = true;
        this.manager = null;
    }

    /**
     * set the participant manager for this stub. this cannot be supplied to the constructor because it
     * refers to the engine which drives this participant and the engine can only be created once the
     * participant has been created. it is needed in order to be able to send a fail upward to the
     * parent coordinator during complete processing. it is not needed during close or compensate
     * processing which is ok because we cannot save and restore it anyway.
     *
     * @param manager the manager for this participant
     */
    public void setManager(BAParticipantManager manager)
    {
        this.manager = manager;
    }

    public void complete() throws WrongStateException, SystemException {
        if (!recovered) {
            // the coordinator will send complete to all participants and then
            // also run phase one commit. the former may throw an exception.
            // if the latter succeeds the tx state will be COMMITTING whereas
            // if it fails it will be ABORTED.
            try {
                coordinator.complete();
            } catch (com.arjuna.mw.wsas.exceptions.WrongStateException wse) {
                throw new WrongStateException(wse.getMessage());
            } catch (com.arjuna.mw.wsas.exceptions.SystemException se) {
                throw new SystemException(se.getMessage());
            }
            // if status is COMMITTING then we return allowing the participant to be logged
            // if status is ABORTED then the participant must fail avoiding any logging

            if (coordinator.status() == ActionStatus.ABORTED) {
                manager.fail(BusinessActivityConstants.WSBA_ELEMENT_FAIL_QNAME);
            } else {
                // null out the manager so we don't attempt to save it to the log
                manager = null;
            }
        } else {
            // we should never get asked to complete a recovered activity
            throw new WrongStateException();
        }
    }

    public void close() throws WrongStateException, SystemException {
        if (!recovered) {
            int result;
            try {
                result = coordinator.close();
            } catch (com.arjuna.mw.wsas.exceptions.SystemException se) {
                throw new SystemException(se.getMessage());
            }
            if (result != ActionStatus.COMMITTED) {
                throw new SystemException("failed to close subordinate transaction " + coordinatorId);
            }
        } else {
            XTSBARecoveryManager recoveryManager = null;
            boolean isRecoveryScanStarted = false;
            if (coordinator == null) {
                // try fetching coordinator from the recovery manager
                recoveryManager = XTSBARecoveryManager.getRecoveryManager();
                // check whether recovery has started before we check for the presence
                // of the subordinate coordinator
                isRecoveryScanStarted = recoveryManager.isSubordinateCoordinatorRecoveryStarted();
                coordinator = SubordinateBACoordinator.getRecoveredCoordinator(coordinatorId);
            }
            if (coordinator == null) {
                // hmm, still null -- see if we have finished recovery scanning
                if (!isRecoveryScanStarted) {
                    // the subtransaction may still be waiting to be resolved
                    // throw an exception causing the commit to be retried later
                    throw new SystemException();
                }
                // ok we have no transaction to commit so assume we already committed it and
                // return without error
            } else if(!coordinator.isActivated()) {
                // the transaction was logged but has not yet been recovered successfully
                // throw an exception causing the commit to be retried later
                    throw new SystemException();
            } else {
                int status = coordinator.status();

                if (status == ActionStatus.PREPARED || status == ActionStatus.COMMITTING) {
                    // ok, the commit process was not previously initiated so start it now
                    try {
                        coordinator.close();
                        SubordinateBACoordinator.removeActiveProxy(coordinatorId);
                    } catch (com.arjuna.mw.wsas.exceptions.SystemException e) {
                        // throw an exception causing the commit to be retried later
                        throw new SystemException();
                    }
                    status = coordinator.status();
                }

                // TODO -- check if this is really necessary given the catch above?
                // check that we are not still committing because of a comms timeout

                if (status == ActionStatus.COMMITTING) {
                    // throw an exception causing the commit to be retried later
                    throw new SystemException();
                }
            }
        }
    }

    public void cancel() throws FaultedException, WrongStateException, SystemException {
        if (!recovered) {
            int result = coordinator.cancel();
            if (result != ActionStatus.ABORTED) {
                throw new FaultedException("failed to compensate subordinate transaction " + coordinatorId);
            }
        } else {
            XTSBARecoveryManager recoveryManager = null;
            boolean isRecoveryScanStarted = false;
            if (coordinator == null) {
                // try fetching coordinator from the recovery manager
                recoveryManager = XTSBARecoveryManager.getRecoveryManager();
                // check whether recovery has started before we check for the presence
                // of the subordinate coordinator
                isRecoveryScanStarted = recoveryManager.isSubordinateCoordinatorRecoveryStarted();
                coordinator = SubordinateBACoordinator.getRecoveredCoordinator(coordinatorId);
            }
            if (coordinator == null) {
                // hmm, still null -- see if we have finished recovery scanning
                if (!isRecoveryScanStarted) {
                    // the subtransaction may still be waiting to be resolved
                    // throw an exception causing the commit to be retried later
                    throw new SystemException();
                }
                // ok we have no transaction to commit so assume we already committed it and
                // return without error
            } else if(!coordinator.isActivated()) {
                // the transaction was logged but has not yet been recovered successfully
                // throw an exception causing the commit to be retried later
                    throw new SystemException();
            } else {
                int status = coordinator.status();

                if (status == ActionStatus.PREPARED || status == ActionStatus.COMMITTING) {
                    // ok, the commit process was not previously initiated so start it now
                    coordinator.cancel();
                    SubordinateBACoordinator.removeActiveProxy(coordinatorId);
                }

                // check that we are not still committing because of a comms timeout

                if (status == ActionStatus.COMMITTING) {
                    // throw an exception causing the commit to be retried later
                    throw new SystemException();
                } else if (status != ActionStatus.ABORTED) {
                    throw new FaultedException();
                }
            }
        }
    }

    public void compensate() throws FaultedException, WrongStateException, SystemException {
        if (!recovered) {
            int result = coordinator.cancel();
            // test result and throw a SystemException if the compensate was delayed
        } else {
            XTSBARecoveryManager recoveryManager = null;
            boolean isRecoveryScanStarted = false;
            if (coordinator == null) {
                // try fetching coordinator from the recovery manager
                recoveryManager = XTSBARecoveryManager.getRecoveryManager();
                // check whether recovery has started before we check for the presence
                // of the subordinate coordinator
                isRecoveryScanStarted = recoveryManager.isSubordinateCoordinatorRecoveryStarted();
                coordinator = SubordinateBACoordinator.getRecoveredCoordinator(coordinatorId);
            }
            if (coordinator == null) {
                // hmm, still null -- see if we have finished recovery scanning
                if (!isRecoveryScanStarted) {
                    // the subtransaction may still be waiting to be resolved
                    // throw an exception causing the commit to be retried later
                    throw new SystemException();
                }
                // ok we have no transaction to commit so assume we already committed it and
                // return without error
            } else if(!coordinator.isActivated()) {
                // the transaction was logged but has not yet been recovered successfully
                // throw an exception causing the commit to be retried later
                    throw new SystemException();
            } else {
                int status = coordinator.status();

                if (status == ActionStatus.PREPARED || status == ActionStatus.COMMITTING) {
                    // ok, the commit process was not previously initiated so start it now
                    coordinator.cancel();
                    SubordinateBACoordinator.removeActiveProxy(coordinatorId);
                    status = coordinator.status();
                }

                // TODO -- check if this is really necessary given the catch above?
                // check that we are not still committing because of a comms timeout

                if (status == ActionStatus.COMMITTING) {
                    // throw an exception causing the commit to be retried later
                    throw new SystemException();
                } else if (status != ActionStatus.ABORTED) {
                    throw new FaultedException();
                }
            }
        }
    }

    /**
     * this should never get called
     * @throws com.arjuna.wst.SystemException
     */
    public String status() throws SystemException
    {
        return null;
    }

    /**
     * this should never get called
     * @throws com.arjuna.wst.SystemException
     */
    public void unknown() throws SystemException
    {
    }

    /**
     * this should never get called
     * @throws com.arjuna.wst.SystemException
     */
    public void error() throws SystemException
    {
    }

    /**
     * Save the state of the particpant to the specified input object stream.
     * @param oos The output output stream.
     * @return true if persisted, false otherwise.
     */
    public boolean saveState(OutputObjectState oos) {
        // we need to save the id of the subordinate coordinator so we can identify it again
        // when we are recreated
        try {
            oos.packString(coordinatorId);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Restore the state of the particpant from the specified input object stream.
     * @param ios The Input object stream.
     * @return true if restored, false otherwise.
     */
    public boolean restoreState(InputObjectState ios) {
        // restore the subordinate coordinator id so we can check to ensure it has been committed
        try {
            coordinatorId = ios.unpackString();
            SubordinateBACoordinator.addActiveProxy(coordinatorId);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /*
     * this participant implements the PersistableBAParticipant interface so it can save its state.
     * recovery is managed by an XTS recovery module
     * @return
     * @throws Exception
     */
    public byte[] getRecoveryState() throws Exception {
        OutputObjectState oos = new OutputObjectState();
        oos.packString(this.getClass().getName());
        this.saveState(oos);
        return oos.buffer();
    }

    private SubordinateBACoordinator coordinator;
    private String coordinatorId;
    private boolean recovered;
    private BAParticipantManager manager;
}
