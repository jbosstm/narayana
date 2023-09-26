/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.jts.orbspecific.javaidl.interceptors.interposition;

import com.arjuna.ats.internal.arjuna.thread.ThreadSetup;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;

class InterpositionThreadSetup implements ThreadSetup
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

    InterpositionThreadSetup ()
    {
    }
    
}