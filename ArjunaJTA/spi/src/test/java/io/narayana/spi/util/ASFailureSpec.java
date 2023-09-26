/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package io.narayana.spi.util;

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
 */
public class ASFailureSpec implements Serializable
{
    private String name;
    private ASFailureMode mode;
    private String modeArg;
    private ASFailureType type;

    public ASFailureSpec()
    {
        mode = ASFailureMode.NONE;
        type = ASFailureType.NONE;
    }

    public ASFailureSpec(String name, ASFailureMode mode, String modeArg, ASFailureType type)
    {
        this.name = name;
        this.mode = mode;
        this.modeArg = modeArg;
        this.type = type;
    }

    public boolean willTerminateVM()
    {
        return mode.willTerminateVM();
    }

    public ASFailureMode getMode()
    {
        return mode;
    }

    public ASFailureType getType()
    {
        return type;
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
        this.mode = ASFailureMode.valueOf(mode);
    }

    public String getModeArg() {
        return modeArg;
    }

    public void setModeArg(String modeArg) {
        this.modeArg = modeArg;
    }

    public void setType(String type)
    {
        this.type = ASFailureType.valueOf(type);
    }

    public String toString()
    {
        return new StringBuilder().append(mode).append(',').append(type).toString();
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof ASFailureSpec)) return false;

        ASFailureSpec that = (ASFailureSpec) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    public int hashCode()
    {
        return (name != null ? name.hashCode() : 0);
    }
}