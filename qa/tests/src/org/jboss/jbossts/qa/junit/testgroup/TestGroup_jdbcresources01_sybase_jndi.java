/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.junit.testgroup;

public class TestGroup_jdbcresources01_sybase_jndi extends TestGroup_jdbcresources01_abstract
{
	public String getTestGroupName() {
		return "jdbcresources01_sybase_jndi";
	}

    public String getDBName1() {
        return "DB1_SYBASE_JNDI";
    }

    public String getDBName2() {
        return "DB2_SYBASE_JNDI";
    }
}