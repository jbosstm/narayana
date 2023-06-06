/*
 * SPDX short identifier: Apache-2.0
 */



package com.arjuna.ats.arjuna.state;

/**
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ByteBuffer.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 3.1.
 */

public interface ByteBuffer
{

    public byte [] getBytes ();
    
    public int size ();
    
}