/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.Utils;

public interface Setup
{
	public void start(String[] args) throws Exception, Error;

	public void stop() throws Exception, Error;
}