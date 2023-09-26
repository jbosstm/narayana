/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.Utils;

public class RemoveServerIORStore
{
	public static void main(String[] args)
	{
		ServerIORStore.remove();

		for (int count = 0; count < args.length; count++)
		{
			try
			{
				ServerIORStore.removeIOR(args[count]);
			}
			catch (Exception e)
			{
				e.printStackTrace(System.err);
			}
		}
		System.out.println("Passed");
	}
}