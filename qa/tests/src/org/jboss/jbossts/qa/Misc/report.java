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
 * Copyright (C) 2003 by Arjuna Technologies Limited.
 *
 * Arjuna Technologies Limited
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: report.java,v 1.3 2004/07/12 10:26:09 kconner Exp $
 */
package org.jboss.jbossts.qa.Misc;

public class report
{
	public static void main(String[] args)
	{
		com.arjuna.ats.arjuna.Info aInfo;
		com.arjuna.ats.txoj.Info tInfo;
		com.arjuna.orbportability.Info oInfo;
		com.arjuna.ats.jts.Info jInfo;
		com.arjuna.ats.jdbc.Info dInfo;

		try
		{
			aInfo = new com.arjuna.ats.arjuna.Info();
			System.out.println(aInfo);

			tInfo = new com.arjuna.ats.txoj.Info();
			System.out.println(tInfo);

			oInfo = new com.arjuna.orbportability.Info();
			System.out.println(oInfo);

			jInfo = new com.arjuna.ats.jts.Info();
			System.out.println(jInfo);

			dInfo = new com.arjuna.ats.jdbc.Info();
			System.out.println(dInfo);

			System.out.println("Passed");
		}
		catch (Throwable t)
		{
			t.getMessage();
			t.printStackTrace();
			System.out.println("Failed");
		}
	}
}
