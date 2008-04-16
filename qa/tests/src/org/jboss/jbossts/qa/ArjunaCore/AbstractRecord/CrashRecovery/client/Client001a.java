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
package org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client;

import com.arjuna.ats.arjuna.common.Uid;
import org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.impl.BasicAbstractRecord;
import org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.impl.RecoveryTransaction;
import org.jboss.jbossts.qa.ArjunaCore.Utils.BaseTestClient;
import org.jboss.jbossts.qa.Utils.ServerIORStore;

public class Client001a extends BaseTestClient
{
	public static void main(String[] args)
	{
		Client001a test = new Client001a(args);
	}

	private Client001a(String[] args)
	{
		super(args);
	}

	public void Test()
	{
		try
		{
			setNumberOfCalls(3);
			setNumberOfResources(2);
			setUniquePrefix(1);

			String txId = null;

			try
			{
				/*
				  for(int i = 0; i < mNumberOfResources; i++)
				  {
					  String s = ServerIORStore.loadIOR(getResourceName("resource_" + i));
					  ServerIORStore.removeIOR(getResourceName("resource_"+i));
					  if(s != null && !s.equals("restored"))
					  {
						  Debug("Error checking resource " + i + " value  = " + s);
						  mCorrect = false;
					  }
				  }
		  */

				txId = ServerIORStore.loadIOR("CrashAbstractRecord");
			}
			catch (Exception e)
			{
				Fail("Exception whilst checking resource", e);

				mCorrect = false;
			}

			if (mCorrect)
			{
				RecoveryTransaction tx = new RecoveryTransaction(new Uid(txId));
				BasicAbstractRecord bar = new BasicAbstractRecord();

				tx.doCommit();

				if (bar.getValue() == mMaxIteration * mNumberOfResources)
				{
					tx = new RecoveryTransaction(new Uid(txId));

					if (tx.activate())  // should generate a warning message
					{
						Debug("Error transaction log is still available!");

						mCorrect = false;
					}
				}
				else
				{
					Debug("Error restored state is " + bar.getValue());
				}
			}

			qaAssert(mCorrect);
		}
		catch (Exception e)
		{
			Fail("Error in Client001a.test() :", e);
		}
	}

}
