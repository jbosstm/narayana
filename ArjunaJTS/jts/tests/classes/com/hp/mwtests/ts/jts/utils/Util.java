/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Util.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.utils;

import java.lang.Thread;
import java.util.Random;

public class Util
{

    public static void indent (char thr, int level)
    {
	System.out.print(thr+" ");
	
	for (int i = 0; i < level; i++)
	    System.out.print(" ");
    }

    public static void indent (int thr, int level)
    {
	System.out.print(thr+" ");
	
	for (int i = 0; i < level; i++)
	    System.out.print(" ");
    }    
    
    public static void lowProbYield ()
    {
	while ((rand.nextInt() % 2) != 0)
	    Thread.yield();
    }
    
    public static void highProbYield ()
    {
	while ((rand.nextInt() % 4) != 0)
	    Thread.yield();
    }
    
    public static Random rand = new Random();
    
}
