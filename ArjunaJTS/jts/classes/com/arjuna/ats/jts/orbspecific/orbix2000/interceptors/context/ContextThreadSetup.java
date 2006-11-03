/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
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
 * Copyright (C) 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: ContextThreadSetup.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.jts.orbspecific.orbix2000.interceptors.context;

import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;
import com.arjuna.ats.internal.arjuna.thread.ThreadSetup;

import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import com.arjuna.ats.internal.jts.OTSImpleManager;

import com.arjuna.ats.jts.*;

class ContextThreadSetup implements ThreadSetup
{

    public void setup ()
    {
	/*
	 * Simply getting (or trying to get) the current tx control
	 * will ensure that this thread is initialised properly. We
	 * have to do this because in a POA implementation the receiving
	 * thread may not be the same one which does the work, so we
	 * cannot do thread association at the interceptor level. We must
	 * do it when the invoked method actually gets called.
	 */

	CurrentImple curr = OTSImpleManager.current();

	/*
	 * Probably separate the underlying work out so that we can
	 * call that directly. No real harm at present since the hard
	 * work represents most of the overhead and has to be done
	 * anyway.
	 */

	curr.contextManager().associate();
    }

    ContextThreadSetup ()
    {
    }
    
}
