/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


//

package org.jboss.jbossts.qa.CrashRecovery04Impls;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.CrashRecovery04.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CosTransactions.*;

public class ServiceImpl03 implements ServiceOperations
{
	public ServiceImpl03(int objectNumber)
	{
		_objectNumber = objectNumber;
	}

	public void setup_oper(Control ctrl, int number_of_resources)
	{
		_resourceImpl = new ResourceImpl01[number_of_resources];
		_resource = new Resource[number_of_resources];
		_recoveryCoordinator = new RecoveryCoordinator[number_of_resources];

		for (int index = 0; index < number_of_resources; index++)
		{
			try
			{
				_resourceImpl[index] = new ResourceImpl01(_objectNumber, index);
				ResourcePOATie servant = new ResourcePOATie(_resourceImpl[index]);

				OAInterface.objectIsReady(servant);
				_resource[index] = ResourceHelper.narrow(OAInterface.corbaReference(servant));

				_recoveryCoordinator[index] = ctrl.get_coordinator().register_resource(_resource[index]);
			}
			catch (Exception exception)
			{
				System.err.println("ServiceImpl03.setup_oper: " + exception);
				exception.printStackTrace(System.err);
				_isCorrect = false;
			}
		}
	}

	public boolean check_oper()
	{
		boolean correct = true;

		for (int index = 0; index < _recoveryCoordinator.length; index++)
		{
			try
			{
				Status status = _recoveryCoordinator[index].replay_completion(_resource[index]);
				correct = correct && (status == Status.StatusRolledBack);
			}
			catch (OBJECT_NOT_EXIST objectNotExist)
			{
			}
			catch (Exception exception)
			{
				System.err.println("ServiceImpl03.check_oper: " + exception);
				exception.printStackTrace(System.err);
				correct = false;
			}
		}

		return correct;
	}

	public boolean is_correct()
	{
		return _isCorrect;
	}

	public ResourceTrace get_resource_trace(int resource_number)
	{
		if ((resource_number < 0) || (resource_number >= _resourceImpl.length))
		{
			return ResourceTrace.ResourceTraceUnknown;
		}
		else
		{
			return _resourceImpl[resource_number].getTrace();
		}
	}

	private int _objectNumber;
	private boolean _isCorrect = true;

	private ResourceImpl01[] _resourceImpl = null;
	private Resource[] _resource = null;
	private RecoveryCoordinator[] _recoveryCoordinator = null;
}