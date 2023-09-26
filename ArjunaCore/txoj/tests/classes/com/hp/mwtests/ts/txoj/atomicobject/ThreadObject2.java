/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.txoj.atomicobject;




public class ThreadObject2 extends Thread
{

	public ThreadObject2 (int v)
	    {
		_value = v;
	    }

	public void run ()
	    {
		for (int i = 0; i < 100; i++)
		{
		    AtomicObjectTest3.randomOperation(_value, 0);
		    AtomicObjectTest3.highProbYield();
		}
	    }

	private int _value;

}