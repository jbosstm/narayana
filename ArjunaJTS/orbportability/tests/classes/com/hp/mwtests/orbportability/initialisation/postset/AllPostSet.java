/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.orbportability.initialisation.postset;



public class AllPostSet
{
	public AllPostSet()
	{
		System.out.println(this.getClass().getName()+": called");

		_called = true;
	}

	public static boolean _called = false;
}