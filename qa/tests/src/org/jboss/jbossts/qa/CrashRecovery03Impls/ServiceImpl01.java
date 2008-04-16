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
// $Id: ServiceImpl01.java,v 1.2 2003/06/26 11:43:26 rbegg Exp $
//

package org.jboss.jbossts.qa.CrashRecovery03Impls;

/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ServiceImpl01.java,v 1.2 2003/06/26 11:43:26 rbegg Exp $
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
 * $Id: ServiceImpl01.java,v 1.2 2003/06/26 11:43:26 rbegg Exp $
 */


import org.jboss.jbossts.qa.CrashRecovery03.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.OTS;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CosTransactions.*;

public class ServiceImpl01 implements ServiceOperations
{
	public ServiceImpl01()
	{
	}

	public void setup_oper()
	{
		try
		{
			_resourceImpl = new ResourceImpl01();
			ResourcePOATie servant = new ResourcePOATie(_resourceImpl);

			OAInterface.objectIsReady(servant);
			_resource = ResourceHelper.narrow(OAInterface.corbaReference(servant));

			_recoveryCoordinator = OTS.current().get_control().get_coordinator().register_resource(_resource);
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
