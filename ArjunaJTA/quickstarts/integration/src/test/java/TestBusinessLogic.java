/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2011,
 * @author JBoss, by Red Hat.
 */
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.narayana.quickstarts.ejb.Customer;
import org.jboss.narayana.quickstarts.ejb.SimpleEJB;
import org.jboss.narayana.quickstarts.ejb.SimpleEJBImpl;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.ObjectModel;
import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.txoj.Lock;
import com.arjuna.ats.txoj.LockManager;
import com.arjuna.ats.txoj.LockMode;
import com.arjuna.ats.txoj.LockResult;

@RunWith(Arquillian.class)
public class TestBusinessLogic {
	@EJB(lookup = "java:module/SimpleEJBImpl")
	private SimpleEJB simpleEJB;

	@Deployment
	public static WebArchive createDeployment() {
		WebArchive archive = ShrinkWrap
				.create(WebArchive.class, "test.war")
				.addClasses(SimpleEJB.class, SimpleEJBImpl.class,
						Customer.class)
				.addClasses(AtomicObject.class)
				.addAsResource("META-INF/persistence.xml",
						"META-INF/persistence.xml")
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
		archive.delete(ArchivePaths.create("META-INF/MANIFEST.MF"));

		// Need to make sure we add the arquillian-service and msc as a
		// dependency
		final String ManifestMF = "Manifest-Version: 1.0\n"
				+ "Dependencies: org.jboss.modules,deployment.arquillian-service,org.jboss.msc,org.jboss.jts\n";
		archive.setManifest(new StringAsset(ManifestMF));

		return archive;
	}

	@Test
	public void checkThatDoubleCallIncreasesListSize() throws NamingException,
			NotSupportedException, SystemException, SecurityException,
			IllegalStateException, RollbackException, HeuristicMixedException,
			HeuristicRollbackException {
		UserTransaction tx = (UserTransaction) new InitialContext()
				.lookup("java:comp/UserTransaction");
		tx.begin();
		simpleEJB.createCustomer("tom");
		tx.commit();

		String firstList = simpleEJB.listIds();

		tx = (UserTransaction) new InitialContext()
				.lookup("java:comp/UserTransaction");
		tx.begin();
		simpleEJB.createCustomer("tom");
		tx.commit();

		String secondList = simpleEJB.listIds();

		System.out.println(firstList);
		System.out.println(secondList);

		assertTrue(firstList.length() < secondList.length());
	}

	@Test
	public void testTxoj() throws Exception {
		UserTransaction tx = (UserTransaction) new InitialContext()
				.lookup("java:comp/UserTransaction");

		AtomicObject foo = new AtomicObject();
		Uid u = foo.get_uid();

		tx.begin();

		foo.set(2);

		tx.commit();

		int finalVal = foo.get();

		assertEquals(2, finalVal);

		foo = new AtomicObject(u);

		tx.begin();

		foo.set(4);

		tx.commit();

		finalVal = foo.get();

		assertEquals(4, finalVal);

		foo = new AtomicObject(u);

		finalVal = foo.get();

		assertEquals(4, finalVal);

		tx.begin();

		foo.set(10);

		tx.rollback();

		finalVal = foo.get();

		assertEquals(4, finalVal);
	}

	public class AtomicObject extends LockManager {

		private int state;

		private boolean printDebug;
		private int retry = 0;

		public AtomicObject() {
			this(ObjectModel.SINGLE);
		}

		public AtomicObject(int om) {
			super(ObjectType.ANDPERSISTENT, om);

			state = 0;

			AtomicAction A = new AtomicAction();

			A.begin();

			if (setlock(new Lock(LockMode.WRITE), 0) == LockResult.GRANTED) {
				if (A.commit() == ActionStatus.COMMITTED)
					System.out
							.println("Created persistent object " + get_uid());
				else
					System.out.println("Action.commit error.");
			} else {
				A.abort();

				System.out.println("setlock error.");
			}

			String debug = System.getProperty("DEBUG", null);

			if (debug != null)
				printDebug = true;
		}

		public AtomicObject(Uid u) {
			this(u, ObjectModel.SINGLE);
		}

		public AtomicObject(Uid u, int om) {
			super(u, ObjectType.ANDPERSISTENT, om);

			state = -1;

			AtomicAction A = new AtomicAction();

			A.begin();

			if (setlock(new Lock(LockMode.READ), 0) == LockResult.GRANTED) {
				System.out.println("Recreated object " + u);
				A.commit();
			} else {
				System.out.println("Error recreating object " + u);
				A.abort();
			}

			String debug = System.getProperty("DEBUG", null);

			if (debug != null)
				printDebug = true;
		}

		public int getRetry() {
			return retry;
		}

		public void setRetry(int t) {
			retry = t;
		}

		public void terminate() {
			super.terminate();
		}

		public void incr(int value) throws Exception {
			AtomicAction A = new AtomicAction();

			A.begin();

			if (setlock(new Lock(LockMode.WRITE), retry) == LockResult.GRANTED) {
				state += value;

				if (A.commit() != ActionStatus.COMMITTED)
					throw new Exception("Action commit error.");
				else
					return;
			} else {
				if (printDebug)
					System.out.println("Error - could not set write lock.");
			}

			A.abort();

			throw new Exception("Write lock error.");
		}

		public void set(int value) throws Exception {
			AtomicAction A = new AtomicAction();

			A.begin();

			if (setlock(new Lock(LockMode.WRITE), retry) == LockResult.GRANTED) {
				state = value;

				if (A.commit() != ActionStatus.COMMITTED)
					throw new Exception("Action commit error.");
				else
					return;
			} else {
				if (printDebug)
					System.out.println("Error - could not set write lock.");
			}

			A.abort();

			throw new Exception("Write lock error.");
		}

		public int get() throws Exception {
			AtomicAction A = new AtomicAction();
			int value = -1;

			A.begin();

			if (setlock(new Lock(LockMode.READ), retry) == LockResult.GRANTED) {
				value = state;

				if (A.commit() == ActionStatus.COMMITTED)
					return value;
				else
					throw new Exception("Action commit error.");
			} else {
				if (printDebug)
					System.out.println("Error - could not set read lock.");
			}

			A.abort();

			throw new Exception("Read lock error.");
		}

		public boolean save_state(OutputObjectState os, int ot) {
			boolean result = super.save_state(os, ot);

			if (!result)
				return false;

			try {
				os.packInt(state);
			} catch (IOException e) {
				result = false;
			}

			return result;
		}

		public boolean restore_state(InputObjectState os, int ot) {
			boolean result = super.restore_state(os, ot);

			if (!result)
				return false;

			try {
				state = os.unpackInt();
			} catch (IOException e) {
				result = false;
			}

			return result;
		}

		public String type() {
			return "/StateManager/LockManager/AtomicObject";
		}
	}

}