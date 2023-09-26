/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wscf.common;

/**
 * Implementations of this interface provide globally unique identifications
 * for coordinators. The may use the activity id but need not do so.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: CoordinatorId.java,v 1.2 2003/01/07 10:33:34 nmcl Exp $
 * @since 1.0.
 */

public interface CoordinatorId
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

    /**
     * @return string form.
     */

    public String toString ();
    
}