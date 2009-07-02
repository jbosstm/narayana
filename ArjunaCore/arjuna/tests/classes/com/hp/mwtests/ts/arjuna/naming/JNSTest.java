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
 * $Id: JNSTest.java 2342 2006-03-30 13:06:17Z  $
 */

import com.arjuna.ats.arjuna.ArjunaNames;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.gandiva.nameservice.NameService;
import com.arjuna.common.util.propertyservice.PropertyManager;

import java.io.IOException;

import org.junit.Test;
import static org.junit.Assert.*;

public class JNSTest
{
    @Test
    public void test() throws IOException
    {
        NameService nameService = new NameService(ArjunaNames.Implementation_NameService_JNS());
        PropertyManager propertyManager = arjPropertyManager.getPropertyManager();

        propertyManager.setProperty("TESTOBJ" + "." + "TESTATTR", "#1");
        long lvalue = nameService.getLongAttribute("TESTOBJ", "TESTATTR");
        assertEquals(1, lvalue);

        propertyManager.setProperty("TESTOBJ" + "." + "TESTATTR", "^StringVal");
        String svalue = nameService.getStringAttribute("TESTOBJ", "TESTATTR");

        assertEquals("StringVal", svalue);
    }
}

		
		
