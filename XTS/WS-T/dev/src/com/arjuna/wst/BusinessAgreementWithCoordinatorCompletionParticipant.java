/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.wst;

public interface BusinessAgreementWithCoordinatorCompletionParticipant extends BusinessAgreementWithParticipantCompletionParticipant
{

    /**
     * The coordinator is informing the participant that all work it needs to
     * do within the scope of this business activity has been received.
     */

    public void complete () throws WrongStateException, SystemException;
    
}