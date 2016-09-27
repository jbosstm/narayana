package com.hp.mwtests.ts.jta.tools;


import com.arjuna.ats.arjuna.common.Uid;

import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.internal.jta.resources.arjunacore.XAOnePhaseResource;
import com.arjuna.ats.jta.xa.XidImple;
import com.hp.mwtests.ts.jta.common.SampleOnePhaseResource;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ForgetTest {
    @Test
    public void testCommitHeuristic ()
    {
        SampleOnePhaseResource res = new SampleOnePhaseResource(SampleOnePhaseResource.ErrorType.heurcom);
        XidImple xid = new XidImple(new Uid());
        XAOnePhaseResource xares = new XAOnePhaseResource(res, xid, null);

        assertEquals(xares.commit(), TwoPhaseOutcome.FINISH_OK);
        assertTrue(res.forgetCalled());
    }

    @Test
    public void testRollbackHeuristic ()
    {
        SampleOnePhaseResource res = new SampleOnePhaseResource(SampleOnePhaseResource.ErrorType.heurrb);
        XidImple xid = new XidImple(new Uid());
        XAOnePhaseResource xares = new XAOnePhaseResource(res, xid, null);

        assertEquals(xares.commit(), TwoPhaseOutcome.ONE_PHASE_ERROR);
        assertTrue(res.forgetCalled());
    }
}
