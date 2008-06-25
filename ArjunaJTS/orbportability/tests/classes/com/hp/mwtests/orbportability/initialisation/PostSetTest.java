/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: PostSetTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.orbportability.initialisation;

import org.jboss.dtf.testframework.unittest.Test;
import org.jboss.dtf.testframework.unittest.LocalHarness;
import com.arjuna.orbportability.internal.utils.PostSetLoader;
import com.arjuna.orbportability.ORB;
import com.hp.mwtests.orbportability.initialisation.postset.AllPostSet;
import com.hp.mwtests.orbportability.initialisation.postset.SinglePostSetUsingInterface;

import java.util.Properties;

public class PostSetTest extends Test
{
	private final static String ORB_INSTANCE_NAME = "PostSetTestORB";
    private final static String ORB_INSTANCE_NAME2 = "PostSetTestORB2";

	public void run(String[] args)
	{
		Properties testProps = System.getProperties();

		testProps.setProperty(PostSetLoader.generateORBPropertyName("com.arjuna.orbportability.orb"),
				"com.hp.mwtests.orbportability.initialisation.postset.AllPostSet");
		testProps.setProperty(PostSetLoader.generateORBPropertyName("com.arjuna.orbportability.orb", ORB_INSTANCE_NAME2),
				"com.hp.mwtests.orbportability.initialisation.postset.SinglePostSetUsingInterface");

		System.setProperties(testProps);

		ORB orb = null;
        ORB orb2 = null;

		try
		{
			orb = ORB.getInstance(ORB_INSTANCE_NAME);
			orb.initORB(args, null);

			orb2 = ORB.getInstance(ORB_INSTANCE_NAME2);

			orb2.setOrb(orb.orb());
		}
		catch (Exception e)
		{
			logInformation("ERROR - "+e);
			e.printStackTrace(System.err);
			assertFailure();
		}

		if ( !AllPostSet._called )
		{
			logInformation("Failed to call AllPostSet initialisation routine");
			assertFailure();
		}
		else
		{
			logInformation("AllPostSet called succeesfully");
		}

		if ( ( !SinglePostSetUsingInterface._called ) && ( SinglePostSetUsingInterface._passedObj == orb2 ) )
		{
			logInformation("Failed to call SinglePostSetUsingInterface initialisation routine");
			assertFailure();
		}
		else
		{
			logInformation("SinglePostSetUsingInterface called succeesfully");
		}

		orb.destroy();

		assertSuccess();
	}

	public static void main(String[] args)
	{
		PostSetTest pst = new PostSetTest();
		pst.initialise(null, null, args, new LocalHarness());
		pst.runTest();
	}

}
