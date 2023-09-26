package com.hp.mwtests.ts.jta.jts.xa;
/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



import static org.junit.Assert.assertTrue;

import java.io.File;

import jakarta.transaction.Synchronization;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.BAD_INV_ORDER;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.PropagationContext;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.internal.jta.resources.jts.orbspecific.ManagedSynchronizationImple;
import com.arjuna.ats.internal.jta.transaction.jts.TransactionImple;
import com.arjuna.ats.internal.jts.ControlWrapper;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.interposition.resources.arjuna.InterpositionCreator;
import com.arjuna.ats.internal.jts.orbspecific.ControlImple;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import com.arjuna.ats.internal.jts.orbspecific.coordinator.ArjunaTransactionImple;
import com.arjuna.ats.internal.jts.orbspecific.interposition.ServerControl;
import com.arjuna.ats.internal.jts.orbspecific.interposition.resources.arjuna.ServerTopLevelAction;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jts.common.jtsPropertyManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;

public class JTSInterpositionSynchronizationTest {
	private boolean originalValue;
	private ORB myORB = null;
	private RootOA myOA = null;
	protected boolean beforeCompletionCalled;
	protected boolean afterCompletionCalled;
	private boolean beforeCompletionCalledFirst;
	protected boolean prepareCalled;

	@Before
	public void setUp() throws Exception {
		emptyObjectStore();

		myORB = ORB.getInstance("test");
		myOA = OA.getRootOA(myORB);

		myORB.initORB(new String[] {}, null);
		myOA.initOA();

		ORBManager.setORB(myORB);
		ORBManager.setPOA(myOA);

		originalValue = jtsPropertyManager.getJTSEnvironmentBean()
				.isSupportInterposedSynchronization();
	}

	@After
	public void tearDown() throws Exception {
		myOA.destroy();
		myORB.shutdown();
		try {
			myORB.orb().shutdown(true);
		} catch (BAD_INV_ORDER bio) {
			// ignore - IDLJ will not tolerate the second call to shutdown
		}
		emptyObjectStore();
		jtsPropertyManager.getJTSEnvironmentBean()
				.setSupportInterposedSynchronization(originalValue);
	}

	private void emptyObjectStore() {
		String objectStoreDirName = arjPropertyManager
				.getObjectStoreEnvironmentBean().getObjectStoreDir();

		System.out.printf("Emptying %s\n", objectStoreDirName);

		File objectStoreDir = new File(objectStoreDirName);

		removeContents(objectStoreDir);
	}

	public void removeContents(File directory) {
		if ((directory != null) && directory.isDirectory()
				&& (!directory.getName().equals(""))
				&& (!directory.getName().equals("/"))
				&& (!directory.getName().equals("\\"))
				&& (!directory.getName().equals("."))
				&& (!directory.getName().equals(".."))) {
			File[] contents = directory.listFiles();

			for (int index = 0; index < contents.length; index++) {
				if (contents[index].isDirectory()) {
					removeContents(contents[index]);
					contents[index].delete();
				} else {
					contents[index].delete();
				}
			}
		}
	}

	@Test
	public void test() throws Exception {
		InterpositionCreator creator = new InterpositionCreator();

		jtaPropertyManager
				.getJTAEnvironmentBean()
				.setTransactionManagerClassName(
						com.arjuna.ats.internal.jta.transaction.jts.TransactionManagerImple.class
								.getName());
		jtaPropertyManager
				.getJTAEnvironmentBean()
				.setUserTransactionClassName(
						com.arjuna.ats.internal.jta.transaction.jts.UserTransactionImple.class
								.getName());

		jtsPropertyManager.getJTSEnvironmentBean()
				.setSupportInterposedSynchronization(true);

		jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
				.transactionManager();

		tm.setTransactionTimeout(1000000);
		tm.begin();
		TransactionImple transaction = (TransactionImple) tm.getTransaction();
		transaction.enlistResource(new XAResource() {

			@Override
			public int prepare(Xid arg0) throws XAException {
				prepareCalled = true;
				beforeCompletionCalledFirst = beforeCompletionCalled;
				return 0;
			}

			@Override
			public void commit(Xid arg0, boolean arg1) throws XAException {

			}

			@Override
			public void end(Xid arg0, int arg1) throws XAException {

			}

			@Override
			public void forget(Xid arg0) throws XAException {

			}

			@Override
			public int getTransactionTimeout() throws XAException {

				return 0;
			}

			@Override
			public boolean isSameRM(XAResource arg0) throws XAException {

				return false;
			}

			@Override
			public Xid[] recover(int arg0) throws XAException {

				return null;
			}

			@Override
			public void rollback(Xid arg0) throws XAException {

			}

			@Override
			public boolean setTransactionTimeout(int arg0) throws XAException {

				return false;
			}

			@Override
			public void start(Xid arg0, int arg1) throws XAException {

			}

		});

		ControlWrapper controlWrapper = transaction.getControlWrapper();
		Uid get_uid = transaction.get_uid();
		ControlImple cont = controlWrapper.getImple();
		ArjunaTransactionImple tx = cont.getImplHandle();

		CurrentImple current = OTSImpleManager.current();
		Control get_control = current.get_control();

		PropagationContext ctx = cont.get_coordinator().get_txcontext();

		ControlImple recreateLocal = creator.recreateLocal(ctx);
		assertTrue(recreateLocal != null);
		Control recreate = creator.recreate(ctx);
		assertTrue(recreate != null);

		Object remove = ControlImple.allControls.remove(get_uid);
		ServerControl sc = new ServerControl(get_uid, get_control, null,
				cont.get_coordinator(), cont.get_terminator());
		ControlImple.allControls.put(get_uid, remove);
		ServerTopLevelAction serverTopLevelAction = new ServerTopLevelAction(sc);

		sc.getImplHandle().register_synchronization(
				new ManagedSynchronizationImple(new Synchronization() {

					@Override
					public void beforeCompletion() {
						beforeCompletionCalled = true;
					}

					@Override
					public void afterCompletion(int status) {
						afterCompletionCalled = true;
					}
				}).getSynchronization());

		transaction.commit();

		assertTrue(prepareCalled == true);
		assertTrue(beforeCompletionCalled);
		assertTrue(afterCompletionCalled);
		assertTrue(beforeCompletionCalledFirst == jtsPropertyManager
				.getJTSEnvironmentBean().isSupportInterposedSynchronization());
	}
}