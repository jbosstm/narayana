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
package com.hp.mwtests.ts.txoj.atomicobject;

import com.arjuna.ats.arjuna.AtomicAction;

/*
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: AtomicObjectTest3.java 2342 2006-03-30 13:06:17Z  $
 */

public class AbortObject extends Thread
{
	public AbortObject ()
	    {
	    }

	public void run ()
	    {
		int thr = nextThreadId;

		nextThreadId++;

		AtomicAction a = new AtomicAction();

		a.begin();

		AtomicObjectTest3.indent(thr, 0);
		System.out.println("begin");

		AtomicObjectTest3.randomOperation(thr, 0);
		AtomicObjectTest3.randomOperation(thr, 0);

		a.abort();

		AtomicObjectTest3.indent(thr, 0);
		System.out.println("abort");
	    }

	private static int nextThreadId = 3;

	}
