/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.jts.tools;

import javax.transaction.xa.XAException;
import java.io.Serializable;

/**
 * An ASFailureSpec is for defining different ways of generating
 * failures and essentially consists of a mode and type.
 *
 * If you need to generate new kinds of failure you should
 * modify the ASFailureMode and ASFailureType classes. Your
 * test will be given a reference to these specifications and is
 * responsible for interpreting their meaning.
 *
 * @author Mike Musgrove
 */
/**
 * @deprecated as of 5.0.5.Final In a subsequent release we will change packages names in order to 
 * provide a better separation between public and internal classes.
 */
@Deprecated // in order to provide a better separation between public and internal classes.
public class XAFailureSpec implements Serializable
{
    String name;
    XAFailureMode mode;
    XAFailureType type;
    String modeArg;
    int recoveryArg;

    public XAFailureSpec()
    {
        mode = XAFailureMode.NONE;
        type = XAFailureType.NONE;
    }

    public XAFailureSpec(String name, XAFailureMode mode, XAFailureType type, String modeArg, int recoveryArg)
    {
        this.name = name;
        this.mode = mode;
        this.type = type;
        this.modeArg = modeArg;
        this.recoveryArg = recoveryArg;
    }

    public boolean willTerminateVM()
    {
        return mode.willTerminateVM();
    }

    public XAFailureMode getMode()
    {
        return mode;
    }

    public XAFailureType getType()
    {
        return type;
    }

    public String getModeArg()
    {
        return modeArg;
    }

    public int getRecoveryArg()
    {
        return recoveryArg;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setMode(String mode)
    {
        this.mode = XAFailureMode.valueOf(mode);
    }

    public void setType(String type)
    {
        this.type = XAFailureType.valueOf(type);
    }

    public void setModeArg(String modeArg)
    {
        this.modeArg = modeArg;
    }

    public void setRecoveryArg(int recoveryArg)
    {
        this.recoveryArg = recoveryArg;
    }

    public String toString()
    {
        return new StringBuilder().append(mode).append(',').append(type).append(',').append(modeArg).toString();
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof XAFailureSpec)) return false;

        XAFailureSpec that = (XAFailureSpec) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    public int hashCode()
    {
        return (name != null ? name.hashCode() : 0);
    }

    public static boolean isHeuristic(int exType) {
        switch (exType) {
            case XAException.XA_HEURCOM:
            case XAException.XA_HEURHAZ:
            case XAException.XA_HEURMIX:
            case XAException.XA_HEURRB:
                return true;
            default:
                return false;
        }
    }

    public static boolean isHeuristic(String exName) {
        return isHeuristic(XAFailureResource.getXAExceptionType(exName).errorCode);
    }

    public static boolean compatibleHeuristics(int xaCode1, int xaCode2) {
        return (XAFailureSpec.isHeuristic(xaCode1) && XAFailureSpec.isHeuristic(xaCode2));
    }

    public static boolean isExpected(int thrownType, String injectedType) {
        XAException xae = XAFailureResource.getXAExceptionType(injectedType);

        if (new XAException(thrownType).getClass().equals(injectedType.getClass()))
            return true;

        if (isHeuristic(xae.errorCode) && isHeuristic(thrownType))
            return true;

        return false;
    }
}