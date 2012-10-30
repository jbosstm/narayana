package org.jboss.narayana.txframework.impl.handlers.wsba;

import com.arjuna.wst.BusinessAgreementWithParticipantCompletionParticipant;
import com.arjuna.wst.FaultedException;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst11.ConfirmCompletedParticipant;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Cancel;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Close;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Compensate;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.ConfirmCompleted;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Error;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Status;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Unknown;
import org.jboss.narayana.txframework.impl.Participant;
import org.jboss.narayana.txframework.impl.ServiceInvocationMeta;
import org.jboss.narayana.txframework.impl.handlers.ParticipantRegistrationException;

import java.io.Serializable;
import java.util.Map;

public class WSBAParticipantCompletionParticipant extends Participant implements BusinessAgreementWithParticipantCompletionParticipant,
        ConfirmCompletedParticipant, Serializable
{

    protected final WSBAParticipantRegistry participantRegistry = new WSBAParticipantRegistry();
    private String txid;

    public WSBAParticipantCompletionParticipant(ServiceInvocationMeta serviceInvocationMeta, Map txDataMap, String txid) throws ParticipantRegistrationException
    {
        super(serviceInvocationMeta, txDataMap);
        this.txid = txid;

        registerEventsOfInterest(Cancel.class, Close.class, Compensate.class, ConfirmCompleted.class, Error.class, Status.class, Unknown.class);
    }

    public void error() throws SystemException
    {
        invoke(org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Error.class);
    }

    public void close() throws WrongStateException, SystemException
    {
        invoke(Close.class);
        participantRegistry.forget(txid);
    }

    public void cancel() throws FaultedException, WrongStateException, SystemException
    {
        invoke(Cancel.class);
        participantRegistry.forget(txid);
    }

    public void compensate() throws FaultedException, WrongStateException, SystemException
    {
        invoke(Compensate.class);
        participantRegistry.forget(txid);
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
