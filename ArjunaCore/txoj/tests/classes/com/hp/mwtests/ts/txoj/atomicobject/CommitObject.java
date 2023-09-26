/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.txoj.atomicobject;

import com.arjuna.ats.arjuna.AtomicAction;



public class CommitObject extends Thread
{

	public CommitObject ()
	    {
	    }

	public void run ()
	    {
		int thr = nextThreadId;

		nextThreadId++;

		AtomicAction a = new AtomicAction();

		a.begin();

		AtomicObjectTest3.indent(thr, 0);
		System.out.println("begin");

		AtomicObjectTest3.randomOperation(thr, 0);
		AtomicObjectTest3.randomOperation(thr, 0);

		a.commit();

		AtomicObjectTest3.indent(thr, 0);
		System.out.println("commit");
	    }

	private static int nextThreadId = 3;

	}