package org.jboss.jbossts.xts.servicetests.service.participant;

import com.arjuna.wst.*;

import java.io.Serializable;

/**
 * A scriptable participant completion participant for use by the XTSServiceTest service.
 */

@SuppressWarnings("serial")
public class ParticipantCompletionTestParticipant
        extends ScriptedTestParticipant
        implements BusinessAgreementWithParticipantCompletionParticipant, Serializable
{
    // constructor for recovery only
    protected ParticipantCompletionTestParticipant()
    {
    }

    public ParticipantCompletionTestParticipant(String id)
    {
        super(id);
    }

    public void addCommand(String command)
    {
        commands.add(command);
    }

    public void close() throws WrongStateException, SystemException {
        for (String s : commands) {
            if (s.equals("close")) {
                commands.remove(s);
                return;
            } else if (s.equals("closeWrongStateException")) {
                commands.remove(s);
                throw new WrongStateException("ParticipantCompletionTestParticipant close : " + id);
            } else if (s.equals("closeSystemException")) {
                commands.remove(s);
                throw new SystemException("ParticipantCompletionTestParticipant close : " + id);
            }
        }

        // default behaviour is just to complete

        return;
    }

    public void cancel() throws FaultedException, WrongStateException, SystemException {
        for (String s : commands) {
            if (s.equals("cancel")) {
                commands.remove(s);
                return;
            } else if (s.equals("cancelFaultedException")) {
                commands.remove(s);
                throw new FaultedException("ParticipantCompletionTestParticipant cancel : " + id);
            } else if (s.equals("cancelWrongStateException")) {
                commands.remove(s);
                throw new WrongStateException("ParticipantCompletionTestParticipant cancel : " + id);
            } else if (s.equals("cancelSystemException")) {
                commands.remove(s);
                throw new SystemException("ParticipantCompletionTestParticipant cancel : " + id);
            }
        }

        // default behaviour is just to complete

        return;
    }

    public void compensate() throws FaultedException, WrongStateException, SystemException {
        for (String s : commands) {
            if (s.equals("compensate")) {
                commands.remove(s);
                return;
            } else if (s.equals("compensateFaultedException")) {
                commands.remove(s);
                throw new FaultedException("ParticipantCompletionTestParticipant compensate : " + id);
            } else if (s.equals("compensateWrongStateException")) {
                commands.remove(s);
                throw new WrongStateException("ParticipantCompletionTestParticipant compensate : " + id);
            } else if (s.equals("compensateSystemException")) {
                commands.remove(s);
                throw new SystemException("ParticipantCompletionTestParticipant compensate : " + id);
            }
        }

        // default behaviour is just to complete

        return;
    }

    public String status() throws SystemException {
        return null;
    }

    public void unknown() throws SystemException {
        // do nothing for now
    }

    public void error() throws SystemException {
        // do nothing for now
    }
}