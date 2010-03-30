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
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: ServerControlWrapper.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.interposition;

import com.arjuna.common.util.logging.*;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;

import com.arjuna.ats.jts.exceptions.ExceptionCodes;
import com.arjuna.ats.jts.logging.*;

import com.arjuna.ats.internal.jts.orbspecific.*;
import com.arjuna.ats.internal.jts.ControlWrapper;
import com.arjuna.ats.internal.jts.interposition.resources.arjuna.*;

import org.omg.CosTransactions.*;
import org.omg.CORBA.CompletionStatus;

import org.omg.CosTransactions.SubtransactionsUnavailable;
import org.omg.CosTransactions.NoTransaction;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.INVALID_TRANSACTION;

/**
 * This class attempts to mask the local/remote control issue. We try to use
 * local controls directly as much as possible and not register them with the
 * ORB until the last minute. This improves performance *significantly*. At
 * present we only do this for top-level transactions, but extending for nested
 * transactions is straightforward.
 * 
 * It also acts as a convenience class for ease of use. Therefore, some
 * Coordinator and Terminator methods may be found directly on this class.
 * Because of the way in which the implementation works, however, some of their
 * signatures may be slightly different.
 * 
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: ServerControlWrapper.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 3.3.
 */

/*
 * We create and destroy instances of this class regularly simply because
 * otherwise we would never know.
 */

public class ServerControlWrapper extends ControlWrapper
{

	public ServerControlWrapper (Control c)
	{
		super(c);
	}

	public ServerControlWrapper (ControlImple impl)
	{
		super(impl);
	}

	public ServerControlWrapper (Control c, ControlImple impl)
	{
		super(c, impl);
	}

	public ServerControlWrapper (Control c, Uid u)
	{
		super(c, u);
	}

	/*
	 * Override some Reapable methods.
	 */

	/**
	 * @message com.arjuna.ats.internal.jts.interposition.cwabort Failed to
	 *          cancel transaction:
	 */

	public int cancel ()
	{
		try
		{
			Interposition.destroy(super.get_uid());

			rollback();

			return ActionStatus.ABORTED;
		}
		catch (Unavailable ex)
		{
			return ActionStatus.INVALID;
		}
		catch (NoTransaction ex)
		{
			return ActionStatus.NO_ACTION;
		}
		catch (Exception ex)
		{
			if (jtsLogger.loggerI18N.isWarnEnabled())
			{
				jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.interposition.cwabort", ex);
			}

			return ActionStatus.INVALID;
		}
	}

	public ControlWrapper create_subtransaction () throws Unavailable,
			Inactive, SubtransactionsUnavailable, SystemException
	{
		Coordinator coord = null;

		try
		{
			coord = get_coordinator();
		}
		catch (SystemException e)
		{
			coord = null;
		}

		if (coord != null)
		{
			return new ServerControlWrapper(coord.create_subtransaction());
		}
		else
		{
			if (jtsLogger.logger.isDebugEnabled()) {
                jtsLogger.logger.debug("ServerControlWrapper::create_subtransaction - subtransaction parent is inactive.");
            }

			throw new INVALID_TRANSACTION(
					ExceptionCodes.UNAVAILABLE_COORDINATOR,
					CompletionStatus.COMPLETED_NO);
		}
	}

}
