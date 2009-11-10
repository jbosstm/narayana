/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2009,
 * @author JBoss, a division of Red Hat.
 */
package com.arjuna.ats.internal.jta.utils.jts;

import com.arjuna.ats.jta.xa.XATxConverter;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jts.orbspecific.coordinator.ArjunaTransactionImple;
import com.arjuna.ats.internal.jts.orbspecific.ControlImple;
import com.arjuna.ats.jts.utils.Utility;

import javax.transaction.xa.Xid;

/**
 * Utility functions for JTS (i.e. jtax) Xid handling.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
public class XidUtils
{
    public static Xid getXid (Uid uid, boolean branch) throws IllegalStateException
    {
        return XATxConverter.getXid(uid, branch, ArjunaTransactionImple.interpositionType());
    }

    public static Xid getXid (org.omg.CosTransactions.Control cont, boolean branch) throws IllegalStateException
    {
        if (cont == null)
            throw new IllegalStateException();

        Uid u = null;

        if (cont instanceof ControlImple)
        {
            u = ((ControlImple) cont).get_uid();

            return getXid(u, branch);
        }
        else
        {
            try
            {
                u = Utility.getUid(cont);
                if(u.equals(Uid.nullUid())) {
                    throw new IllegalStateException();
                }

                return getXid(u, branch);
            }
            catch (Exception e)
            {
                throw new IllegalStateException(e);
            }
        }
    }
}
