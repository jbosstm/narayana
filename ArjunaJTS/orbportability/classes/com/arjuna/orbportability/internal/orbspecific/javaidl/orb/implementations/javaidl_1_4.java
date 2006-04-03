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
 * Copyright (C) 1998, 1999, 2000, 2001, 2002
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 * 
 * $Id: javaidl_1_4.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.orbportability.internal.orbspecific.javaidl.orb.implementations;

import com.arjuna.orbportability.internal.orbspecific.orb.implementations.ORBBase;
import com.arjuna.orbportability.common.opPropertyManager;
import com.arjuna.orbportability.Services;

public class javaidl_1_4 extends ORBBase
{
    public javaidl_1_4()
    {
	System.setProperty("org.omg.CORBA.ORBClass", "com.sun.corba.se.internal.Interceptors.PIORB");
	System.setProperty("org.omg.CORBA.ORBSingletonClass", "com.sun.corba.se.internal.corba.ORBSingleton");
	opPropertyManager.propertyManager.setProperty("com.arjuna.orbportability.internal.defaultBindMechanism", Services.bindString(Services.CONFIGURATION_FILE));
    }
}
