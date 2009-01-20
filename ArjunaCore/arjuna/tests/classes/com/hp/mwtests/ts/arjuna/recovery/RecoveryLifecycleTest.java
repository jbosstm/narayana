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
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: EmbeddedRecoveryTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.arjuna.recovery;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;

import com.hp.mwtests.ts.arjuna.resources.*;

import org.jboss.dtf.testframework.unittest.Test;

public class RecoveryLifecycleTest extends Test
{

    public void run (String[] args)
    {
	RecoveryManager manager = RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT);
	DummyRecoveryModule module = new DummyRecoveryModule();

	manager.addModule(module);

	manager.scan();

	if (!module.finished())
	    assertFailure();

	manager.terminate();
	
	manager.initialize();
	
	module = new DummyRecoveryModule();
	
	if (module.finished())
	    assertFailure();
	
	manager.addModule(module);

        manager.scan();

        if (module.finished())
            assertSuccess();
        else
            assertFailure();
    }

    public static void main (String[] args)
    {
        RecoveryLifecycleTest test = new RecoveryLifecycleTest();

    	test.initialise(null, null, args, new org.jboss.dtf.testframework.unittest.LocalHarness());

    	test.run(args);
    }

}
