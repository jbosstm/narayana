/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.jts.tools;

import java.io.Serializable;

/**
 * Specification of what to do when a failure is injected
 *
 * @author Mike Musgrove
 */
/**
 * @deprecated as of 5.0.5.Final In a subsequent release we will change packages names in order to 
 * provide a better separation between public and internal classes.
 */
@Deprecated // in order to provide a better separation between public and internal classes.
public enum XAFailureMode implements Serializable
{
    NONE(false)
    
    ,HALT(true)    // halt the JVM
    ,EXIT(true)   // exit the JVM
    ,SUSPEND(false)  // suspend the calling thread
    ,XAEXCEPTION(false)    // fail via one of the xa exception codes
    ;

    private boolean willTerminateVM;

    XAFailureMode(boolean willTerminateVM)
    {
        this.willTerminateVM = willTerminateVM;
    }

    public boolean willTerminateVM()
    {
        return willTerminateVM;
    }

    public static XAFailureMode toEnum(String mode)
    {
        return XAFailureMode.valueOf(mode.toUpperCase());
    }
}