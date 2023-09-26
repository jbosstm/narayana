/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.Utils;



public interface ServerIORStorePlugin
{
	public void initialise() throws Exception;

	public void storeIOR(String serverName, String serverIOR) throws Exception;

	public void removeIOR(String serverName) throws Exception;

	public String loadIOR(String serverName) throws Exception;

	public void remove();
}