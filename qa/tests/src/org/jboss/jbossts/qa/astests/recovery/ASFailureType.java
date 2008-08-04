package org.jboss.jbossts.qa.astests.recovery;

import java.io.Serializable;

/**
 * Specification of when to inject a failure
 */
public enum ASFailureType implements Serializable
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
    
    public static ASFailureType toEnum(String type)
    {
        return ASFailureType.valueOf(type.toUpperCase());
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
