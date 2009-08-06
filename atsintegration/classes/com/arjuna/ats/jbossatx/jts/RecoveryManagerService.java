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

import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.OA;
import com.arjuna.ats.internal.jts.recovery.RecoveryORBManager;
import org.jboss.logging.Logger;

/**
 * JBoss Transaction Recovery Service.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 * @version $Id$
 */
public class RecoveryManagerService extends com.arjuna.ats.jbossatx.jta.RecoveryManagerService
{
    private final Logger log = org.jboss.logging.Logger.getLogger(RecoveryManagerService.class);

    public RecoveryManagerService(org.omg.CORBA.ORB theCorbaORB) throws Exception
    {
        log.info("<init> JTS transaction recovery manager");

        /** Create an ORB portability wrapper around the CORBA ORB services orb **/
        ORB orb = ORB.getInstance("jboss-atx");

        org.omg.PortableServer.POA rootPOA = org.omg.PortableServer.POAHelper.narrow(theCorbaORB.resolve_initial_references("RootPOA"));

        orb.setOrb(theCorbaORB);
        OA oa = OA.getRootOA(orb);
        oa.setPOA(rootPOA);

        RecoveryORBManager.setORB(orb);
        RecoveryORBManager.setPOA(oa);
    }
}
