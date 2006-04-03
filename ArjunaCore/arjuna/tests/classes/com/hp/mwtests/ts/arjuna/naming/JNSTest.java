/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated 
 * by the @authors tag. All rights reserved. 
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
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.gandiva.nameservice.NameService;

import java.io.IOException;

/*
 * To test:
 *
 * java -DLONG=#5 -DSTRING=^"foobar" com.hp.mwtests.ts.arjuna.naming.JNSTest
 */

public class JNSTest
{
    
public static void main (String[] args)
    {
	NameService nameService = new NameService(ArjunaNames.Implementation_NameService_JNS());

	try
	{
	    String longAttr = new String("LONG");
	    long lvalue = nameService.getLongAttribute(null, longAttr);

	    System.out.println("Long value: "+lvalue);

	    String stringAttr = new String("STRING");
	    String svalue = nameService.getStringAttribute(null, stringAttr);

	    System.out.println("String value: "+svalue);
	}
	catch (IOException e)
	{
	    System.out.println(e);
	}
    }
    
}

		
		
