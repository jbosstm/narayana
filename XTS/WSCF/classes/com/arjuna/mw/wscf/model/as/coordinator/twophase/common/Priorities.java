/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
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
 * $Id: Priorities.java,v 1.1 2003/01/07 10:33:41 nmcl Exp $
 */

package com.arjuna.mw.wscf.model.as.coordinator.twophase.common;

/**
 * The typical two-phase coordination protocols have one or two types
 * of participant:
 *
 * (i) the normal participant that takes part in the prepare/commit/rollback.
 * (ii) the pre-two-phase participants (typically known as Synchronizations).
 *
 * Since WSCF allows participants to be prioritised within a coordinator's
 * registered lists or participants, we can use this information to specify
 * whether a participant is a "normal" participant or a Synchronization.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: Priorities.java,v 1.1 2003/01/07 10:33:41 nmcl Exp $
 * @since WSCF 1.0.
 */

public class Priorities
{
  
    /**
     * The participant is a Synchronization.
     */

    public static final int SYNCHRONIZATION = 0;

    /**
     * The participant takes part in the two-phase completion protocol only.
     */

    public static final int PARTICIPANT = 1;

    /**
     * @return the string version of the specified priority.
     */

    public static String stringForm (int res)
    {
	switch (res)
	{
	case SYNCHRONIZATION:
	    return "Priorities.SYNCHRONIZATION";
	case PARTICIPANT:
	    return "Priorities.PARTICIPANT";
	default:
	    return "Unknown - "+res;
	}
    }
	
}
