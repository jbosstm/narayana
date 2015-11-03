/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
//
// Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
//
// Arjuna Technologies Ltd.,
// Newcastle upon Tyne,
// Tyne and Wear,
// UK.
//
// $Id: AfterCrashServiceImpl02.java,v 1.5 2003/07/17 11:52:49 jcoleman Exp $
//

package org.jboss.jbossts.qa.CrashRecovery05Impls;

/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: AfterCrashServiceImpl02.java,v 1.5 2003/07/17 11:52:49 jcoleman Exp $
 */

/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */

/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: AfterCrashServiceImpl02.java,v 1.5 2003/07/17 11:52:49 jcoleman Exp $
 */


import org.jboss.jbossts.qa.CrashRecovery05.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ServerIORStore;
import org.omg.CosTransactions.*;

public class AfterCrashServiceImpl02 implements AfterCrashServiceOperations
{
	public AfterCrashServiceImpl02(int serviceNumber, int objectNumber)
	{
		System.out.println("AfterCrashServiceImpl02(" + serviceNumber + ", " + objectNumber + ")");
		_serviceNumber = serviceNumber;
		_objectNumber = objectNumber;
	}

	public void setup_oper(int number_of_resources)
	{
		_resourceImpl = new ResourceImpl02[number_of_resources];
		_resource = new Resource[number_of_resources];
		_recoveryCoordinator = new RecoveryCoordinator[number_of_resources];

		for (int index = 0; index < number_of_resources; index++)
		{
			try
			{
				_resourceImpl[index] = new ResourceImpl02(_objectNumber, index);
				ResourcePOATie servant = new ResourcePOATie(_resourceImpl[index]);

				OAInterface.objectIsReady(servant);
				_resource[index] = ResourceHelper.narrow(OAInterface.corbaReference(servant));

				System.out.println("AfterCrashServiceImpl02: loading IOR \"RecoveryCoordinator_" + _serviceNumber + "_" + _objectNumber + "_" + index + "\"");
				String recoveryCoordinatorIOR = ServerIORStore.loadIOR("RecoveryCoordinator_" + _serviceNumber + "_" + _objectNumber + "_" + index);

				if (recoveryCoordinatorIOR != null)
				{
					_recoveryCoordinator[index] = RecoveryCoordinatorHelper.narrow(ORBInterface.orb().string_to_object(recoveryCoordinatorIOR));
				}
				else
				{
					_recoveryCoordinator[index] = null;
				}
			}
			catch (Exception exception)
			{
				System.err.println("AfterCrashServiceImpl02.setup_oper: " + exception);
				exception.printStackTrace(System.err);
				_isCorrect = false;
			}
		}
	}

	public boolean check_oper(CheckBehavior[] check_behaviors)
	{
		boolean correct = true;

		for (int index = 0; index < _recoveryCoordinator.length; index++)
		{
			if (_recoveryCoordinator[index] == null)
			{
				System.err.println("AfterCrashServiceImpl02.check_oper [O" + _objectNumber + ".R" + index + "]: Done");
				correct = correct && check_behaviors[index].allow_done;
			}
			else
			{
				try
				{
					Status status = _recoveryCoordinator[index].replay_completion(_resource[index]);
					System.err.printf("AfterCrashServiceImpl02.check_oper [O%d.R%d]: replay_completion returned: %s%n",
							_objectNumber, index, status.value());
					/*
					 * replay_completion is allowed to run in the background (see RecoveredTransactionReplayer) so the
					 * resources are not guaranteed to have seen the request until the background replayer runs. Hence
					 * wait a bit (an alternative would be to rendezvous with _resourceImpl[index]):
					 * Section 2.7.1 of the OTS spec says of the replay_completion operation on the RecoveryCoordinator:
					 * "This non-blocking operation returns the current status of the transaction"
					 */
					boolean ok = false;

					// wait enough time for the replay attempt on the resources
					for (int i = 0; i < 10; i++) {
						Thread.sleep(100);
						status = _resourceImpl[index].getStatus();

						if (((status == Status.StatusPrepared) && check_behaviors[index].allow_returned_prepared) ||
								((status == Status.StatusCommitting) && check_behaviors[index].allow_returned_committing) ||
								((status == Status.StatusCommitted) && check_behaviors[index].allow_returned_committed) ||
								((status == Status.StatusRolledBack) && check_behaviors[index].allow_returned_rolledback)) {
							ok = true;
							break;
						}
					}

					if (!ok) {
						correct = false;
						System.out.printf("AfterCrashServiceImpl01#check_oper correct=false%n");

						System.out.printf("REASON: %b %b %b %b (%d)%n",
								((status == Status.StatusPrepared) && check_behaviors[index].allow_returned_prepared),
								((status == Status.StatusCommitting) && check_behaviors[index].allow_returned_committing),
								((status == Status.StatusCommitted) && check_behaviors[index].allow_returned_committed),
								((status == Status.StatusRolledBack) && check_behaviors[index].allow_returned_rolledback),
								status.value());
					}
				}
				catch (NotPrepared notPrepared)
				{
					correct = correct && check_behaviors[index].allow_raised_not_prepared;
					System.err.println("AfterCrashServiceImpl02.check_oper [O" + _objectNumber + ".R" + index + "]: replay_completion raised NotPrepared");
				}
				catch (Exception exception)
				{
					System.err.println("AfterCrashServiceImpl02.check_oper [O" + _objectNumber + ".R" + index + "]:" + exception);
					exception.printStackTrace(System.err);
					correct = false;
				}
			}
		}

		return correct;
	}

	public boolean is_correct()
	{
		System.err.println("AfterCrashServiceImpl02.is_correct [O" + _objectNumber + "]: " + _isCorrect);

		return _isCorrect;
	}

	public ResourceTrace get_resource_trace(int resource_number)
	{
		ResourceTrace resourceTrace = ResourceTrace.ResourceTraceUnknown;

		if ((resource_number >= 0) && (resource_number < _resourceImpl.length))
		{
			resourceTrace = _resourceImpl[resource_number].getTrace();
		}

		System.err.println("AfterCrashServiceImpl02.get_resource_trace [O" + _objectNumber + ".R" + resource_number + "]: " + resourceTrace);

		return resourceTrace;
	}

	private int _serviceNumber;
	private int _objectNumber;
	private boolean _isCorrect = true;

	private ResourceImpl02[] _resourceImpl = null;
	private Resource[] _resource = null;
	private RecoveryCoordinator[] _recoveryCoordinator = null;
}
