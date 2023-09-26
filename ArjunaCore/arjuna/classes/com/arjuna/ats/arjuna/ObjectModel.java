/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna;

import java.io.PrintWriter;

/**
 * An enumeration of the types of object model supported. Based upon the model
 * type, certain optimisations may be used.
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ObjectModel.java 2342 2006-03-30 13:06:17Z $
 * @since JTS 1.0.
 */

public class ObjectModel
{

    /**
     * In the SINGLE model, it is assumed that only a single instance of the
     * object will exist within a single JVM, and that it will be shared between
     * threads.
     */

    public static final int SINGLE = 0;

    /**
     * In the MULTIPLE model, it is assumed that multiple instances of the
     * object may exist in different JVMs concurrently, or within the same
     * JVM if each thread gets its own instance.
     */

    public static final int MULTIPLE = 1;

    public static String stringForm (int os)
    {
        switch (os)
        {
        case SINGLE:
            return "SINGLE";
        case MULTIPLE:
            return "MULTIPLE";
        default:
            return "Unknown";
        }
    }
    
    /**
     * Print out a human-readable form of the model type.
     */

    public static void print (PrintWriter strm, int os)
    {
        strm.print(stringForm(os));
    }

}