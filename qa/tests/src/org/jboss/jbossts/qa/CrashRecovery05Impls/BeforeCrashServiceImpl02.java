/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


//

package org.jboss.jbossts.qa.CrashRecovery05Impls;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.CrashRecovery05.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.OTS;
import org.jboss.jbossts.qa.Utils.ServerIORStore;
import org.omg.CosTransactions.*;

public class BeforeCrashServiceImpl02 implements BeforeCrashServiceOperations
{
	public BeforeCrashServiceImpl02(int serviceNumber, int objectNumber)
	{
		System.out.println("BeforeCrashServiceImpl02(" + serviceNumber + ", " + objectNumber + ")");
		_serviceNumber = serviceNumber;
		_objectNumber = objectNumber;
	}

	public void setup_oper(Control ctrl, ResourceBehavior[] resource_behaviors)
	{
		ResourceImpl01[] resourceImpl = new ResourceImpl01[resource_behaviors.length];
		Resource[] resource = new Resource[resource_behaviors.length];
		RecoveryCoordinator[] recoveryCoordinator = new RecoveryCoordinator[resource_behaviors.length];

		try
		{
			com.arjuna.ats.jts.ExplicitInterposition interposition = new com.arjuna.ats.jts.ExplicitInterposition();

			interposition.registerTransaction(ctrl);

			for (int index = 0; index < resource_behaviors.length; index++)
			{
				resourceImpl[index] = new ResourceImpl01(_serviceNumber, _objectNumber, index, resource_behaviors[index]);
				ResourcePOATie servant = new ResourcePOATie(resourceImpl[index]);

				OAInterface.objectIsReady(servant);
				resource[index] = ResourceHelper.narrow(OAInterface.corbaReference(servant));

				recoveryCoordinator[index] = OTS.current().get_control().get_coordinator().register_resource(resource[index]);

				System.out.println("BeforeCrashServiceImpl02: storing IOR \"RecoveryCoordinator_" + _serviceNumber + "_" + _objectNumber + "_" + index + "\"");
				ServerIORStore.storeIOR("RecoveryCoordinator_" + _serviceNumber + "_" + _objectNumber + "_" + index, ORBInterface.orb().object_to_string(recoveryCoordinator[index]));
			}

			interposition.unregisterTransaction();
		}
		catch (Exception exception)
		{
			System.err.println("BeforeCrashServiceImpl02.setup_oper: " + exception);
			exception.printStackTrace(System.err);
			_isCorrect = false;
		}
		catch (Error error)
		{
			System.err.println("BeforeCrashServiceImpl02.setup_oper: " + error);
			error.printStackTrace(System.err);
			_isCorrect = false;
		}
	}

	public boolean is_correct()
	{
		System.err.println("BeforeCrashServiceImpl02.is_correct: " + _isCorrect);

		return _isCorrect;
	}

	private int _serviceNumber;
	private int _objectNumber;
	private boolean _isCorrect = true;
}