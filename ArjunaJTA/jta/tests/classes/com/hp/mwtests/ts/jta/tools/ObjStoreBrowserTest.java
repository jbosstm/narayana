package com.hp.mwtests.ts.jta.tools;

import java.util.*;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.tools.osb.mbean.*;
import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;
import com.arjuna.ats.internal.jta.tools.osb.mbean.jta.JTAActionBean;
import com.arjuna.ats.internal.jta.tools.osb.mbean.jta.XAResourceRecordBean;
import com.hp.mwtests.ts.jta.common.DummyXA;
import com.hp.mwtests.ts.jta.common.FailureXAResource;
import org.junit.BeforeClass;
import org.junit.Test;

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple;

import javax.transaction.HeuristicMixedException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import static org.junit.Assert.*;

class ExtendedFailureXAResource extends FailureXAResource {
	boolean forgotten;

	@Override
	public void commit(Xid id, boolean onePhase) throws XAException {
		if (!forgotten)
			super.commit(id, onePhase);
	}

	@Override
	public void forget(Xid xid) throws XAException {
		super.forget(xid);
		forgotten = true;
	}
}

public class ObjStoreBrowserTest {

	private ObjStoreBrowser createObjStoreBrowser() {
		ObjStoreBrowser osb = new ObjStoreBrowser();

		osb.setTypes( new HashMap<String, String>() {{
			put("StateManager/BasicAction/TwoPhaseCoordinator/ArjunaTransactionImple", "com.arjuna.ats.arjuna.tools.osb.mbean.ActionBean");
			put("StateManager/BasicAction/TwoPhaseCoordinator/AtomicAction", "com.arjuna.ats.internal.jta.tools.osb.mbean.jta.JTAActionBean");
		}});

		return osb;
	}

	@BeforeClass
	public static void setUp() {
	}

	@Test
	public void testXAResourceRecordBean() throws Exception {
		com.arjuna.common.tests.simple.EnvironmentBeanTest.testBeanByReflection(new XAResourceRecordBean(new UidWrapper(Uid.nullUid())));
	}

	@Test
	public void testMBeanHeuristic () throws Exception
	{
		ThreadActionData.purgeActions();
		ObjStoreBrowser osb = createObjStoreBrowser();
		XAResource[] resources = {
				new DummyXA(false),
				new FailureXAResource(FailureXAResource.FailLocation.commit) // generates a heuristic on commit
		};

		TransactionImple tx = new TransactionImple(0);

		// enlist the XA resources into the transaction
		for (XAResource resource : resources) {

			tx.enlistResource(resource);
		}

		try {
			tx.commit();

			fail("Expected a mixed heuristic");
		} catch (final HeuristicMixedException ex) {
		}

		osb.start();
		// there should be one MBean corresponding to the Transaction
		UidWrapper w = osb.findUid(tx.get_uid());
		assertNotNull(w);
		OSEntryBean ai = w.getMBean();
		assertNotNull(ai);

		// the MBean should wrap a JTAActionBean
		assertTrue(ai instanceof JTAActionBean);
		JTAActionBean actionBean = (JTAActionBean) ai;

		// and the transaction should contain only one participant (namely the FailureXAResource that generated the heuristic):
		Collection<LogRecordWrapper> participants = actionBean.getParticipants();

		assertEquals(1, participants.size());

		for (LogRecordWrapper participant : participants) {
			assertTrue(participant.isHeuristic());
			// put the participant back onto the pending list
			participant.setStatus("PREPARED");
			// and check that the record is no longer in a heuristic state
			assertFalse(participant.isHeuristic());
		}

		osb.stop();
	}
}
