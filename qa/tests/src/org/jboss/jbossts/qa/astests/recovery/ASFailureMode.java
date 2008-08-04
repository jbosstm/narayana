package org.jboss.jbossts.qa.astests.recovery;

import java.io.Serializable;

/**
 * Specification of what to do when a failure is injected
 */
public enum ASFailureMode implements Serializable
{
    NONE(false)
    
    ,HALT(true)    // halt the JVM
    ,EXIT(true)   // exit the JVM
    ,SUSPEND(false)  // suspend the calling thread
    ,XAEXCEPTION(false)    // fail via one of the xa exception codes
    ;

    private boolean willTerminateVM;

    ASFailureMode(boolean willTerminateVM)
    {
        this.willTerminateVM = willTerminateVM;
    }

    public boolean willTerminateVM()
    {
        return willTerminateVM;
    }

    public static ASFailureMode toEnum(String mode)
    {
        return ASFailureMode.valueOf(mode.toUpperCase());
    }
}
