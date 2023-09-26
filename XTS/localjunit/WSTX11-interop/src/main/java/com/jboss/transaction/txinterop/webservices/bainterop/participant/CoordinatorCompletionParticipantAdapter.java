/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.jboss.transaction.txinterop.webservices.bainterop.participant;

import com.arjuna.wst.BusinessAgreementWithCoordinatorCompletionParticipant;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.WrongStateException;

class CoordinatorCompletionParticipantAdapter extends ParticipantCompletionParticipantAdapter implements BusinessAgreementWithCoordinatorCompletionParticipant
{
    public void complete()
        throws WrongStateException, SystemException
    {
    }
}