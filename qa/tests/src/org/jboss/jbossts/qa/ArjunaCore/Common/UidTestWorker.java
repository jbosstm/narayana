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
package org.jboss.jbossts.qa.ArjunaCore.Common;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.utils.Utility;
import org.jboss.jbossts.qa.ArjunaCore.Utils.qautil;

/**
 * Simple test to see if Uid generation is unique.
 */
public class UidTestWorker
{
	/**
	 * This is a sub processso it may be difficult to return our results.
	 */
	public static void main(String[] args)
	{
		int mNumberOfUids = 0;
		try
		{
			mNumberOfUids = Integer.parseInt(args[0]);
		}
		catch (NumberFormatException mfe)
		{
			qautil.debug("error in uid worker using default value of 1000");
			mNumberOfUids = 1000;
		}

        System.err.println("start ms/pid: "+System.currentTimeMillis()+"/"+ Utility.getpid());

		for (int i = 0; i < mNumberOfUids; i++)
		{
			System.out.println(new Uid().toString());
		}

        // hold ownership of the pid socket for at least one second,
        // as that's the uniqness scope of the process init timestamp
        // in Uid. Anything less and another proc may grab the same port
        // within the same second and thus get same Uids.
        try {
            Thread.sleep(1200);
        } catch(InterruptedException e) {

        }

        System.err.println("end pid "+Utility.getpid());
	}
}
