/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jta.utils;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.arjuna.ats.internal.jta.xa.XID;
import com.arjuna.ats.jta.common.JTAEnvironmentBean;
import com.arjuna.ats.jta.xa.XATxConverter;
import com.arjuna.ats.jta.xa.XidImple;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

import java.util.List;

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

	public static boolean isSameRM(XAResource r1, XAResource r2) throws XAException {
		if (!xaResourceIsSameRMClassNames.isEmpty()) {
			if (xaResourceIsSameRMClassNames.contains(r1.getClass().getName()) || xaResourceIsSameRMClassNames.contains(r2.getClass().getName())) {
				return false;
			}
		}

		return r1.isSameRM(r2);
	}

	/**
	 * Trying to convert {@link Xid} to Narayana implementation
	 * for being able to access the internal byte array
	 * in order to return node name codified inside of the Xid.
	 *
	 * @param xid  the xid to check the node name
	 * @return  node name saved in the xid
	 */
	public static final String getXANodeName (Xid xid)
	{
        return XATxConverter.getNodeName(getXIDfromXid(xid));
	}

	/**
	 * Returning subordinate node name codified inside of the Xid.
	 *
	 * @param xid  the xid to check the subordinate node name
	 * @return  subordinate node name saved in the xid
	 */
	public static final String getSubordinateNodeName (Xid xid)
	{
	    return XATxConverter.getSubordinateNodeName(getXIDfromXid(xid));
	}

	/**
	 * Returning eis name codified inside of the Xid.
	 *
	 * @param xid  the xid to check the eis name
	 * @return  eis name integer saved in the xid
	 */
	public static final Integer getEisName (Xid xid)
	{
	    return XATxConverter.getEISName(getXIDfromXid(xid));
	}

	private static final XID getXIDfromXid(Xid xid) {
         XidImple xidImple;
         if(xid instanceof XidImple) {
             xidImple = (XidImple)xid;
         } else {
             xidImple = new XidImple(xid);
         }
         return xidImple.getXID();
	}

	private static final List<String> xaResourceIsSameRMClassNames = BeanPopulator
			.getDefaultInstance(JTAEnvironmentBean.class)
			.getXaResourceIsSameRMClassNames();

	private static final String ORACLE = "oracle";
}