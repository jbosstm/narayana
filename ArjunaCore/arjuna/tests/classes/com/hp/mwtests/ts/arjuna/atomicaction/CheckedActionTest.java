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
package com.hp.mwtests.ts.arjuna.atomicaction;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.coordinator.CheckedAction;
import com.arjuna.ats.arjuna.coordinator.CheckedActionFactory;



public class CheckedActionTest
{
    protected boolean called;

	@Test
    public void test()
    {
        arjPropertyManager.getCoordinatorEnvironmentBean().setCheckedActionFactoryClassName(DummyCheckedAction.class.getName());
        DummyCheckedAction.reset();
        assertFalse(DummyCheckedAction.factoryCalled());
        assertFalse(DummyCheckedAction.called());

        AtomicAction A = new AtomicAction();

        A.begin();

        A.commit();

        assertTrue(DummyCheckedAction.factoryCalled());
        assertFalse(DummyCheckedAction.called());
    }
    
    @Test
    public void testCheckedAction ()
    {
        arjPropertyManager.getCoordinatorEnvironmentBean().setCheckedActionFactoryClassName(DummyCheckedAction.class.getName());
        DummyCheckedAction.reset();
        assertFalse(DummyCheckedAction.factoryCalled());
        assertFalse(DummyCheckedAction.called());

        AtomicAction A = new AtomicAction();
        
        A.begin();
        
        /*
         * CheckedAction only called if there are multiple
         * threads active in the transaction. Simulate this.
         */
        
        A.addChildThread(new Thread());

        A.commit();

        assertTrue(DummyCheckedAction.factoryCalled());
        assertTrue(DummyCheckedAction.called());
    }
    

	private int factory1Called = 0;
	private int factory2Called = 0;
	private int factory3Called = 0;

	@Test
	public void testCanChangeCheckedActionFactory() {
		{
			arjPropertyManager.getCoordinatorEnvironmentBean()
					.setCheckedActionFactory(new CheckedActionFactory() {
						@Override
						public CheckedAction getCheckedAction(Uid txId,
								String actionType) {
							factory1Called++;
							return null;
						}
					});
			AtomicAction A = new AtomicAction();
			A.begin();
			A.commit();
		}

		{
			arjPropertyManager.getCoordinatorEnvironmentBean()
					.setCheckedActionFactory(new CheckedActionFactory() {
						@Override
						public CheckedAction getCheckedAction(Uid txId,
								String actionType) {
							factory2Called++;
							return null;
						}
					});
			AtomicAction A = new AtomicAction();
			A.begin();
			A.commit();
		}
		arjPropertyManager.getCoordinatorEnvironmentBean()
				.setAllowCheckedActionFactoryOverride(true);
		{
			arjPropertyManager.getCoordinatorEnvironmentBean()
					.setCheckedActionFactory(new CheckedActionFactory() {
						@Override
						public CheckedAction getCheckedAction(Uid txId,
								String actionType) {
							factory3Called++;
							return null;
						}
					});
			AtomicAction A = new AtomicAction();
			A.begin();
			A.commit();
		}

		assertTrue(factory1Called == 2);
		assertTrue(factory2Called == 0);
		assertTrue(factory3Called == 1);

		arjPropertyManager.getCoordinatorEnvironmentBean()
				.setAllowCheckedActionFactoryOverride(false);

		{
			arjPropertyManager.getCoordinatorEnvironmentBean()
					.setCheckedActionFactory(new CheckedActionFactory() {
						@Override
						public CheckedAction getCheckedAction(Uid txId,
								String actionType) {
							factory2Called++;
							return null;
						}
					});
			AtomicAction A = new AtomicAction();
			A.begin();
			A.commit();
		}
		assertTrue(factory1Called == 3);
		assertTrue(factory2Called == 0);

		arjPropertyManager.getCoordinatorEnvironmentBean()
				.setAllowCheckedActionFactoryOverride(true);

		{
			arjPropertyManager.getCoordinatorEnvironmentBean()
					.setCheckedActionFactory(new CheckedActionFactory() {
						@Override
						public CheckedAction getCheckedAction(Uid txId,
								String actionType) {
							factory2Called++;
							return null;
						}
					});
			AtomicAction A = new AtomicAction();
			A.begin();
			A.commit();
		}
		assertTrue(factory1Called == 3);
		assertTrue(factory2Called == 1);
	}
}
