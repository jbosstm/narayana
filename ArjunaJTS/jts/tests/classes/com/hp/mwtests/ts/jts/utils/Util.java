/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.utils;

import org.jboss.logging.Logger;

import java.util.Random;

public class Util
{
    public static final Logger logger = Logger.getLogger("Util");

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
    
    public static void lowProbYield () {
        while ((rand.nextInt() % 2) != 0) {
            logger.trace("low Yielding");
            Thread.yield();
            logger.trace("Yielded");
        }
    }
    
    public static void highProbYield ()
    {
	while ((rand.nextInt() % 4) != 0) {
        logger.trace("high Yielding");
        Thread.yield();
        logger.trace("Yielded");
    }
    }
    
    public static Random rand = new Random();
    
}