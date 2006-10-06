/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
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
 * (C) 2006,
 * @author JBoss Inc.
 *
 * $Id$
 */

package com.hp.mwtests.ts.jdbc.basic;

import com.arjuna.mwlabs.testframework.unittest.LocalHarness;

/**
 * Exercises the JDBC3.0 specific methods on the transactional JDBC wrapper.
 */
public class JDBC3Test extends JDBC2Test
{
	public void run(String[] args) {
		setup(args);


		// TODO: exercise JDBC3.0 specific methods here

	}

	public static void main(String[] args)
    {
        JDBC3Test test = new JDBC3Test();
        test.initialise(null,null,args,new LocalHarness());
        test.runTest();
    }
}
