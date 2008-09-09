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
package com.hp.mwtests.performance.implementations.local;

import com.hp.mwtests.performance.PerformanceTest;
import com.arjuna.ats.arjuna.AtomicAction;

public class NestedTrx extends PerformanceTest
{
    protected void work() throws Exception
    {
        try
        {
            AtomicAction tx1 = new AtomicAction();
            AtomicAction tx2 = new AtomicAction();

            tx1.begin();         // Top level begin
            tx2.begin();         // Nested level begin

            if (isParameterDefined("-commit"))
            {
                tx2.commit();        // Nested level commit
                tx1.commit();        // Top level commit
            }
            else
            {
                tx2.abort();        // Nested level rollback
                tx1.abort();        // Top level rollback
            }

        }
        catch (Exception e)
        {
            System.err.println("Unexpected Exception: "+e);
            e.printStackTrace(System.err);
        }

    }
}
