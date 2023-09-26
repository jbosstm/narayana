/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.jta.utils.jts;

import javax.transaction.xa.Xid;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jts.orbspecific.ControlImple;
import com.arjuna.ats.internal.jts.orbspecific.coordinator.ArjunaTransactionImple;
import com.arjuna.ats.jta.xa.XATxConverter;
import com.arjuna.ats.jts.utils.Utility;

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