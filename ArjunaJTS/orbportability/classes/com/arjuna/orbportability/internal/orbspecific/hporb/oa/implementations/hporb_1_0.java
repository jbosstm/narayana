/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: hporb_1_0.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.orbportability.internal.orbspecific.hporb.oa.implementations;

import com.arjuna.orbportability.oa.core.POAImple;
import com.arjuna.orbportability.logging.opLogger;
import com.arjuna.orbportability.internal.orbspecific.oa.implementations.POABase;
import org.omg.PortableServer.*;
import org.omg.CORBA.Policy;
import java.util.*;
import java.applet.Applet;
import java.io.*;

import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.PortableServer.POAPackage.AdapterAlreadyExists;
import org.omg.PortableServer.POAPackage.InvalidPolicy;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;

public class hporb_1_0 extends POABase
{

    /**
     * @message com.arjuna.orbportability.internal.orbspecific.hporb.oa.implementations.hporb_1.0.norootoa {0} called without root POA.
     */
public void createPOA (String adapterName,
		       Policy[] policies) throws AdapterAlreadyExists, InvalidPolicy, AdapterInactive, SystemException
    {
	if (_poa == null)
	{
            if ( opLogger.loggerI18N.isWarnEnabled() )
            {
                opLogger.loggerI18N.warn( "com.arjuna.orbportability.internal.orbspecific.hporb.oa.implementations.hporb_1.0.norootoa",
                                            new Object[] { "hporb_1_0.createPOA" } );
            }

	    throw new AdapterInactive();
	}

	POA childPoa = _poa.create_POA(adapterName, _poa.the_POAManager(), policies);

	childPoa.the_POAManager().activate();

	super._poas.put(adapterName, childPoa);	
    }

}

