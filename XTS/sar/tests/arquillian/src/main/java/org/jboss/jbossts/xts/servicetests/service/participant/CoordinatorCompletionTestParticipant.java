package org.jboss.jbossts.xts.servicetests.service.participant;

import com.arjuna.wst.*;

import java.io.Serializable;

/**
 * A scriptable coordinator completion participant for use by the XTSServiceTest service.
 */

@SuppressWarnings("serial")
public class CoordinatorCompletionTestParticipant extends ParticipantCompletionTestParticipant
        implements BusinessAgreementWithCoordinatorCompletionParticipant, Serializable
{
    /**
	 * 
	 */

	// constructor for recovery only
    protected CoordinatorCompletionTestParticipant()
    {
    }

    public CoordinatorCompletionTestParticipant(String id)
    {
        super(id);
    }

    public void complete() throws WrongStateException, SystemException {
        for (String s : commands) {
            if (s.equals("complete")) {
                commands.remove(s);
                return;
            } else if (s.equals("completeWrongStateException")) {
                commands.remove(s);
                throw new WrongStateException("CoordinatorCompletionTestParticipant complete : " + id);
            } else if (s.equals("completeSystemException")) {
                commands.remove(s);
                throw new SystemException("CoordinatorCompletionTestParticipant complete : " + id);
            }
        }

        // default behaviour is just to complete

        return;
    }
}