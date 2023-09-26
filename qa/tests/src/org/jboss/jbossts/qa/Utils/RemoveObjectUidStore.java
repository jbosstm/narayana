/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.Utils;

public class RemoveObjectUidStore
{
	public static void main(String[] args)
	{
		ObjectUidStore.remove();

		System.out.println("Passed");
	}
}