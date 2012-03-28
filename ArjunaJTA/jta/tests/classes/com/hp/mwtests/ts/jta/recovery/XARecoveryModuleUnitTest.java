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
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: xidcheck.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jta.recovery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.AddOutcome;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeManager;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeMap;
import com.arjuna.ats.arjuna.recovery.RecoverAtomicAction;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.jta.recovery.arjunacore.NodeNameXAResourceOrphanFilter;
import com.arjuna.ats.internal.jta.recovery.arjunacore.RecoveryXids;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.internal.jta.resources.arjunacore.XAResourceRecord;
import com.arjuna.ats.internal.jta.transaction.arjunacore.AtomicAction;
import com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.jca.TransactionImple;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.recovery.XAResourceOrphanFilter;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import com.arjuna.ats.jta.xa.XidImple;
import com.hp.mwtests.ts.jta.common.RecoveryXAResource;

public class XARecoveryModuleUnitTest
{
    @Test
    public void testNull ()
    {
        XARecoveryModule xarm = new XARecoveryModule();
        
        xarm.periodicWorkFirstPass();
        xarm.periodicWorkSecondPass();
        
        assertNotNull(xarm.id());
    }

    
    @Test
    public void testRecoverFromMultipleXAResourceRecovery() throws Exception {
        // Make sure the file doesn't exist
        assertFalse(new File("XARR.txt").exists());

        ArrayList<String> r = new ArrayList<String>();

        AtomicAction aa = new AtomicAction();
        aa.begin();
        assertEquals(AddOutcome.AR_ADDED, aa.add(new XAResourceRecord(null, new XARRTestResource(), new XidImple(aa), null)));

        Class c = BasicAction.class;
        Method method = c.getDeclaredMethod("prepare", boolean.class);
        method.setAccessible(true);
        int result = (Integer) method.invoke(aa, new Object[] { true });
        assertEquals(result, TwoPhaseOutcome.PREPARE_OK);

        // Make sure the file exists
        assertTrue(new File("XARR.txt").exists());

//        AtomicActionRecoveryModule aaRecoveryModule = new AtomicActionRecoveryModule();
//        aaRecoveryModule.periodicWorkFirstPass();
//        aaRecoveryModule.periodicWorkSecondPass();

        RecordTypeManager.manager().add(new RecordTypeMap() {
                @SuppressWarnings("unchecked")
                public Class getRecordClass ()
                {
                    return XAResourceRecord.class;
                }
                
                public int getType ()
                {
                    return RecordType.JTA_RECORD;
                }
        });
        
        List<String> xarn = new ArrayList<String>();
        xarn.add(NodeNameXAResourceOrphanFilter.RECOVER_ALL_NODES);
        
        jtaPropertyManager.getJTAEnvironmentBean().setXaRecoveryNodes(xarn);
        XARecoveryModule xaRecoveryModule = new XARecoveryModule();
        Field safetyIntervalMillis = RecoveryXids.class.getDeclaredField("safetyIntervalMillis");
        safetyIntervalMillis.setAccessible(true);
        safetyIntervalMillis.set(null, 0);
        xaRecoveryModule.addXAResourceRecoveryHelper(new XARROne());
        xaRecoveryModule.addXAResourceRecoveryHelper(new XARRTwo());
        xaRecoveryModule.addXAResourceOrphanFilter(new com.arjuna.ats.internal.jta.recovery.arjunacore.JTATransactionLogXAResourceOrphanFilter());
        xaRecoveryModule.addXAResourceOrphanFilter(new com.arjuna.ats.internal.jta.recovery.arjunacore.JTANodeNameXAResourceOrphanFilter());
        RecoveryManager.manager().addModule(xaRecoveryModule);
        
        
        // This is done rather than using the AtomicActionRecoveryModule as the transaction is inflight
        RecoverAtomicAction rcvAtomicAction = new RecoverAtomicAction(aa.get_uid(), ActionStatus.COMMITTED);
        rcvAtomicAction.replayPhase2();
        
        // The XARM would execute next
        xaRecoveryModule.periodicWorkFirstPass();
        xaRecoveryModule.periodicWorkSecondPass();

        // Make sure the file doesn't exist
        assertFalse(new File("XARR.txt").exists());
        
        aa.abort();
    }
    
