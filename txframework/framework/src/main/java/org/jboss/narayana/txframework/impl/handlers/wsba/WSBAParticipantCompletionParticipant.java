package org.jboss.narayana.txframework.impl.handlers.wsba;

import com.arjuna.wst.BusinessAgreementWithParticipantCompletionParticipant;
import com.arjuna.wst.FaultedException;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst11.ConfirmCompletedParticipant;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Error;
import org.jboss.narayana.txframework.api.exception.TXFrameworkException;
import org.jboss.narayana.txframework.impl.Participant;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.*;
import org.jboss.narayana.txframework.impl.handlers.ParticipantRegistrationException;

import java.io.Serializable;

public class WSBAParticipantCompletionParticipant extends Participant implements BusinessAgreementWithParticipantCompletionParticipant,
        ConfirmCompletedParticipant, Serializable
{
    public WSBAParticipantCompletionParticipant(Object serviceImpl, boolean injectDataManagement) throws ParticipantRegistrationException
    {
        super(serviceImpl, injectDataManagement);

        registerEventsOfInterest(Cancel.class, Close.class, Compensate.class, ConfirmCompleted.class, Error.class, Status.class, Unknown.class);
    }

    public void error() throws SystemException
    {
        invoke(org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Error.class);
    }

    public void close() throws WrongStateException, SystemException
    {
        invoke(Close.class);
    }

    public void cancel() throws FaultedException, WrongStateException, SystemException
    {
        invoke(Cancel.class);
    }

    public void compensate() throws FaultedException, WrongStateException, SystemException
    {
        invoke(Compensate.class);
    }

    public String status() throws SystemException
    {
        //todo: return a default status
        //todo: check impl returns a String
        return (String) invoke(Status.class);
    }

    @Deprecated
    public void unknown() throws SystemException
    {
        invoke(Unknown.class);
    }

    public void confirmCompleted(boolean completed)
    {
        invoke(ConfirmCompleted.class, completed);
    }
}
