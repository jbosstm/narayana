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
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: XAUtils.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jta.utils;

import com.arjuna.ats.jta.xa.XidImple;

import javax.transaction.xa.*;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: XAUtils.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 3.3.
 */

public class XAUtils
{

	public static final boolean mustEndSuspendedRMs (XAResource res)
	{
		boolean end = false;

		if (res != null)
		{
			String name = res.getClass().getName().toLowerCase();

			if (name.indexOf(XAUtils.ORACLE) != -1)
				end = true;
		}

		return end;
	}

	public static final boolean canOptimizeDelist (XAResource res)
	{
		boolean optimize = true;

		if (res != null)
		{
			String name = res.getClass().getName().toLowerCase();

			if (name.indexOf(XAUtils.ORACLE) != -1)
				optimize = false;
		}

		return optimize;
	}

	public static final String getXANodeName (Xid xid)
	{
        XidImple xidImple;
        if(xid instanceof XidImple) {
            xidImple = (XidImple)xid;
        } else {
            xidImple = new XidImple(xid);
        }
        return xidImple.getNodeName();
	}

	private static final String ORACLE = "oracle";
}
