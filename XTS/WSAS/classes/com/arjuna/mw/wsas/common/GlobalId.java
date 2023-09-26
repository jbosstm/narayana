/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wsas.common;

/**
 * Implementations of this interface provide globally unique identifications
 * for activities.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: GlobalId.java,v 1.1 2002/11/25 10:51:41 nmcl Exp $
 * @since 1.0.
 */

public interface GlobalId
{

    /**
     * Two instances are identical if their targets are the same.
     */

    public boolean equals (Object obj);
    
    /**
     * @return the byte stream representing this instance.
     */

    public byte[] value ();
    
    /**
     * @return <code>true</code> if this instance is valid, <code>false</code>
     * otherwise.
     */

    public boolean valid ();

}