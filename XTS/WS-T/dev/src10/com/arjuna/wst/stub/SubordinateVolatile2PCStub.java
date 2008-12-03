package com.arjuna.wst.stub;

import com.arjuna.wst.*;
import com.arjuna.mwlabs.wscf.model.twophase.arjunacore.subordinate.SubordinateCoordinator;

/**
 * A volatile participant registered on behalf of an interposed WS-AT coordinator in order to ensure that
 * volatile participants in the subtransaction are prepared at the right time.
 */
public class SubordinateVolatile2PCStub implements Volatile2PCParticipant
{
    public SubordinateVolatile2PCStub(SubordinateCoordinator coordinator)
    {
        this.coordinator = coordinator;
    }

    /**
     * This will be called when the parent coordinator is preparing its volatile participants and should ensure
     * that the interposed cooordinator does the same.
     *
     * @return the Vote returned by the subordinate coordinator.
     * @throws com.arjuna.wst.WrongStateException if the subordinate coordinator does the same
     * @throws com.arjuna.wst.SystemException if the subordinate coordinator does the same
     */
    public Vote prepare() throws WrongStateException, SystemException {
        if (coordinator.prepareVolatile()) {
            return new Prepared();
        } else {
            return new Aborted();
        }
    }

    /**
     * this is called as part of the after completion processing and should ensure that the interposed
     * coordinator performs its afterCompletion processing
     * @throws com.arjuna.wst.WrongStateException
     * @throws com.arjuna.wst.SystemException
     */

    public void commit() throws WrongStateException, SystemException {
        coordinator.commitVolatile();
    }

    /**
     * this is called as part of the after completion processing and should ensure that the interposed
     * coordinator performs its afterCompletion processing
     * @throws com.arjuna.wst.WrongStateException
     * @throws com.arjuna.wst.SystemException
     */

    public void rollback() throws WrongStateException, SystemException {
        coordinator.rollbackVolatile();
    }

    /**
     * this should never get called
     * @throws com.arjuna.wst.SystemException
     */
    public void unknown() throws SystemException {
    }

    /**
     * this should never get called
     * @throws com.arjuna.wst.SystemException
     */
    public void error() throws SystemException {
    }

    /**
     * the interposed coordinator
     */
    private SubordinateCoordinator coordinator;
}