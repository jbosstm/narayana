/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.jts.xa;
 
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.byteman.contrib.bmunit.BMRule;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.ORBPackage.InvalidName;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(BMUnitRunner.class)
public class JTSTest {
    private ORB myORB;
    private RootOA myOA;
    private boolean resource1Rollback;
    private boolean resource2Rollback;

    @Before
    public void setup() throws InvalidName, SystemException {
        System.setProperty("OrbPortabilityEnvironmentBean.orbImpleClassName", System.getProperty("OrbPortabilityEnvironmentBean.orbImpleClassName", "com.arjuna.orbportability.internal.orbspecific.javaidl.orb.implementations.javaidl_1_4"));
        System.setProperty("OrbPortabilityEnvironmentBean.poaImpleClassName", System.getProperty("OrbPortabilityEnvironmentBean.poaImpleClassName", "com.arjuna.orbportability.internal.orbspecific.javaidl.oa.implementations.javaidl_1_4"));
        System.setProperty("OrbPortabilityEnvironmentBean.orbDataClassName", System.getProperty("OrbPortabilityEnvironmentBean.orbDataClassName", "com.arjuna.orbportability.internal.orbspecific.versions.javaidl_1_4"));
        myORB = ORB.getInstance("test");
        myOA = OA.getRootOA(myORB);
        myORB.initORB(new String[] {}, null);
        myOA.initOA();

        ORBManager.setORB(myORB);
        ORBManager.setPOA(myOA);
        jtaPropertyManager.getJTAEnvironmentBean().setTransactionManagerClassName(com.arjuna.ats.internal.jta.transaction.jts.TransactionManagerImple.class.getName());
        jtaPropertyManager.getJTAEnvironmentBean().setUserTransactionClassName(com.arjuna.ats.internal.jta.transaction.jts.UserTransactionImple.class.getName());

        resource1Rollback = false;
        resource2Rollback = false;
    }
    
    @After
    public void tearDown() {
        if (myOA != null) {
//            myOA.destroy();
//            myORB.shutdown();
        }
    }

