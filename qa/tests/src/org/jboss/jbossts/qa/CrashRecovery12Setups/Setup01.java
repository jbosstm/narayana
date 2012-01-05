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
/*
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Setup01.java,v 1.1 2004/07/11 06:13:54 jcoleman Exp $
 */

package org.jboss.jbossts.qa.CrashRecovery12Setups;

import org.jboss.jbossts.qa.CrashRecovery12Clients.Client01;

import java.io.File;

public class Setup01
{
	public static void main(String[] args)
	{
		String resultsFile = Client01.resultsFile;
		boolean passed = false;

		if (args.length >= 1)
		{
			resultsFile = args[0];
		}

		try
		{
			File f = new File(resultsFile);
			f.delete();
			if (!f.exists())
			{
				passed = true;
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		if (passed)
		{
			System.out.println("Passed");
		}
		else
		{
			System.out.println("Failed");
		}

	}
}
