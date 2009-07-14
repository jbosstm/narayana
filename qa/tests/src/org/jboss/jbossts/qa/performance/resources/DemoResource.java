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
package org.jboss.jbossts.qa.performance.resources;

/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: DemoResource.java,v 1.1.1.1 2002/08/13 12:27:17 rbegg Exp $
 */

import org.omg.CosTransactions.*;
import org.omg.CORBA.SystemException;

public class DemoResource extends ResourcePOA
{
    public Vote prepare() throws HeuristicMixed, HeuristicHazard, SystemException
    {
        // System.out.println("prepare called");

        return Vote.VoteCommit;
    }

    public void rollback() throws HeuristicCommit, HeuristicMixed,
            HeuristicHazard, SystemException
    {
        // System.out.println("rollback called");
    }

    public void commit() throws NotPrepared, HeuristicRollback,
            HeuristicMixed, HeuristicHazard, SystemException
    {
        // System.out.println("commit called");
    }

    public void commit_one_phase() throws HeuristicHazard, SystemException
    {
        // System.out.println("commit_one_phase called");
    }

    public void forget() throws SystemException
    {
        // System.out.println("forget called");
    }
}
