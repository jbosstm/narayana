/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.jbossatx.jts;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.OA;
import com.arjuna.ats.jbossatx.logging.jbossatxLogger;
import com.arjuna.common.internal.util.ClassloadingUtility;
import com.arjuna.orbportability.ORBInfo;
import com.arjuna.orbportability.ORBType;

import java.lang.reflect.Method;

/**
 * JBoss Transaction Recovery Service.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 * @version $Id$
 */
public class RecoveryManagerService extends com.arjuna.ats.jbossatx.jta.RecoveryManagerService
{
    public RecoveryManagerService(org.omg.CORBA.ORB theCorbaORB) throws Exception
    {
        jbossatxLogger.i18NLogger.info_jts_RecoveryManagerService_init();

        /** Create an ORB portability wrapper around the CORBA ORB services orb **/
        ORB orb = ORB.getInstance(TransactionManagerService.ORB_NAME);

        org.omg.PortableServer.POA rootPOA = org.omg.PortableServer.POAHelper.narrow(theCorbaORB.resolve_initial_references("RootPOA"));

        orb.setOrb(theCorbaORB);
        OA oa = OA.getRootOA(orb);
        oa.setPOA(rootPOA);

        ORBManager.setORB(orb);
        ORBManager.setPOA(oa);
    }

    @Override
    public void stop() throws Exception {
        super.stop();

        ORB.getInstance(TransactionManagerService.ORB_NAME).shutdown();
    }
}