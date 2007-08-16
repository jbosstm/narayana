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
 * Copyright (C) 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: StatusConverter.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jta.utils.arjunacore;

import com.arjuna.ats.arjuna.coordinator.ActionStatus;

public class StatusConverter
{

	public static int convert (int status)
	{
		switch (status)
		{
		case ActionStatus.RUNNING:
			return javax.transaction.Status.STATUS_ACTIVE;
		case ActionStatus.COMMITTED:
		case ActionStatus.H_COMMIT:
		case ActionStatus.H_HAZARD:  // not exactly true, but ...
		case ActionStatus.H_MIXED:
			return javax.transaction.Status.STATUS_COMMITTED;
		case ActionStatus.COMMITTING:
			return javax.transaction.Status.STATUS_COMMITTING;
		case ActionStatus.ABORT_ONLY:
			return javax.transaction.Status.STATUS_MARKED_ROLLBACK;
		case ActionStatus.NO_ACTION:
			return javax.transaction.Status.STATUS_NO_TRANSACTION;
		case ActionStatus.PREPARED:
			return javax.transaction.Status.STATUS_PREPARED;
		case ActionStatus.ABORTED:
		case ActionStatus.H_ROLLBACK:
			return javax.transaction.Status.STATUS_ROLLEDBACK;
		case ActionStatus.INVALID:
			return javax.transaction.Status.STATUS_UNKNOWN;
		case ActionStatus.ABORTING:
			return javax.transaction.Status.STATUS_ROLLING_BACK;
		case ActionStatus.PREPARING:
			return javax.transaction.Status.STATUS_PREPARING;
		default:
			return javax.transaction.Status.STATUS_UNKNOWN;
		}
	}

}
