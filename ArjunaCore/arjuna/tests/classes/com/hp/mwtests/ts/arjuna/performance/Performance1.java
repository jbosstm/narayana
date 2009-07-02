/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Performance1.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.arjuna.performance;

import com.arjuna.ats.arjuna.AtomicAction;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

public class Performance1
{
    @Test
    public void test()
    {
        int numberOfTransactions = 1000;
        long stime = Calendar.getInstance().getTime().getTime();

        for (int i = 0; i < numberOfTransactions; i++) {
            AtomicAction A = new AtomicAction();

            A.begin();
            A.abort();
        }

        long ftime = Calendar.getInstance().getTime().getTime();
        long timeTaken = ftime - stime;

        System.out.println("time for " + numberOfTransactions + " write transactions is " + timeTaken);
        System.out.println("number of transactions: " + numberOfTransactions);
        System.out.println("throughput: " + (float) (numberOfTransactions / (timeTaken / 1000.0)));
    }

}
