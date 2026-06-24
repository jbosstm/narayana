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
