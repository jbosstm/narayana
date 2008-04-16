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

package org.jboss.jbossts.qa.JDBCResources01Clients;

import org.jboss.jbossts.qa.JDBCResources01.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.OTS;
import org.jboss.jbossts.qa.Utils.ServerIORStore;

public class Client17
{
	public static void main(String[] args)
	{
		//set test value to true
		boolean correct = true;
		InfoTable infoTable1 = null;
		InfoTable infoTable2 = null;
		try
		{
			try
			{
				ORBInterface.initORB(args, null);
				OAInterface.initOA();

				//create remote object 1 (one connection for all the test)
				String infoTableIOR1 = ServerIORStore.loadIOR(args[args.length - 2]);
				infoTable1 = InfoTableHelper.narrow(ORBInterface.orb().string_to_object(infoTableIOR1));

				//create remote object 2 (one connection per call)
				String infoTableIOR2 = ServerIORStore.loadIOR(args[args.length - 1]);
				infoTable2 = InfoTableHelper.narrow(ORBInterface.orb().string_to_object(infoTableIOR2));
			}
			catch (Exception exception)
			{
				correct = false;
				System.err.println("error in createing remote objects");
				exception.printStackTrace(System.err);
			}

//-----------------------------------------------------------------------------------------------------------------

			//first do insert without transaction on table 1
			System.err.println("doing inserts on table 1 no-tx");
			try
			{
				for (int index = 0; correct && index < 10; index++)
				{
					String name = "Name_" + index;
					String value = "Value_" + index;

					infoTable1.insert(name, value);
				}
			}
			catch (Exception exception)
			{
				correct = false;
				System.err.println("error in insert no-tx table 1");
				exception.printStackTrace(System.err);
			}

			//now try the same on table 2
			System.err.println("doing inserts on table 2 no-tx");
			try
			{
				for (int index = 0; correct && index < 10; index++)
				{
					String name = "Name_" + index;
					String value = "Value_" + index;

					infoTable2.insert(name, value);
				}
			}
			catch (Exception exception)
			{
				correct = false;
				System.err.println("error in insert no tx");
				exception.printStackTrace(System.err);
			}

//-----------------------------------------------------------------------------------------------------------------

			//now try more inserts using a single transaction per call
			System.err.println("doing inserts on table 1 single call per tx");
			try
			{
				for (int index = 10; correct && index < 20; index++)
				{
					OTS.current().begin();
					String name = "Name_" + index;
					String value = "Value_" + index;

					infoTable1.insert(name, value);
					OTS.current().commit(true);
				}
			}
			catch (Exception exception)
			{
				correct = false;
				System.err.println("error in running single tx on table 1");
				exception.printStackTrace(System.err);
			}

			System.err.println("doing inserts on table 2 single  call per tx");
			try
			{

				for (int index = 10; correct && index < 20; index++)
				{
					OTS.current().begin();
					String name = "Name_" + index;
					String value = "Value_" + index;

					infoTable2.insert(name, value);
					OTS.current().commit(true);
				}

			}
			catch (Exception exception)
			{
				correct = false;
				System.err.println("error in running single tx on table 2");
				exception.printStackTrace(System.err);
			}

//-----------------------------------------------------------------------------------------------------------------

			//now try more inserts using a single transaction across many jdbc calls
			System.err.println("doing inserts on table 1 multi call per tx");
			try
			{
				OTS.current().begin();
				for (int index = 20; correct && index < 30; index++)
				{
					String name = "Name_" + index;
					String value = "Value_" + index;

					infoTable1.insert(name, value);
				}
				OTS.current().commit(true);
			}
			catch (Exception exception)
			{
				correct = false;
				System.err.println("error in running single tx on table 1");
				exception.printStackTrace(System.err);
			}

			System.err.println("doing inserts on table 1 multi call per tx");
			try
			{
				OTS.current().begin();
				for (int index = 20; correct && index < 30; index++)
				{
					String name = "Name_" + index;
					String value = "Value_" + index;

					infoTable2.insert(name, value);
				}
				OTS.current().commit(true);
			}
			catch (Exception exception)
			{
				correct = false;
				System.err.println("error in running single tx on table 2");
				exception.printStackTrace(System.err);
			}

//-----------------------------------------------------------------------------------------------------------------

			//now try doing inserts on both tables during the transaction
			System.err.println("Starting two phse tests");
			try
			{
				for (int index = 30; correct && index < 40; index++)
				{
					OTS.current().begin();
					String name = "Name_" + index;
					String value = "Value_" + index;

					infoTable1.insert(name, value);
					infoTable2.insert(name, value);
					OTS.current().commit(true);
				}

			}
			catch (Exception exception)
			{
				correct = false;
				System.err.println("error in running single tx per call on both tables");
				exception.printStackTrace(System.err);
			}

			System.err.println("two phase test with a single transaction");
			try
			{
				OTS.current().begin();
				for (int index = 40; correct && index < 50; index++)
				{
					String name = "Name_" + index;
					String value = "Value_" + index;

					infoTable1.insert(name, value);
					infoTable2.insert(name, value);
				}
				OTS.current().commit(true);

			}
			catch (Exception exception)
			{
				correct = false;
				System.err.println("error in running single tx per call on both tables");
				exception.printStackTrace(System.err);
			}

//-----------------------------------------------------------------------------------------------------------------

/**
 * We have done most of the test that we need to so lets reduce the size of the table
 * and check that the transaction is being used by rollingback some of our work.
 *
 */
			System.err.println("two phase delete");
			try
			{
				OTS.current().begin();
				for (int index = 10; correct && index < 50; index++)
				{
					String name = "Name_" + index;

					infoTable1.delete(name);
					infoTable2.delete(name);
				}
				//commit false to test if this makes a difference.
				OTS.current().commit(false);
			}
			catch (Exception exception)
			{
				correct = false;
				System.err.println("error in deleteing extra rows");
				exception.printStackTrace(System.err);
			}

//-----------------------------------------------------------------------------------------------------------------

			System.err.println("rollback tests");
			try
			{
				for (int index = 0; correct && index < 10; index++)
				{
					OTS.current().begin();
					String name = "Name_" + index;
					String value = "Value_" + (9 - index);
					try
					{
						infoTable1.update(name, value);
						infoTable2.update(name, value);
					}
					catch (Exception e)
					{
						correct = false;
						System.err.println("Error in update : " + e);
						e.printStackTrace(System.err);
					}
					OTS.current().rollback();
				}
			}
			catch (Exception exception)
			{
				correct = false;
				System.err.println("error in deleteing extra rows");
				exception.printStackTrace(System.err);
			}
		}
		catch (Exception exception)
		{
			correct = false;
			System.err.println("Client17.main: " + exception);
			exception.printStackTrace(System.err);
		}
		finally
		{
// code change to stop database locking
			try
			{
				if (OTS.current().get_control() != null)
				{
					OTS.current().rollback();
				}
			}
			catch (Exception e)
			{
				System.err.println("Finally has caught exception");
				e.printStackTrace(System.err);
			}
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Client17.main: " + exception);
			exception.printStackTrace(System.err);
		}

		if (correct)
		{
			System.out.println("Passed");
		}
		else
		{
			System.out.println("Failed");
		}
	}
}
