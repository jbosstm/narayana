/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.orbportability.initialisation.postset;

import com.arjuna.orbportability.utils.InitClassInterface;



public class SinglePostSetUsingInterface implements InitClassInterface
{
	/**
	 * This method is called and passed the object which is associated with this pre/post-initialisation routine.
	 *
	 * @param obj The object which has or is being initialised.
	 */
	public void invoke(Object obj)
	{
		System.out.println(this.getClass().getName()+".invoke("+obj+"): called");

		_called = true;
		_passedObj = obj;
	}

	public static boolean _called = false;
	public static Object  _passedObj = null;
}