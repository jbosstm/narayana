/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


//

package org.jboss.jbossts.qa.CrashRecovery06Impls;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.CrashRecovery06.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CosTransactions.*;

public class ServiceImpl01 implements ServiceOperations
{
	public ServiceImpl01()
	{
	}

	public void setup_oper(Control ctrl)
	{
		try
		{
			_resourceImpl = new ResourceImpl01();
			ResourcePOATie servant = new ResourcePOATie(_resourceImpl);

			OAInterface.objectIsReady(servant);
			_resource = ResourceHelper.narrow(OAInterface.corbaReference(servant));

			_recoveryCoordinator = ctrl.get_coordinator().register_resource(_resource);
		}
		catch (Exception exception)
		{
			System.err.println("ServiceImpl01.setup_oper: " + exception);
			exception.printStackTrace(System.err);
			_isCorrect = false;
		}
	}

	public boolean check_oper()
	{
		boolean correct = true;

		try
		{
			Status status = _recoveryCoordinator.replay_completion(null);
		}
		catch (OBJECT_NOT_EXIST objectNotExist)
		{
		}
		catch (Exception exception)
		{
			System.err.println("ServiceImpl01.check_oper: " + exception);
			exception.printStackTrace(System.err);
			correct = false;
		}

		return correct;
	}

	public boolean is_correct()
	{
		return _isCorrect;
	}

	private boolean _isCorrect = true;

	private ResourceImpl01 _resourceImpl = null;
	private Resource _resource = null;
	private RecoveryCoordinator _recoveryCoordinator = null;
}