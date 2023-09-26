/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


//

package org.jboss.jbossts.qa.JDBCLocals01Impls;

public interface InfoTable
{
	public void insert(String name, String value) throws Exception;

	public void update(String name, String value) throws Exception;

	public String select(String name) throws Exception;

	public void delete(String name) throws Exception;
}