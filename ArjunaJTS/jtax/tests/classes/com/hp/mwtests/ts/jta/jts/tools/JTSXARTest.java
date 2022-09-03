/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.hp.mwtests.ts.jta.jts.tools;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.tools.osb.mbean.HeuristicStatus;
import com.arjuna.ats.arjuna.tools.osb.mbean.OSBTypeHandler;
import com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreBrowser;
import com.arjuna.ats.arjuna.tools.osb.mbean.UidWrapper;
import com.arjuna.ats.arjuna.tools.osb.util.JMXServer;
import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;
import com.arjuna.ats.internal.jta.tools.osb.mbean.jts.ArjunaTransactionImpleWrapper;
import com.arjuna.ats.internal.jta.transaction.jts.TransactionImple;
import com.arjuna.ats.internal.jta.transaction.jts.TransactionManagerImple;
import com.hp.mwtests.ts.jta.jts.common.DummyXA;
import com.hp.mwtests.ts.jta.jts.common.ExtendedCrashRecord;
import org.junit.Test;

import javax.management.*;
import jakarta.transaction.*;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;

/**
 *
 * @author Mike Musgrove
 */
/**
 * @deprecated as of 5.0.5.Final In a subsequent release we will change packages names in order to 
 * provide a better separation between public and internal classes.
 */
@Deprecated // in order to provide a better separation between public and internal classes.
public class JTSXARTest extends JTSOSBTestBase {
    void injectCommitException(String exceptionType) throws Exception {
        XAFailureSpec fault = new XAFailureSpec(
                "JTSXARTest", XAFailureMode.XAEXCEPTION, XAFailureType.XARES_COMMIT, exceptionType, 0);
        XAException xae = XAFailureResource.getXAExceptionType(exceptionType);
        int expectedXAE = xae == null ? HeuristicStatus.UNKNOWN_XA_ERROR_CODE : xae.errorCode;

        TransactionManagerImple tm = new TransactionManagerImple();
        Uid txUid;
        TransactionImple txn;

        // clean the thread that will create the transaction
        ThreadActionData.purgeActions();

        // begin a transaction
        tm.begin();
        txn = (TransactionImple) tm.getTransaction();
        txUid = txn.get_uid();

        // enlist two XA resources
        tm.getTransaction().enlistResource(new XAFailureResource());
        tm.getTransaction().enlistResource(new XAFailureResource(fault));

        // commit the transaction and check for any expected exceptions
        try {
            tm.commit();
        } catch (HeuristicRollbackException e) {
            if (!XAFailureSpec.compatibleHeuristics(XAException.XA_HEURRB, expectedXAE))
                throw e;
        } catch (RollbackException e) {
            if (!XAFailureSpec.compatibleHeuristics(XAException.XA_RBROLLBACK, expectedXAE))
                throw e;
        } catch (HeuristicMixedException e) {
            if (!XAFailureSpec.compatibleHeuristics(XAException.XA_HEURMIX, expectedXAE))
                throw e;
        } catch (SystemException e) {
            if (!XAFailureSpec.compatibleHeuristics(XAException.XAER_RMFAIL, expectedXAE))
                throw e;
        }

        // Validate that the correct MBeans were created to instrument the transaction and participants
        // and validate they have the correct heuristic status
        ObjStoreBrowser osb = createObjStoreBrowser(true);

        UidWrapper w = osb.findUid(txUid);
        assertNotNull(w);

        Set<UidWrapper> wrappers = validateChildBeans(osb, w.getName(), 1, expectedXAE);

        assertFalse(wrappers.isEmpty());
    }

    @Test
    public void testXAR() throws Exception {
        injectCommitException("XAER_RMFAIL");
    }

    @Test
    public void testHeuristic() throws Exception {
        injectCommitException("XA_HEURRB");
    }

    private Set<UidWrapper> validateChildBeans(ObjStoreBrowser osb, String name, int expectedNumberOfChildBeans, int expectedXAE)
            throws MalformedObjectNameException, ReflectionException, InstanceNotFoundException, AttributeNotFoundException, MBeanException {
        MBeanServer mbs = JMXServer.getAgent().getServer();
        ObjectName txnON = new ObjectName(name);
        Object aid = mbs.getAttribute(txnON, "Id");
        assertNotNull(aid);
        Uid uidOfTxn = new Uid(aid.toString());
        Set<ObjectName> participants = mbs.queryNames(new ObjectName(name + ",puid=*"), null);

        assertEquals(expectedNumberOfChildBeans, participants.size());

        Set<UidWrapper> wrappers = new HashSet<UidWrapper>();

        for (ObjectName on : participants) {
            //mbs.getAttributes(on, new String[] {"Id", "Type", "Status", "HeuristicStatus", "FormatId", "GlobalTransactionId", "NodeName", "BranchQualifier"});
            AttributeList al = mbs.getAttributes(on, new String[] {"Id", "Status", "HeuristicStatus", "GlobalTransactionId"});

            for (Attribute a : al.asList()) {
                if ("Id".equals(a.getName())) {
                    Uid uid = new Uid(a.getValue().toString());
                    UidWrapper w = osb.findUid(uid);

                    // assert that the wrapper is in the mbean wrapper cache
                    assertNotNull(w);

                    wrappers.add(w);
                } else if ("GlobalTransactionId".equals(a.getName())) {
                    byte[] gtid = (byte[]) a.getValue();
                    Uid txOfXar = new Uid(gtid);

                    // assert that the gtid of the participant matches the parent action
                    assertEquals(txOfXar, uidOfTxn);
                } else if ("HeuristicStatus".equals(a.getName())) {
                    HeuristicStatus hs = HeuristicStatus.valueOf(a.getValue().toString());

                    // assert that the instrumented heuristic status has the expected value
                    assertEquals(hs.getXAErrorCode(), expectedXAE);
                }
            }
        }

        return wrappers;
    }
}
