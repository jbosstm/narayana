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
