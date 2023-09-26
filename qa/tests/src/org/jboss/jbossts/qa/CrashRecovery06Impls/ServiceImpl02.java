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
import org.jboss.jbossts.qa.Utils.OTS;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CosTransactions.*;

public class ServiceImpl02 implements ServiceOperations
{
	public ServiceImpl02()
	{
	}

	public void setup_oper(Control ctrl)
	{
		try
		{
			com.arjuna.ats.jts.ExplicitInterposition interposition = new com.arjuna.ats.jts.ExplicitInterposition();

			interposition.registerTransaction(ctrl);

			_resourceImpl = new ResourceImpl01();
			ResourcePOATie servant = new ResourcePOATie(_resourceImpl);

			OAInterface.objectIsReady(servant);
			_resource = ResourceHelper.narrow(OAInterface.corbaReference(servant));

			_recoveryCoordinator = OTS.current().get_control().get_coordinator().register_resource(_resource);

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
			System.err.println("ServiceImpl02.check_oper: " + exception);
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