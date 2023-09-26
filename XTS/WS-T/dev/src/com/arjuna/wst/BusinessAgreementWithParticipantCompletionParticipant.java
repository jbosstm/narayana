/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.wst;

public interface BusinessAgreementWithParticipantCompletionParticipant
{

    /**
     * The transaction has completed successfully. The participant previously
     * informed the coordinator that it was ready to complete.
     */

    public void close () throws WrongStateException, SystemException;

    /**
     * The transaction has cancelled, and the participant should undo any work.
     * The participant cannot have informed the coordinator that it has
     * completed.
     */

    public void cancel () throws FaultedException, WrongStateException, SystemException;

    /**
     * The transaction has cancelled. The participant previously
     * informed the coordinator that it had finished work but could compensate
     * later if required, so it is now requested to do so.
     * @throws FaultedException if the participant was unable to
     * perform the required compensation action because of an
     * unrecoverable error. The coordinator is notified of this fault
     * and as a result will stop resending compensation requests.
     * @throws SystemException if the participant was unable to
     * perform the required compensation action because of a transient
     * fault. The coordinator is not notified of this fault so it
     * will retry the compensate request after a suitable timeout.
     */

    public void compensate () throws FaultedException, WrongStateException, SystemException;

    /**
     * @return the status value.
     */

    public String status () throws SystemException;

    /**
     * If the participant enquires as to the status of the transaction it was
     * registered with and that transaction is no longer available (has rolled
     * back) then this operation will be invoked by the coordination service.
     *
     * This has been deprecated since the correct action when a GetStatus request fails
     * is either to cancel or compensate the participant or to call error. GetStatus
     * is only dispatched while the participant is completed so cancel will never be
     * appropriate. compensate is called when the participant is unknown to the
     * coordinator (it responds to the GetStatus request with an InvalidState fault).
     * This will only happen if the coordinator crashed after the participant completed
     * but before the client requested a close/cancel. error is called if any other fault
     * response is received. So there is no other circumstance in which it would be
     * appropriate for unknown to be called.
     */

    @Deprecated
    public void unknown () throws SystemException;

    /**
     * If the participant enquired as to the status of the transaction it was
     * registered with and an unrecoverable error occurs then this operation will be
     * invoked.
     */

    public void error () throws SystemException;
}