/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2009,
 * @author Red Hat Middleware LLC.
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

        if (ORBType.JACORB == ORBInfo.getOrbEnumValue()) {
            // Make sure the orb is ready: TODO improve on this
            Class c = ClassloadingUtility.loadClass("com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.JacOrbRCServiceInit");
            Method m = c.getDeclaredMethod("waitForRunningORBRunner", null);
            m.invoke(null, null);
        }

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
