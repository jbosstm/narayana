/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
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
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: DHThreadObject3b.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.orbspecific.resources;

import com.arjuna.ats.jts.OTSManager;

import com.hp.mwtests.ts.jts.utils.Util;
import com.hp.mwtests.ts.jts.TestModule.*;

import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;

import java.util.Random;
import java.lang.InterruptedException;

public class DHThreadObject3b extends Thread
{

    public DHThreadObject3b (int id)
    {
        _id = id;
    }

    public void run ()
    {
        for (int i = 0; i < 1000; i++)
        {
            DistributedHammerWorker3.randomOperation(_id, 0);
            Util.highProbYield();
        }
    }

    private int _id;
    
}

