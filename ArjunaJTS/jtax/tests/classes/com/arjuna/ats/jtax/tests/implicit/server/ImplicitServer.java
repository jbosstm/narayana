/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
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
package com.arjuna.ats.jtax.tests.implicit.server;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ImplicitServer.java 2342 2006-03-30 13:06:17Z  $
 */

import com.arjuna.orbportability.*;

import com.arjuna.ats.jtax.tests.implicit.impl.*;
import com.arjuna.ats.jta.common.jtaPropertyManager;

import org.junit.Test;

public class ImplicitServer
{
    @Test
    public void test() throws Exception
    {
        jtaPropertyManager.getJTAEnvironmentBean().setTransactionManagerClassName(com.arjuna.ats.internal.jta.transaction.jts.TransactionManagerImple.class.getName());
        jtaPropertyManager.getJTAEnvironmentBean().setUserTransactionClassName(com.arjuna.ats.internal.jta.transaction.jts.UserTransactionImple.class.getName());

        ORB orb = ORB.getInstance("implicitserver-orb");
        OA oa = OA.getRootOA(orb);

        orb.initORB(new String[] {}, null);
        oa.initPOA(new String[] {});

        RemoteImpl impl = new RemoteImpl();

        oa.objectIsReady(impl);

        org.omg.CORBA.Object obj = oa.corbaReference(impl);

        // TODO registerService(args[0], orb.orb().object_to_string(obj));

        orb.orb().run();
    }
}
