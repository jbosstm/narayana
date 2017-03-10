/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: JTATest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jta.jts.xa;
 
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.hp.mwtests.ts.jta.xa.JTATest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.ORBPackage.InvalidName;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

public class JTSTest {
    private ORB myORB;
    private RootOA myOA;

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
    }
    
    @After
    public void tearDown() {
        if (myOA != null) {
//            myOA.destroy();
//            myORB.shutdown();
        }
    }

    @Test
    public void testDuplicateXAREndCalled() throws javax.transaction.SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {
        javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        tm.begin();

        javax.transaction.Transaction theTransaction = tm.getTransaction();

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
    public void testDuplicateXAREndCalledFailure() throws javax.transaction.SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {
        javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        tm.begin();

        javax.transaction.Transaction theTransaction = tm.getTransaction();

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

        javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        tm.begin();

        javax.transaction.Transaction theTransaction = tm.getTransaction();

        assertTrue(theTransaction.enlistResource(theResource));

        try {
            tm.commit();
            fail();
        } catch (javax.transaction.HeuristicMixedException e) {
            // Expected
        }
    }
    
	@Test
	public void test() throws Exception {

		javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

		tm.begin();

		javax.transaction.Transaction theTransaction = tm.getTransaction();

		assertTrue(theTransaction.enlistResource(new XARMERRXAResource(false)));
		assertTrue(theTransaction.enlistResource(new XARMERRXAResource(true)));

		tm.rollback();
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
