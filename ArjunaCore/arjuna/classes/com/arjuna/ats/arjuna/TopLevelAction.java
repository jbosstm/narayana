/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: TopLevelAction.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna;

import com.arjuna.ats.arjuna.coordinator.ActionType;

/**
 * This class provides a (nested) top-level transaction. So, no
 * matter how deeply nested a thread may be within a transaction
 * hierarchy, creating an instance of this class will always start
 * a new top-level transaction.
 *
 * Derived from AtomicAction so we can get the action-to-thread
 * tracking.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: TopLevelAction.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class TopLevelAction extends AtomicAction
{

public TopLevelAction ()
    {
	super(ActionType.TOP_LEVEL);
    }

}
