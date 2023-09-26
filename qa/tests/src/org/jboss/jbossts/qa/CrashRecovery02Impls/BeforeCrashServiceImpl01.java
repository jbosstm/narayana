/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


//

package org.jboss.jbossts.qa.CrashRecovery02Impls;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.CrashRecovery02.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.OTS;
import org.jboss.jbossts.qa.Utils.ServerIORStore;
import org.omg.CosTransactions.RecoveryCoordinator;
import org.omg.CosTransactions.Resource;
import org.omg.CosTransactions.ResourceHelper;
import org.omg.CosTransactions.ResourcePOATie;

public class BeforeCrashServiceImpl01 implements BeforeCrashServiceOperations
{
	public BeforeCrashServiceImpl01(int serviceNumber, int objectNumber)
	{
		System.out.println("BeforeCrashServiceImpl01(" + serviceNumber + ", " + objectNumber + ")");
		_serviceNumber = serviceNumber;
		_objectNumber = objectNumber;
	}

	public void setup_oper(ResourceBehavior[] resource_behaviors)
	{
		ResourceImpl01[] resourceImpl = new ResourceImpl01[resource_behaviors.length];
		Resource[] resource = new Resource[resource_behaviors.length];
		RecoveryCoordinator[] recoveryCoordinator = new RecoveryCoordinator[resource_behaviors.length];

		for (int index = 0; index < resource_behaviors.length; index++)
		{
			try
			{
				resourceImpl[index] = new ResourceImpl01(_serviceNumber, _objectNumber, index, resource_behaviors[index]);
				ResourcePOATie servant = new ResourcePOATie(resourceImpl[index]);

				OAInterface.objectIsReady(servant);
				resource[index] = ResourceHelper.narrow(OAInterface.corbaReference(servant));

				recoveryCoordinator[index] = OTS.current().get_control().get_coordinator().register_resource(resource[index]);

				System.out.println("BeforeCrashServiceImpl01: storing IOR \"RecoveryCoordinator_" + _serviceNumber + "_" + _objectNumber + "_" + index + "\"");
				ServerIORStore.storeIOR("RecoveryCoordinator_" + _serviceNumber + "_" + _objectNumber + "_" + index, ORBInterface.orb().object_to_string(recoveryCoordinator[index]));
			}
			catch (Exception exception)
			{
				System.err.println("BeforeCrashServiceImpl01.setup_oper: " + exception);
				exception.printStackTrace(System.err);
				_isCorrect = false;
			}
		}
	}

	public boolean is_correct()
	{
		System.err.println("BeforeCrashServiceImpl01.is_correct: " + _isCorrect);

		return _isCorrect;
	}

	private int _serviceNumber;
	private int _objectNumber;
	private boolean _isCorrect = true;
}