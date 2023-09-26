/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


//

package org.jboss.jbossts.qa.CrashRecovery08Impls;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.CrashRecovery08.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.OTS;
import org.omg.CosTransactions.*;

public class ServiceImpl02 implements ServiceOperations
{
	public ServiceImpl02(int objectNumber)
	{
		_objectNumber = objectNumber;
	}

	public void setup_oper(Control ctrl, int number_of_resources)
	{
		_resourceImpl = new ResourceImpl01[number_of_resources];
		_resource = new Resource[number_of_resources];
		_recoveryCoordinator = new RecoveryCoordinator[number_of_resources];

		try
		{
			com.arjuna.ats.jts.ExplicitInterposition interposition = new com.arjuna.ats.jts.ExplicitInterposition();

			interposition.registerTransaction(ctrl);

			for (int index = 0; index < number_of_resources; index++)
			{
				_resourceImpl[index] = new ResourceImpl01(_objectNumber, index);
				ResourcePOATie servant = new ResourcePOATie(_resourceImpl[index]);

				OAInterface.objectIsReady(servant);
				_resource[index] = ResourceHelper.narrow(OAInterface.corbaReference(servant));

				_recoveryCoordinator[index] = OTS.current().get_control().get_coordinator().register_resource(_resource[index]);
			}

			interposition.unregisterTransaction();
		}
		catch (Exception exception)
		{
			System.err.println("ServiceImpl02.setup_oper: " + exception);
			exception.printStackTrace(System.err);
			_isCorrect = false;
		}
		catch (Error error)
		{
			System.err.println("ServiceImpl02.setup_oper: " + error);
			error.printStackTrace(System.err);
			_isCorrect = false;
		}
	}

	public boolean is_correct()
	{
		System.err.println("ServiceImpl02.is_correct: " + _isCorrect);

		return _isCorrect;
	}

	public ResourceTrace get_resource_trace(int resource_number)
	{
		ResourceTrace resourceTrace = ResourceTrace.ResourceTraceUnknown;

		if ((resource_number >= 0) && (resource_number < _resourceImpl.length))
		{
			resourceTrace = _resourceImpl[resource_number].getTrace();
		}

		System.err.println("ServiceImpl02.get_resource_trace [O" + _objectNumber + ".R" + resource_number + "]: " + resourceTrace.value());

		return resourceTrace;
	}

	private int _objectNumber;
	private boolean _isCorrect = true;

	private ResourceImpl01[] _resourceImpl = null;
	private Resource[] _resource = null;
	private RecoveryCoordinator[] _recoveryCoordinator = null;
}