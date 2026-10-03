/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2008,
 * @author JBoss Inc.
 */
package org.jboss.jbossts.qa.astests.recovery;

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
 * @see org.jboss.jbossts.qa.astests.ASCrashRecovery01.Test03 for an example
 * @see org.jboss.jbossts.qa.astests.recovery.ASFailureMode
 * @see org.jboss.jbossts.qa.astests.recovery.ASFailureType
 *
 * A failure specification is defined in ASTestConfig
 * @see org.jboss.jbossts.qa.astests.taskdefs.ASTestConfig
 */
public class ASFailureSpec implements Serializable
{
    String name;
    ASFailureMode mode;
    ASFailureType type;
    String modeArg;
    int recoveryArg;

    public ASFailureSpec()
    {
        mode = ASFailureMode.NONE;
        type = ASFailureType.NONE;
    }

    public ASFailureSpec(String name, ASFailureMode mode, ASFailureType type, String modeArg, int recoveryArg)
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

    public ASFailureMode getMode()
    {
        return mode;
    }

    public ASFailureType getType()
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
        this.mode = ASFailureMode.valueOf(mode);
    }

    public void setType(String type)
    {
        this.type = ASFailureType.valueOf(type);
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
