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

package org.jboss.jbossts.qa.Utils;

public class JVMStats
{
	public static long getMemory()
	{
		Runtime runtime = Runtime.getRuntime();

		long presentMemory = (runtime.totalMemory() - runtime.freeMemory());
		long memory = Long.MAX_VALUE;

		while (presentMemory < memory)
		{
			memory = presentMemory;

            // no clean way to to this at present, so we'll sleep and hope the gc runs.
            System.gc();
            try {
                Thread.sleep(100);
            } catch(InterruptedException e) {

            }

			presentMemory = (runtime.totalMemory() - runtime.freeMemory());
		}

		return memory;
	}
}
