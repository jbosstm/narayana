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
package com.hp.mwtests.ts.arjuna.naming;

/*
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: PNSTest.java 2342 2006-03-30 13:06:17Z  $
 */

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.gandiva.*;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;

public class PNSTest
{
    @Test
    public void test() throws IOException
    {
        String longAttr = new String("LONG");
        String stringAttr = new String("STRING");
        String objAttr = new String("OBJECTNAME");
        String uidAttr = new String("UID");

        long l = 12345;
        Uid u = new Uid();
        String s = new String("test");

        ObjectName o1 = new ObjectName("PNS:o1");
        ObjectName o2 = new ObjectName("PNS:o2");

        o1.setStringAttribute(stringAttr, s);
        o2.setLongAttribute(longAttr, l);
        o2.setUidAttribute(uidAttr, u);
        o1.setObjectNameAttribute(objAttr, o2);

        assertEquals(s, o1.getStringAttribute(stringAttr));
        assertEquals(o2, o1.getObjectNameAttribute(objAttr));

        assertEquals(l, o2.getLongAttribute(longAttr));
        assertEquals(u, o2.getUidAttribute(uidAttr));
    }
}
		
		
