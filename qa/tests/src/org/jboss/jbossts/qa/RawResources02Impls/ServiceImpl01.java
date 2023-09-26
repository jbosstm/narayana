/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


//

package org.jboss.jbossts.qa.RawResources02Impls;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.RawResources02.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.Resource;
import org.omg.CosTransactions.ResourceHelper;
import org.omg.CosTransactions.ResourcePOATie;

public class ServiceImpl01 implements ServiceOperations
{
	public ServiceImpl01(int objectNumber)
	{
		_objectNumber = objectNumber;
	}

	public void oper(ResourceBehavior[] resource_behaviors, Control ctrl)
	{
		_resourceImpl = new ResourceImpl01[resource_behaviors.length];
		_resource = new Resource[resource_behaviors.length];

		for (int index = 0; index < resource_behaviors.length; index++)
		{
			try
			{
				_resourceImpl[index] = new ResourceImpl01(_objectNumber, index, resource_behaviors[index]);
				ResourcePOATie servant = new ResourcePOATie(_resourceImpl[index]);

				OAInterface.objectIsReady(servant);
				_resource[index] = ResourceHelper.narrow(OAInterface.corbaReference(servant));

				ctrl.get_coordinator().register_resource(_resource[index]);
			}
			catch (Exception exception)
			{
				System.err.println("ImplicitObjectImpl01.oper: " + exception);
				exception.printStackTrace(System.err);
				_isCorrect = false;
			}
		}
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
}