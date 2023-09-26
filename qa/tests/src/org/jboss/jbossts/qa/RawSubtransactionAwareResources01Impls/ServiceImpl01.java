/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


//

package org.jboss.jbossts.qa.RawSubtransactionAwareResources01Impls;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.RawSubtransactionAwareResources01.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.OTS;
import org.omg.CosTransactions.SubtransactionAwareResource;
import org.omg.CosTransactions.SubtransactionAwareResourceHelper;
import org.omg.CosTransactions.SubtransactionAwareResourcePOATie;

public class ServiceImpl01 implements ServiceOperations
{
	public ServiceImpl01(int objectNumber)
	{
		_objectNumber = objectNumber;
	}

	public void oper(int subtransactionAwareResourceNumber)
	{
		_subtransactionAwareResourceImpl = new SubtransactionAwareResourceImpl01[subtransactionAwareResourceNumber];
		_subtransactionAwareResource = new SubtransactionAwareResource[subtransactionAwareResourceNumber];

		for (int index = 0; index < subtransactionAwareResourceNumber; index++)
		{
			System.err.println("ServiceImpl01.oper [O" + _objectNumber + ".R" + index + "]");

			try
			{
				_subtransactionAwareResourceImpl[index] = new SubtransactionAwareResourceImpl01(_objectNumber, index);
				SubtransactionAwareResourcePOATie servant = new SubtransactionAwareResourcePOATie(_subtransactionAwareResourceImpl[index]);

				OAInterface.objectIsReady(servant);
				_subtransactionAwareResource[index] = SubtransactionAwareResourceHelper.narrow(OAInterface.corbaReference(servant));

				OTS.current().get_control().get_coordinator().register_subtran_aware(_subtransactionAwareResource[index]);
			}
			catch (Exception exception)
			{
				System.err.println("ServiceImpl01.oper: " + exception);
				exception.printStackTrace(System.err);
				_isCorrect = false;
			}
		}
	}

	public boolean is_correct()
	{
		System.err.println("ServiceImpl01.is_correct [O" + _objectNumber + "]: " + _isCorrect);

		return _isCorrect;
	}

	public SubtransactionAwareResourceTrace get_subtransaction_aware_resource_trace(int subtransactionAwareResourceNumber)
	{
		SubtransactionAwareResourceTrace subtransactionAwareResourceTrace = SubtransactionAwareResourceTrace.SubtransactionAwareResourceTraceUnknown;

		if ((subtransactionAwareResourceNumber >= 0) && (subtransactionAwareResourceNumber < _subtransactionAwareResourceImpl.length))
		{
			subtransactionAwareResourceTrace = _subtransactionAwareResourceImpl[subtransactionAwareResourceNumber].getTrace();
		}

		System.err.println("ServiceImpl01.get_subtransaction_aware_resource_trace [O" + _objectNumber + ".R" + subtransactionAwareResourceNumber + "]: " + subtransactionAwareResourceTrace);

		return subtransactionAwareResourceTrace;
	}

	private int _objectNumber;
	private boolean _isCorrect = true;

	private SubtransactionAwareResourceImpl01[] _subtransactionAwareResourceImpl = null;
	private SubtransactionAwareResource[] _subtransactionAwareResource = null;
}