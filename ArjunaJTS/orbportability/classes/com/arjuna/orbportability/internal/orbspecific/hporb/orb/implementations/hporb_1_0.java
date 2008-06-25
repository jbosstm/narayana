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

package com.arjuna.orbportability.internal.orbspecific.hporb.orb.implementations;

import com.arjuna.orbportability.orb.core.ORBImple;
import com.arjuna.orbportability.Services;
import com.arjuna.orbportability.common.opPropertyManager;
import com.arjuna.orbportability.internal.orbspecific.orb.implementations.ORBBase;

import java.util.*;
import java.applet.Applet;
import java.io.*;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.BAD_OPERATION;

public class hporb_1_0 extends ORBBase
{

public hporb_1_0 ()
    {
	System.setProperty("org.omg.CORBA.ORBClass", "com.hp.mw.hporb.ORB");
	System.setProperty("org.omg.CORBA.ORBSingletonClass", "com.hp.mw.hporb.ORBSingleton");
	opPropertyManager.propertyManager.setProperty("com.arjuna.orbportability.internal.defaultBindMechanism", Services.bindString(Services.CONFIGURATION_FILE));
    }
    
}

