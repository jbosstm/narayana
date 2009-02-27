package com.arjuna.wst11.stub;

import com.arjuna.wst.*;
import com.arjuna.mwlabs.wscf.model.twophase.arjunacore.subordinate.SubordinateCoordinator;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.arjuna.state.InputObjectState;

import java.io.IOException;

import org.jboss.jbossts.xts.recovery.participant.at.XTSATRecoveryManager;
import org.jboss.jbossts.xts.recovery.participant.at.PersistableATParticipant;

/**
 * A durable participant registered on behalf of an interposed WS-AT coordinator in order to ensure that
 * durable participants in the subtransaction prepared, committed and aborted at the right time.
 */
public class SubordinateDurable2PCStub implements Durable2PCParticipant, PersistableParticipant, PersistableATParticipant
{
    /**
     * normal constructor used when the subordinate coordinator is registered as a durable participant
     * with its parent coordinator.
     *
     * @param coordinator
     */
    public SubordinateDurable2PCStub(SubordinateCoordinator coordinator)
    {
        this.coordinator = coordinator;
        this.coordinatorId = coordinator.get_uid().stringForm();
        this.recovered = false;
    }

    /**
     * empty constructor for use only during recovery
     */
    public SubordinateDurable2PCStub()
    {
        this.coordinator = null;
        this.coordinatorId = null;
        this.recovered = true;
    }

    /**
     * This will be called when the parent coordinator is preparing its durable participants and should ensure
     * that the interposed cooordinator does the same.
     *
     * @return the Vote returned by the subordinate coordinator.
     * @throws com.arjuna.wst.WrongStateException if the subordinate coordinator does the same
     * @throws com.arjuna.wst.SystemException if the subordinate coordinator does the same
     */
    public Vote prepare() throws WrongStateException, SystemException {
        switch (coordinator.prepare())
        {
            case TwoPhaseOutcome.PREPARE_OK:
                return new Prepared();
            case TwoPhaseOutcome.PREPARE_READONLY:
                return new ReadOnly();
            case TwoPhaseOutcome.PREPARE_NOTOK:
            default:
                return new Aborted();
        }
    }

    /**
     * this will be called when the parent coordinator commits its durable participants and should ensure
     * that the interposed cooordinator does the same
     * @throws com.arjuna.wst.WrongStateException
     * @throws com.arjuna.wst.SystemException
     */

    public void commit() throws WrongStateException, SystemException {
        if (!isRecovered()) {
            coordinator.commit();
        } else {
            XTSATRecoveryManager recoveryManager = null;
            boolean isRecoveryScanStarted = false;
            if (coordinator == null) {
                // try fetching coordinator from the recovery manager
                recoveryManager = XTSATRecoveryManager.getRecoveryManager();
                // check whether recovery has started before we check for the presence
                // of the subordinate coordinator
                isRecoveryScanStarted = recoveryManager.isSubordinateCoordinatorRecoveryStarted();
                coordinator = SubordinateCoordinator.getRecoveredCoordinator(coordinatorId);
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
                    coordinator.commit();
                    status = coordinator.status();
                }

                // check that we are not still committing because of a comms timeout

                if (status == ActionStatus.COMMITTING) {
                    // throw an exception causing the commit to be retried later
                    throw new SystemException();
                }
            }
        }
    }

    /**
     * this will be called when the parent coordinator rolls back its durable participants and should ensure
     * that the interposed cooordinator does the same
     * @throws com.arjuna.wst.WrongStateException
     * @throws com.arjuna.wst.SystemException
     */

    public void rollback() throws WrongStateException, SystemException {
        if (!isRecovered()) {
            coordinator.rollback();
        } else {
            // first check whether crashed coordinators have been recovered
            XTSATRecoveryManager recoveryManager = XTSATRecoveryManager.getRecoveryManager();
            boolean isRecoveryScanStarted = recoveryManager.isSubordinateCoordinatorRecoveryStarted();
            // now look for a subordinate coordinator with the right id
            coordinator = SubordinateCoordinator.getRecoveredCoordinator(coordinatorId);
            if (coordinator == null) {
                if (!isRecoveryScanStarted) {
                    // the subtransaction may still be waiting to be resolved
                    // throw an exception causing the rollback to be retried later
                    throw new SystemException();
                }
            } else if(!coordinator.isActivated()) {
                // the transaction was logged but has not yet been recovered successfully
                // throw an exception causing the rollback to be retried later
                    throw new SystemException();
            } else {
                int status = coordinator.status();

                if ((status ==  ActionStatus.ABORTED) ||
                        (status == ActionStatus.H_ROLLBACK) ||
                        (status == ActionStatus.ABORTING) ||
                        (status == ActionStatus.ABORT_ONLY)) {
                    // ok, the rollback process was not previously initiated so start it now
                    coordinator.rollback();
                    status = coordinator.status();
                }
            }
        }
    }

    /**
     * this should never get called
     * @throws com.arjuna.wst.SystemException
     */
    public void unknown() throws SystemException {
        coordinator.unknown();
    }

    /**
     * this should never get called
     * @throws com.arjuna.wst.SystemException
     */
    public void error() throws SystemException {
        coordinator.error();
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
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * test if this participant is recovered
     */
    public boolean isRecovered()
    {
        return recovered;
    }

    /**
     * the interposed coordinator
     */
    private SubordinateCoordinator coordinator;

    /**
     * the interposed coordinator's id
     */
    private String coordinatorId;

    /**
     * a flag indicating whether this participant has been recovered
     */

    private boolean recovered;

    /**
     * this participant implements the PersistableATarticipant interface so it can save its state.
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
}