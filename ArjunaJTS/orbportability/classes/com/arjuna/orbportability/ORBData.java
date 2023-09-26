/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.orbportability;

/**
 * ORB specific data objects implement this interface to provide
 * runtime information about the ORB.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ORBData.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.0.
 */

public interface ORBData
{
    /**
     * Retrieve the XML fragment which contains runtime information about the orb.
     *
     * @return A string containing the XML fragment.
     */
    public String getORBdata();
}