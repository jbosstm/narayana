/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2009,
 * @author JBoss Inc.
 */
package com.hp.mwtests.ts.jta.jts.subordinate;

import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.arjuna.orbportability.OA;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinateTransaction;
import com.arjuna.ats.internal.jta.transaction.jts.subordinate.jca.TransactionImple;
import org.junit.Before;
import org.junit.After;

/**
 * JTAX version of the Subordinate transaction tests.
 */
public class SubordinateTestCase extends com.hp.mwtests.ts.jta.subordinate.SubordinateTestCase
{
    // we mostly reuse the JTA version of the test class, but need to ensure correct config, orb init
    // and use of the appropriate tx impl class:
    
    private ORB orb ;
    private RootOA oa ;

    @Before
    public void setUp()
        throws Exception
    {
        System.setProperty("com.arjuna.ats.jta.jtaTMImplementation", "com.arjuna.ats.internal.jta.transaction.jts.TransactionManagerImple");
        System.setProperty("com.arjuna.ats.jta.jtaUTImplementation", "com.arjuna.ats.internal.jta.transaction.jts.UserTransactionImple");
        
        orb = ORB.getInstance("test");
        oa = OA.getRootOA(orb);
        
        orb.initORB(new String[0], null);
        oa.initOA();

        ORBManager.setORB(orb);
        ORBManager.setPOA(oa);
    }

    @After
    public void tearDown()
        throws Exception
    {
        if (oa != null)
        {
            oa.destroy();
        }
        if (orb != null)
        {
            orb.shutdown();
        }
    }
    
    @Override
    public SubordinateTransaction createTransaction() {
            return new TransactionImple(0); // implicit begin
    }
}