    @Test
    public void testDuplicateXAREndCalled() throws jakarta.transaction.SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {
        jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        tm.begin();

        jakarta.transaction.Transaction theTransaction = tm.getTransaction();

        AtomicBoolean endCalled = new AtomicBoolean(false);

        assertTrue(theTransaction.enlistResource(new SimpleXAResource() {
            public int id = 1;
            @Override
            public boolean isSameRM(XAResource xares) throws XAException {
                try {
                    Class<? extends XAResource> aClass = xares.getClass();
                    Field field = aClass.getField("id");
                    int other = field.getInt(xares);
                    if (other == 1) {
                        return true;
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    fail("Unexpected XAR");
                }
                return false;
            }

            @Override
            public void start(Xid xid, int flags) throws XAException {
                super.start(xid, flags);
            }

            @Override
            public void end(Xid xid, int flags) throws XAException {
                super.end(xid, flags);
            }
        }));
        assertTrue(theTransaction.enlistResource(new SimpleXAResource() {
            public int id = 1;
            @Override
            public boolean isSameRM(XAResource xares) throws XAException {
                try {
                    Field field = xares.getClass().getField("id");
                    int other = field.getInt(xares);
                    if (other == 1) {
                        return true;
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    fail("Unexpected XAR");
                }
                return false;
            }

            @Override
            public void start(Xid xid, int flags) throws XAException {
                super.start(xid, flags);
            }

            @Override
            public void end(Xid xid, int flags) throws XAException {
                endCalled.set(true);
                super.end(xid, flags);
            }
        }));
        assertTrue(theTransaction.enlistResource(new SimpleXAResource() {
            public int id = 2;
            @Override
            public boolean isSameRM(XAResource xares) throws XAException {
                return false;
            }
        }));
        tm.commit();

        assertTrue(endCalled.get());
    }

    @Test
    public void testDuplicateXAREndCalledFailure() throws jakarta.transaction.SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {
        jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        tm.begin();

        jakarta.transaction.Transaction theTransaction = tm.getTransaction();

        assertTrue(theTransaction.enlistResource(new SimpleXAResource() {
            @Override
            public boolean isSameRM(XAResource xares) throws XAException {
                return true;
            }

            @Override
            public void end(Xid xid, int flags) throws XAException {
                super.end(xid, flags);
            }
        }));
        assertTrue(theTransaction.enlistResource(new SimpleXAResource() {
            @Override
            public boolean isSameRM(XAResource xares) throws XAException {
                return true;
            }

            @Override
            public void end(Xid xid, int flags) throws XAException {
                throw new XAException(XAException.XA_RBROLLBACK);
            }
        }));
        try {
            tm.commit();
            fail("Committed");
        } catch (RollbackException e) {
        }
    }

    @Test
    public void testRMFAILcommit1PC() throws Exception
    {
        XAResource theResource = new XAResource() {

            @Override
            public void start(Xid xid, int flags) throws XAException {
            }

            @Override
            public void end(Xid xid, int flags) throws XAException {
            }

            @Override
            public int prepare(Xid xid) throws XAException {
                return 0;
            }

            @Override
            public void commit(Xid xid, boolean onePhase) throws XAException {
                throw new XAException(XAException.XAER_RMFAIL);
            }

            @Override
            public void rollback(Xid xid) throws XAException {
            }

            @Override
            public void forget(Xid xid) throws XAException {
            }

            @Override
            public Xid[] recover(int flag) throws XAException {
                return null;
            }

            @Override
            public boolean isSameRM(XAResource xaRes) throws XAException {
                return false;
            }

            @Override
            public int getTransactionTimeout() throws XAException {
                return 0;
            }

            @Override
            public boolean setTransactionTimeout(int seconds) throws XAException {
                return false;
            }
        };

        jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        tm.begin();

        jakarta.transaction.Transaction theTransaction = tm.getTransaction();

        assertTrue(theTransaction.enlistResource(theResource));

        try {
            tm.commit();
            fail();
        } catch (jakarta.transaction.HeuristicMixedException e) {
            // Expected
        }
    }
    
	@Test
	public void test() throws Exception {

		jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

		tm.begin();

		jakarta.transaction.Transaction theTransaction = tm.getTransaction();

		assertTrue(theTransaction.enlistResource(new XARMERRXAResource(false)));
		assertTrue(theTransaction.enlistResource(new XARMERRXAResource(true)));

		tm.rollback();
	}

    @Test
    public void testPrepareThrowsXaRbIntegrity() throws Exception {
        jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
                .transactionManager();

        tm.begin();

        jakarta.transaction.Transaction theTransaction = tm.getTransaction();

        assertTrue(theTransaction.enlistResource(new SimpleXAResource() {
            @Override
            public int prepare(Xid xid) throws XAException {
                throw new XAException(XAException.XA_RBINTEGRITY);
            }

            @Override
            public void rollback(Xid xid) throws XAException {
                resource1Rollback = true;
            }
        }));

        assertTrue(theTransaction.enlistResource(new SimpleXAResource() {

            @Override
            public void rollback(Xid xid) throws XAException {
                resource2Rollback = true;
            }
        }));

        try {
            tm.commit();
            fail("Should not have committed");
        } catch (RollbackException e) {
            // This is going to pass because of JBTM-3843
            assertFalse(resource1Rollback);
            assertTrue(resource2Rollback);
        }
    }

    @Test
    public void testRollbackCalledWhenEndHasXA_RBINTEGRITY() throws Exception {
        jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
                .transactionManager();

        tm.begin();

        jakarta.transaction.Transaction theTransaction = tm.getTransaction();

        assertTrue(theTransaction.enlistResource(new SimpleXAResource(){}));

        assertTrue(theTransaction.enlistResource(new SimpleXAResource() {
            @Override
            public void end(Xid xid, int flags) throws XAException {
                assertTrue(flags == XAResource.TMSUCCESS);
                throw new XAException(XAException.XA_RBINTEGRITY);
            }

            @Override
            public void rollback(Xid xid) throws XAException {
                resource1Rollback = true;
                throw new XAException(XAException.XAER_NOTA);
            }
        }));

        try {
            tm.commit();
            fail("Should not have committed");
        } catch (RollbackException e) {
            assertTrue(resource1Rollback);
            // Suppressed exceptions are not supported for the JTS mode of Narayana so can't check for the exception
            // from the SimpleXAReesource end call
        }
    }

    @Test
    @BMRule(name = "Fail if logging statement executes",
            targetClass = "com.arjuna.ats.internal.jta.utils.jtaxI18NLogger_$logger",
            targetMethod = "warn_could_not_end_xar",
            targetLocation = "AT ENTRY",
            action = "throw new java.lang.Error(\"JBTM-3345 not solved\")")
    public void testJBTM3345() throws Exception {
        jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
                .transactionManager();

        tm.begin();

        jakarta.transaction.Transaction theTransaction = tm.getTransaction();

        assertTrue(theTransaction.enlistResource(new SimpleXAResource() {
            @Override
            public int prepare(Xid xid) throws XAException {
                throw new XAException(XAException.XA_RBINTEGRITY);
            }

            @Override
            public void rollback(Xid xid) throws XAException {
                resource1Rollback = true;
            }
        }));

        assertTrue(theTransaction.enlistResource(new SimpleXAResource() {
            @Override
            public void end(Xid xid, int flags) throws XAException {
                assertTrue(flags == XAResource.TMFAIL);
                throw new XAException(XAException.XA_RBROLLBACK);
            }

            @Override
            public void rollback(Xid xid) throws XAException {
                resource2Rollback = true;
                throw new XAException(XAException.XAER_NOTA);
            }
        }));

        try {
            tm.commit();
            fail("Should not have committed");
        } catch (RollbackException e) {
            // This is going to pass because of JBTM-3843
            assertFalse(resource1Rollback);
            assertTrue(resource2Rollback);
        }
    }

    @Test
    public void testXAEndTMFAILXARBHandling () throws jakarta.transaction.SystemException, NotSupportedException, RollbackException, HeuristicRollbackException, HeuristicMixedException {
        jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
                .transactionManager();

        tm.begin();

        jakarta.transaction.Transaction theTransaction = tm.getTransaction();

        XAResource xar = new SimpleXAResource() {
            @Override
            public void end(Xid xid, int flags) throws XAException {
                assertTrue(flags == XAResource.TMFAIL);
                throw new XAException(XAException.XA_RBTRANSIENT);
            }

            @Override
            public void rollback(Xid xid) throws XAException {
                throw new XAException(XAException.XAER_NOTA);
            }
        };

        assertTrue(theTransaction.enlistResource(xar));

        tm.setRollbackOnly();
        assertTrue(theTransaction.delistResource(xar, XAResource.TMFAIL));

        try {
            tm.commit();
            fail("Should not have committed");
        } catch (RollbackException e) {
            // This is expected
        }
    }

    @Test
    public void testXAEndTMFAILXARBTransientHandling () throws jakarta.transaction.SystemException, NotSupportedException, RollbackException, HeuristicRollbackException, HeuristicMixedException {
        jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
                .transactionManager();

        tm.begin();

        jakarta.transaction.Transaction theTransaction = tm.getTransaction();

        assertTrue(theTransaction.enlistResource(new SimpleXAResource() {
            @Override
            public void end(Xid xid, int flags) throws XAException {
                throw new XAException(XAException.XA_RBTRANSIENT);
            }

            @Override
            public void rollback(Xid xid) throws XAException {
                resource1Rollback = true;
                throw new XAException(XAException.XAER_NOTA);
            }
        }));

        tm.setRollbackOnly();

        try {
            tm.commit();
            fail("Should not have committed");
        } catch (RollbackException e) {
            assertTrue(resource1Rollback);
        }
    }

    @Test
    public void testOnePhaseCommitWithXA_RBROLLBACK () throws jakarta.transaction.SystemException, NotSupportedException, RollbackException, HeuristicRollbackException, HeuristicMixedException {
        jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
                .transactionManager();

        tm.begin();

        jakarta.transaction.Transaction theTransaction = tm.getTransaction();

        assertTrue(theTransaction.enlistResource(new SimpleXAResource() {
            @Override
            public void end(Xid xid, int flags) throws XAException {
                // This should be invoked on the path to successfully commit the branch
                assertEquals(flags, TMSUCCESS);
                throw new XAException(XAException.XA_RBROLLBACK);
            }

            @Override
            public void rollback(Xid xid) throws XAException {
                resource1Rollback = true;
                throw new XAException(XAException.XAER_NOTA);
            }
        }));

        try {
            tm.commit();
            fail("Should not have committed");
        } catch (RollbackException e) {
            assertTrue(resource1Rollback);
        } catch (HeuristicMixedException heuristicMixedException) {
            fail("HeuristicMixedException shouldn't be thrown!");
        }
    }

	private class XARMERRXAResource implements XAResource {

		private boolean returnRMERROutOfEnd;

		public XARMERRXAResource(boolean returnRMERROutOfEnd) {
			this.returnRMERROutOfEnd = returnRMERROutOfEnd;
		}

		@Override
		public void commit(Xid xid, boolean onePhase) throws XAException {
			// TODO Auto-generated method stub

		}

		@Override
		public void end(Xid xid, int flags) throws XAException {
			if (returnRMERROutOfEnd) {
				throw new XAException(XAException.XAER_RMERR);
			}
		}

		@Override
		public void forget(Xid xid) throws XAException {
			// TODO Auto-generated method stub

		}

		@Override
		public int getTransactionTimeout() throws XAException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public boolean isSameRM(XAResource xares) throws XAException {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public int prepare(Xid xid) throws XAException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Xid[] recover(int flag) throws XAException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void rollback(Xid xid) throws XAException {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean setTransactionTimeout(int seconds) throws XAException {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void start(Xid xid, int flags) throws XAException {
			// TODO Auto-generated method stub

		}

	}

    private abstract class SimpleXAResource implements XAResource {

        @Override
        public void start(Xid xid, int flags) throws XAException {

        }

        @Override
        public boolean setTransactionTimeout(int seconds) throws XAException {

            return false;
        }

        @Override
        public void rollback(Xid xid) throws XAException {
        }

        @Override
        public Xid[] recover(int flag) throws XAException {

            return null;
        }

        @Override
        public int prepare(Xid xid) throws XAException {
            return XAResource.XA_OK;
        }

        @Override
        public boolean isSameRM(XAResource xares) throws XAException {

            return false;
        }

        @Override
        public int getTransactionTimeout() throws XAException {

            return 0;
        }

        @Override
        public void forget(Xid xid) throws XAException {

        }

        @Override
        public void end(Xid xid, int flags) throws XAException {

        }

        @Override
        public void commit(Xid xid, boolean onePhase) throws XAException {

        }
    }
}