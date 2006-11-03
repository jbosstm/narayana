/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
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
package com.hp.mwtests.orbportability.initialisation.postset;

import com.arjuna.orbportability.utils.InitClassInterface;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: SinglePostSetUsingInterface.java 2342 2006-03-30 13:06:17Z  $
 */

public class SinglePostSetUsingInterface implements InitClassInterface
{
	/**
	 * This method is called and passed the object which is associated with this pre/post-initialisation routine.
	 *
	 * @param obj The object which has or is being initialised.
	 */
	public void invoke(Object obj)
	{
		System.out.println(this.getClass().getName()+".invoke("+obj+"): called");

		_called = true;
		_passedObj = obj;
	}

	public static boolean _called = false;
	public static Object  _passedObj = null;
}
