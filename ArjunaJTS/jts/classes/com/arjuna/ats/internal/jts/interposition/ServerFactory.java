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
 * Copyright (C) 1998, 1999, 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: ServerFactory.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.interposition;

import com.arjuna.ats.jts.logging.*;

import com.arjuna.ats.internal.jts.orbspecific.interposition.*;
import com.arjuna.ats.internal.jts.orbspecific.interposition.coordinator.ServerTransaction;
import com.arjuna.ats.internal.jts.orbspecific.ControlImple;
import com.arjuna.ats.internal.jts.orbspecific.coordinator.ArjunaTransactionImple;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.coordinator.TransactionReaper;

import org.omg.CosTransactions.*;
import java.util.*;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.BAD_PARAM;

/**
 * This is a server-side factory used for creating server transactions.
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ServerFactory.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.2.4.
 */

public class ServerFactory
{

	/**
	 * @return the server transaction status.
	 * @since JTS 2.1.1.
	 */

	public static org.omg.CosTransactions.Status getCurrentStatus (Uid uid)
			throws SystemException
	{
		if (!uid.valid())
			throw new BAD_PARAM();
		else
		{
			try
			{
				ControlImple ctx = null;

				synchronized (ServerControl.allServerControls)
				{
					ctx = (ServerControl) ServerControl.allServerControls.get(uid);
				}

				/*
				 * If it's not present then check each element's savingUid just
				 * in case that is being used instead of the transaction id.
				 * This is because a server transaction actually has two names:
				 * 
				 * (i) the tid it pretends to be (ii) the tid it actually is and
				 * saves its intentions list in.
				 * 
				 * Don't bother synchronizing since the hash table is
				 * synchronized anyway, and we're not bothered if new items go
				 * in while we're looking. If the element we're looking for
				 * isn't there now it won't be there at all.
				 */

				if (ctx == null)
				{
					Enumeration e = ServerControl.allServerControls.elements();

					while (e.hasMoreElements())
					{
						ctx = (ServerControl) e.nextElement();

						if (ctx.getImplHandle().getSavingUid().equals(uid))
						{
							break;
						}
					}
				}

				if (ctx != null)
					return ctx.getImplHandle().get_status();
				else
					throw new NoTransaction();
			}
			catch (NoTransaction ex)
			{
				return org.omg.CosTransactions.Status.StatusNoTransaction;
			}
			catch (Exception e) {
                jtsLogger.i18NLogger.warn_interposition_sfcaught("ServerFactory.getCurrentStatus", uid, e);

                return Status.StatusUnknown;
            }
		}
	}

	/*
	 * @return the status of the transaction, even if it is not active.
	 * 
	 * @since JTS 2.1.1.
	 */

	public static org.omg.CosTransactions.Status getStatus (Uid u)
			throws NoTransaction, SystemException
	{
		org.omg.CosTransactions.Status s = org.omg.CosTransactions.Status.StatusUnknown;

		try
		{
			s = getCurrentStatus(u);
		}
		catch (SystemException e2)
		{
			throw e2;
		}
		catch (Exception e3) {
            jtsLogger.i18NLogger.warn_interposition_sfcaught("ServerFactory.getStatus", u, e3);

            return Status.StatusUnknown;
        }

		if ((s == org.omg.CosTransactions.Status.StatusUnknown)
				|| (s == org.omg.CosTransactions.Status.StatusNoTransaction))
		{
			return getOSStatus(u);
		}
		else
			return s;
	}

	/**
	 * @return the status of the server transaction as recorded in the object
	 *         store.
	 * @since JTS 2.1.1.
	 */

	public static org.omg.CosTransactions.Status getOSStatus (Uid u)
			throws NoTransaction, SystemException
	{
		org.omg.CosTransactions.Status s = org.omg.CosTransactions.Status.StatusUnknown;

		if (!u.valid())
			throw new BAD_PARAM();
		else
		{
			// if here then it is not active, so look in the object store

			ObjectStore store = com.arjuna.ats.arjuna.coordinator.TxControl.getStore();

			try
			{
				/*
				 * Do we need to search server transactions too? Possibly not,
				 * since an interposed coordinator can never always say with
				 * certainty what the status is of the root coordinator.
				 */

				int status = store.currentState(u, ServerTransaction.typeName());

				switch (status)
				{
				case StateStatus.OS_UNKNOWN: // means no state present
					return org.omg.CosTransactions.Status.StatusNoTransaction;
				case StateStatus.OS_COMMITTED:
					return org.omg.CosTransactions.Status.StatusCommitted;
				case StateStatus.OS_UNCOMMITTED:
					return org.omg.CosTransactions.Status.StatusPrepared;
				case StateStatus.OS_HIDDEN:
				case StateStatus.OS_COMMITTED_HIDDEN:
				case StateStatus.OS_UNCOMMITTED_HIDDEN:
					return org.omg.CosTransactions.Status.StatusPrepared;
				default:
					return org.omg.CosTransactions.Status.StatusUnknown;
				}
			}
			catch (Exception e) {
                jtsLogger.i18NLogger.warn_interposition_sfcaught("ServerFactory.getStatus", u, e);

                return Status.StatusUnknown;
            }
		}
	}

	public static ServerControl create_transaction (Uid u, Control parentControl, ArjunaTransactionImple parentImpl, Coordinator realCoord, Terminator realTerm, int time_out)
	{
		ServerControl tranControl = new ServerControl(u, parentControl,
				parentImpl, realCoord, realTerm);

		/*
		 * We can't just add server transactions to the reaper list directly
		 * because they are wrapped by the interposition hierarchy and if we
		 * reap the transaction, we need to reap the hierarchy too. So, in the
		 * interposition classes we add the hierarchy to the reaper list.
		 */

		if ((time_out != 0) && (parentImpl == null))
		{
			TransactionReaper reaper = TransactionReaper.transactionReaper();

			reaper.insert(new ServerControlWrapper((ControlImple) tranControl), time_out);
		}

		return tranControl;
	}

	public static ServerControl create_subtransaction (Uid actUid, Coordinator realCoord, Terminator realTerm, ServerControl parent)
	{
		if (parent == null) {
            jtsLogger.i18NLogger.warn_interposition_sfnoparent("ServerFactory.create_subtransaction");

            return null;
        }

		ServerControl toReturn = null;

		try
		{
			Control handle = parent.getControl();
			ArjunaTransactionImple tranHandle = parent.getImplHandle();

			toReturn = new ServerControl(actUid, handle, tranHandle, realCoord,
					realTerm);

			handle = null;
			tranHandle = null;
		}
		catch (Exception e)
		{
			if (toReturn != null)
			{
				try
				{
					toReturn.destroy(); // will delete itself
				}
				catch (Exception ex)
				{
				}
			}
		}

		return toReturn;
	}

}
