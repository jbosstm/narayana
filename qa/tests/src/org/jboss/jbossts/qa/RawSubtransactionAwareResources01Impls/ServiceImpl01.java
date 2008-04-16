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
// $Id: ServiceImpl01.java,v 1.2 2003/06/26 11:45:01 rbegg Exp $
//

package org.jboss.jbossts.qa.RawSubtransactionAwareResources01Impls;

/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ServiceImpl01.java,v 1.2 2003/06/26 11:45:01 rbegg Exp $
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
 * $Id: ServiceImpl01.java,v 1.2 2003/06/26 11:45:01 rbegg Exp $
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
