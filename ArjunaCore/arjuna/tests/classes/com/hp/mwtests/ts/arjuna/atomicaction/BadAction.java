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
package com.hp.mwtests.ts.arjuna.atomicaction;

/*
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: BadAction.java 2342 2006-03-30 13:06:17Z  $
 */

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.arjuna.common.*;
import org.jboss.dtf.testframework.unittest.Test;

public class BadAction extends Test
{

public void run(String[] args)
    {
        try
        {
            AtomicAction A = new AtomicAction();
            AtomicAction B = new AtomicAction();

            logInformation("Starting top-level action.");

            A.begin();

            logInformation(A.toString());

            logInformation("Starting nested action.");

            B.begin();

            logInformation(B.toString());

            logInformation("Committing top-level action. This should fail.");

            A.commit();

            logInformation("Committing nested action. This should fail.");

            B.commit();

            BasicAction current = BasicAction.Current();

            logInformation("Current action is " + current);

            assertSuccess();
        }
        catch (Exception e)
        {
            logInformation("Unexpected Exception - "+e);
            e.printStackTrace(System.err);
            assertFailure();
        }
    }

public static void main(String[] args)
    {
        BadAction test = new BadAction();
        test.initialise(null, null, args, new org.jboss.dtf.testframework.unittest.LocalHarness());
        test.run(args);
    }

};
