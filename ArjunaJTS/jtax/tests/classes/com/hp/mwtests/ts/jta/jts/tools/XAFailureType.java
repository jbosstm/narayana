/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.jts.tools;

import java.io.Serializable;

/**
 * Specification of when to inject a failure
 *
 * @author Mike Musgrove
 */
/**
 * @deprecated as of 5.0.5.Final In a subsequent release we will change packages names in order to 
 * provide a better separation between public and internal classes.
 */
@Deprecated // in order to provide a better separation between public and internal classes.
public enum XAFailureType implements Serializable
{
    NONE

    ,PRE_PREPARE // do something before prepare is called

    ,XARES_START    // failures specific to the XA protocol
    ,XARES_END
    ,XARES_PREPARE
    ,XARES_ROLLBACK
    ,XARES_COMMIT
    ,XARES_RECOVER
    ,XARES_FORGET

    ,SYNCH_BEFORE   // do something before completion
    ,SYNCH_AFTER
    ;
    
    public static XAFailureType toEnum(String type)
    {
        return XAFailureType.valueOf(type.toUpperCase());
    }

    public boolean isXA()
    {
        return name().startsWith("XARES");
    }

    public boolean isSynchronization()
    {
        return name().startsWith("SYNCH");
    }

    public boolean isPreCommit()
    {
        return equals(PRE_PREPARE);
    }
}