    @Test
    public void testRecover () throws Exception
    {
        ArrayList<String> r = new ArrayList<String>();
        TransactionImple tx = new TransactionImple(0);
        
        assertTrue(tx.enlistResource(new RecoveryXAResource()));
        
        assertEquals(tx.doPrepare(), TwoPhaseOutcome.PREPARE_OK);
        
        r.add("com.hp.mwtests.ts.jta.recovery.DummyXARecoveryResource");

        jtaPropertyManager.getJTAEnvironmentBean().setXaResourceRecoveryClassNames(r);
        
        XARecoveryModule xarm = new XARecoveryModule();

        assertNull(xarm.getNewXAResource( new XAResourceRecord(null, null, new XidImple(), null) ));
        
        for (int i = 0; i < 11; i++)
        {
            xarm.periodicWorkFirstPass();
            xarm.periodicWorkSecondPass();
        }
        
        assertTrue(xarm.getNewXAResource(  new XAResourceRecord(null, null, new XidImple(new Uid()), null) ) == null);
        
        assertNull(xarm.getNewXAResource( new XAResourceRecord(null, null, new XidImple(), null) ));
    }
    
    @Test
    public void testFailures () throws Exception
    {
        XARecoveryModule xarm = new XARecoveryModule();       
        Class[] parameterTypes = new Class[2];
        Uid u = new Uid();
        Xid x = new XidImple();
        
        parameterTypes[0] = Xid.class;
        parameterTypes[1] = Uid.class;
      
        Method m = xarm.getClass().getDeclaredMethod("addFailure", parameterTypes);
        m.setAccessible(true);
      
        Object[] parameters = new Object[2];
        parameters[0] = x;
        parameters[1] = u;
      
        m.invoke(xarm, parameters);
        
        parameterTypes = new Class[1];
        parameterTypes[0] = Xid.class;
        
        parameters = new Object[1];
        parameters[0] = x;
        
        m = xarm.getClass().getDeclaredMethod("previousFailure", parameterTypes);
        m.setAccessible(true);
        
        Uid ret = (Uid) m.invoke(xarm, parameters);
        
        assertEquals(ret, u);
        
        parameterTypes = new Class[2];
        parameterTypes[0] = Xid.class;
        parameterTypes[1] = Uid.class;
        
        parameters = new Object[2];
        parameters[0] = x;
        parameters[1] = u;
        
        m = xarm.getClass().getDeclaredMethod("removeFailure", parameterTypes);
        m.setAccessible(true);
        
        m.invoke(xarm, parameters);
               
        m = xarm.getClass().getDeclaredMethod("clearAllFailures", (Class[]) null);
        m.setAccessible(true);
        
        m.invoke(xarm, (Object[]) null);
    }

    @Test
    public void testXAResourceRecoveryHelperRegistration() {

        XARecoveryModule xaRecoveryModule = new XARecoveryModule();
        XAResourceRecoveryHelper xaResourceRecoveryHelper = new DummyXAResourceRecoveryHelper();

        xaRecoveryModule.addXAResourceRecoveryHelper(xaResourceRecoveryHelper);
        xaRecoveryModule.removeXAResourceRecoveryHelper(xaResourceRecoveryHelper);
    }

    class DummyXAResourceRecoveryHelper implements XAResourceRecoveryHelper {
        @Override
        public boolean initialise(String p) throws Exception
        {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public XAResource[] getXAResources() throws Exception
        {
            return new XAResource[0];  //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    @Test
    public void testXAResourceOrphanFilterRegistration() {

        XARecoveryModule xaRecoveryModule = new XARecoveryModule();
        XAResourceOrphanFilter xaResourceOrphanFilter = new DummyXAResourceOrphanFilter();

        xaRecoveryModule.addXAResourceOrphanFilter(xaResourceOrphanFilter);
        xaRecoveryModule.removeXAResourceOrphanFilter(xaResourceOrphanFilter);
    }
    
    @Test
    public void testXAResourceOrphanFilter () throws Exception
    {
        XAResourceOrphanFilter xaResourceOrphanFilter = new DummyXAResourceOrphanFilter(XAResourceOrphanFilter.Vote.ROLLBACK);
    
        XARecoveryModule xarm = new XARecoveryModule();
        
        xarm.addXAResourceOrphanFilter(xaResourceOrphanFilter);
        
        Class[] parameterTypes = new Class[2];
        
        parameterTypes[0] = XAResource.class;
        parameterTypes[1] = Xid.class;
        
        Method m = xarm.getClass().getDeclaredMethod("handleOrphan", parameterTypes);
        m.setAccessible(true);
        
        Object[] parameters = new Object[2];
        parameters[0] = new RecoveryXAResource();
        parameters[1] = new XidImple();
        
        m.invoke(xarm, parameters);
    }

    class DummyXAResourceOrphanFilter implements XAResourceOrphanFilter
    {
        public DummyXAResourceOrphanFilter ()
        {
            _vote = null;
        }
        
        public DummyXAResourceOrphanFilter (Vote v)
        {
            _vote = v;
        }
        
        @Override
        public Vote checkXid(Xid xid)
        {
            return _vote;
        }
        
        private Vote _vote;
    }
}
