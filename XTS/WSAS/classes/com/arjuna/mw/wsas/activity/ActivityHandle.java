/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2002,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ActivityHandle.java,v 1.1 2002/11/25 10:51:40 nmcl Exp $
 */

package com.arjuna.mw.wsas.activity;

/**
 * ActivityHandle is used as a representation of a single activity
 * when it is suspended from a running thread and may be later
 * resumed. The implementation of the token can be as lightweight
 * as required by the underlying implementation in order that it
 * can uniquely represent all activity instances.
 *
 * Since this is a client-facing class, it is unlikely that the
 * application user will typically want to see the entire activity
 * context in order to simply suspend it from the thread.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: ActivityHandle.java,v 1.1 2002/11/25 10:51:40 nmcl Exp $
 * @since 1.0.
 */

public interface ActivityHandle
{
    /**
     * @return the timeout associated with this activity.
     */

    public int getTimeout ();
    
    /**
     * Although users won't typically care what the underlying implementation
     * of a context is, they will need to do comparisons.
     * So, although this method is provided by Java.Object we have it here
     * to ensure that we don't forget to implement it!
     *
     * Two instances are equal if the refer to the same transaction.
     *
     * @return <code>true</code> if the two objects are the same,
     * <code>false</code> otherwise.
     */

    public boolean equals (Object obj);
    
    /**
     * Although users won't typically care what the underlying implementation
     * of a context is, they will need to do comparisons.
     * So, although this method is provided by Java.Object we have it here
     * to ensure that we don't forget to implement it!
     *
     * @return the hash value for the target.
     */

    public int hashCode ();

    /**
     * @return whether or not this is a valid handle.
     */

    public boolean valid ();

    /**
     * @return the unique activity identifier.
     */

    public String tid ();

}
