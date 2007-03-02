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
package com.hp.mwtests.ts.jta.jts;

import junit.framework.TestCase;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;

public class JTSTestCase extends TestCase
{
    private ORB orb ;
    private RootOA oa ;
    
    protected void setUp()
        throws Exception
    {
        orb = ORB.getInstance("test");
        oa = OA.getRootOA(orb);
        
        orb.initORB(new String[0], null);
        oa.initOA();

        ORBManager.setORB(orb);
        ORBManager.setPOA(oa);
    }

    protected void tearDown()
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
}
