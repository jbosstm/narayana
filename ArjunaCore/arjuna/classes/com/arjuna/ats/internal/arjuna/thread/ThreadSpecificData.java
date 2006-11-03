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
/*
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: ThreadSpecificData.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.thread;

/**
 * A thread specific data implementation. An instance of this class will
 * maintain data on behalf of each thread. One thread cannot gain access to
 * another thread's data.
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ThreadSpecificData.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class ThreadSpecificData
{

	/**
	 * Create a new instance. Space will be allocated dynamically as required.
	 */

	public ThreadSpecificData ()
	{
		_table = new ThreadLocal();
	}

	/**
	 * Create a new instance with the specified size. Space will be increased
	 * dynamically as required.
	 * 
	 * @deprecated No longer supported.
	 */

	public ThreadSpecificData (int v)
	{
		_table = new ThreadLocal();
	}

	public void finalize ()
	{
		_table = null;
	}

	/**
	 * Associate the specified data with the current thread.
	 */

	public void setSpecific (Object o)
	{
		_table.set(o);
	}

	/**
	 * Return the data associated with the current thread. If no association has
	 * occurred then null will be returned. Obviously if null was legally
	 * associated with the thread then it is not possible to determine whether
	 * data was stored.
	 */

	public Object getSpecific ()
	{
		return _table.get();
	}

	/**
	 * Returns whether data has been associated with the current thread.
	 */

	public boolean hasSpecific ()
	{
		if (_table.get() != null)
			return true;
		else
			return false;
	}

	/**
	 * Remove the data associated with the current thread.
	 */

	public void removeSpecific ()
	{
		_table.set(null);
	}

	private ThreadLocal _table;

}
