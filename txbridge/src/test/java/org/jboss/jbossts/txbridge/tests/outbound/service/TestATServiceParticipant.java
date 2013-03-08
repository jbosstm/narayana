package org.jboss.jbossts.txbridge.tests.outbound.service;

import java.io.Serializable;
import java.util.ArrayList;

import com.arjuna.wst.Durable2PCParticipant;
import com.arjuna.wst.Prepared;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.Vote;
import com.arjuna.wst.WrongStateException;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public final class TestATServiceParticipant implements Durable2PCParticipant, Serializable {

    private static final long serialVersionUID = 1L;

    private static volatile ArrayList<String> twoPhaseCommitInvocations = new ArrayList<String>();

    public static ArrayList<String> getTwoPhaseCommitInvocations() {
        return twoPhaseCommitInvocations;
    }

    public static void resetTwoPhaseCommitInvocations() {
        twoPhaseCommitInvocations.clear();
    }

    @Override
    public Vote prepare() throws WrongStateException, SystemException {
        twoPhaseCommitInvocations.add("prepare");
        return new Prepared();
    }

    @Override
    public void commit() throws WrongStateException, SystemException {
        twoPhaseCommitInvocations.add("commit");
    }

    @Override
    public void rollback() throws WrongStateException, SystemException {
        twoPhaseCommitInvocations.add("rollback");
    }

    @Override
    public void unknown() throws SystemException {
        twoPhaseCommitInvocations.add("unknown");
    }

    @Override
    public void error() throws SystemException {
        twoPhaseCommitInvocations.add("error");
    }

}
