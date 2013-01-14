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

import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.internal.arjuna.abstractrecords.LastResourceRecord;
import com.hp.mwtests.ts.arjuna.resources.BasicRecord;
import com.hp.mwtests.ts.arjuna.resources.ShutdownRecord;
import com.hp.mwtests.ts.arjuna.resources.OnePhase;
import com.hp.mwtests.ts.arjuna.resources.LastResourceShutdownRecord;
import org.junit.Test;
import org.junit.Assert;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.arjPropertyManager;

public class AsyncUnitTest
{
    @Test
    public void testAsyncPrepare () throws Exception
    {
        arjPropertyManager.getCoordinatorEnvironmentBean().setAsyncPrepare(true);
        arjPropertyManager.getCoordinatorEnvironmentBean().setAsyncCommit(true);
        arjPropertyManager.getCoordinatorEnvironmentBean().setAsyncRollback(true);
        
        AtomicAction A = new AtomicAction();
        
        A.begin();
        
        A.add(new BasicRecord());
        A.add(new BasicRecord());
        
        A.commit(false);
    }

    @Test
    public void testAsyncPrepareWithLRRSuccess()
    {
        setupCoordinatorEnvironmentBean(true, false, false);

        OnePhase onePhase = new OnePhase();
        AbstractRecord lastResourceRecord = new LastResourceRecord(onePhase);
        AbstractRecord basicRecord = new BasicRecord();

        AtomicAction a = executeAsyncPrepareTest(true, basicRecord, lastResourceRecord);

        Assert.assertEquals(OnePhase.COMMITTED, onePhase.status());
        Assert.assertEquals(ActionStatus.COMMITTED, a.status());
    }

    @Test
    public void testAsyncPrepareWithLRRFailOn2PCAwareResourcePrepare()
    {
        setupCoordinatorEnvironmentBean(true, false, false);

        OnePhase onePhase = new OnePhase();
        AbstractRecord lastResourceRecord = new LastResourceRecord(onePhase);
        AbstractRecord shutdownRecord = new ShutdownRecord(ShutdownRecord.FAIL_IN_PREPARE);

        AtomicAction a = executeAsyncPrepareTest(true, shutdownRecord, lastResourceRecord);

        Assert.assertEquals(OnePhase.ROLLEDBACK, onePhase.status());
        Assert.assertEquals(ActionStatus.ABORTED, a.status());
    }

    @Test
    public void testAsyncPrepareWithLRRFailOn2PCUnawareResourcePrepare()
    {
        setupCoordinatorEnvironmentBean(true, false, false);

        OnePhase onePhase = new OnePhase();
        AbstractRecord lastResourceRecord = new LastResourceShutdownRecord(onePhase, true);
        AbstractRecord basicRecord = new BasicRecord();

        AtomicAction a = executeAsyncPrepareTest(true, lastResourceRecord, basicRecord);

        Assert.assertEquals(OnePhase.ROLLEDBACK, onePhase.status());
        Assert.assertEquals(ActionStatus.ABORTED, a.status());
    }

    @Test
    public void testAsyncPrepareWithLRRFailOn2PCAwareResourceCommit()
    {
        setupCoordinatorEnvironmentBean(true, false, false);

        OnePhase onePhase = new OnePhase();
        AbstractRecord lastResourceRecord = new LastResourceRecord(onePhase);
        AbstractRecord shutdownRecord = new ShutdownRecord(ShutdownRecord.FAIL_IN_COMMIT);

        executeAsyncPrepareTest(true, lastResourceRecord, shutdownRecord);

        Assert.assertEquals(OnePhase.COMMITTED, onePhase.status());
    }
    
    @Test
    public void testAsyncCommit () throws Exception
    {
        arjPropertyManager.getCoordinatorEnvironmentBean().setAsyncPrepare(true);
        arjPropertyManager.getCoordinatorEnvironmentBean().setAsyncCommit(true);
        arjPropertyManager.getCoordinatorEnvironmentBean().setAsyncRollback(true);
        
        AtomicAction A = new AtomicAction();
        
        A.begin();
        
        A.add(new BasicRecord());
        A.add(new BasicRecord());
        
        A.commit(false);
    }
    
    @Test
    public void testAsyncAbort () throws Exception
    {
        arjPropertyManager.getCoordinatorEnvironmentBean().setAsyncPrepare(true);
        arjPropertyManager.getCoordinatorEnvironmentBean().setAsyncCommit(true);
        arjPropertyManager.getCoordinatorEnvironmentBean().setAsyncRollback(true);
        
        AtomicAction A = new AtomicAction();
        
        A.begin();
        
        A.add(new BasicRecord());
        A.add(new BasicRecord());
        
        A.abort();
    }

    private void setupCoordinatorEnvironmentBean(boolean isAsyncPrepare, boolean isAsyncCommit, boolean isAsyncRollback) {
        arjPropertyManager.getCoordinatorEnvironmentBean().setAsyncPrepare(isAsyncPrepare);
        arjPropertyManager.getCoordinatorEnvironmentBean().setAsyncCommit(isAsyncCommit);
        arjPropertyManager.getCoordinatorEnvironmentBean().setAsyncRollback(isAsyncRollback);
    }

    private AtomicAction executeAsyncPrepareTest(boolean isCommit, AbstractRecord... records) {
        AtomicAction a = new AtomicAction();
        a.begin();

        for (AbstractRecord record : records) {
            a.add(record);
        }

        if (isCommit) {
            a.commit();
        } else {
            a.abort();
        }

        return a;
    }
}
