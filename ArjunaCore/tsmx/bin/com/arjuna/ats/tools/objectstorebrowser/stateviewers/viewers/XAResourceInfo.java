/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2008,
 * @author JBoss Inc.
 */
package com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers;

import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import javax.transaction.xa.XAException;

public class XAResourceInfo extends UidInfo
{
    private String txState;
    private String className;
    private String eisProductName;
    private String eisProductVersion;
    private Xid xid;
    private int timeout;

    public XAResourceInfo(XAResource xares, Xid xid, String txState)
    {
        super(toUid(xid), xares.toString());

        this.txState = txState;
        this.xid = xid;
        className = xares.getClass().getName();
//        if (xares instanceof org.jboss.jca.spi.xa.XAResourceWrapper)
        eisProductName = callMethod(xares, "getEISProductName");
        eisProductVersion = callMethod(xares, "getEISProductVersion");

        try
        {
            timeout = xares.getTransactionTimeout();
        }
        catch (XAException e)
        {
            System.out.println(e.getMessage());
            timeout = -1;
        }
    }

    private String callMethod(Object object, String mName)
    {
        try
        {
            return (String) object.getClass().getMethod(mName).invoke(object);
        }
        catch (NoSuchMethodException e)
        {
            // ignore
        }
        catch (Exception e)
        {
            System.out.println(e);
        }

        return "";
    }

    public String getTxState()
    {
        return txState;
    }

    public String getClassName()
    {
        return className;
    }

    public String getEisProductName()
    {
        return eisProductName;
    }

    public String getEisProductVersion()
    {
        return eisProductVersion;
    }

    public String getXid()
    {
        return xid == null ? "null" : xid.toString();
    }

    public Object getTimeout()
    {
        return timeout;
    }
}
