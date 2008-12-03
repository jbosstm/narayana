package com.arjuna.wst.stub;

import com.arjuna.wst.*;
import com.arjuna.mwlabs.wscf.model.twophase.arjunacore.subordinate.SubordinateCoordinator;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;

/**
 * A durable participant registered on behalf of an interposed WS-AT coordinator in order to ensure that
 * durable participants in the subtransaction prepared, committed and aborted at the right time.
 */
public class SubordinateDurable2PCStub implements Durable2PCParticipant
{
    public SubordinateDurable2PCStub(SubordinateCoordinator coordinator)
    {
        this.coordinator = coordinator;
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
        coordinator.commit();
    }

    /**
     * this will be called when the parent coordinator rolls back its durable participants and should ensure
     * that the interposed cooordinator does the same
     * @throws com.arjuna.wst.WrongStateException
     * @throws com.arjuna.wst.SystemException
     */

    public void rollback() throws WrongStateException, SystemException {
        coordinator.rollback();
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
     * the interposed coordinator
     */
    private SubordinateCoordinator coordinator;
}