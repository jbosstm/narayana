/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.junit.testgroup;

public class TestGroup_jdbcresources02_mysql_jndi extends TestGroup_jdbcresources02_abstract
{
	public String getTestGroupName()
	{
		return "jdbcresources02_mysql_jndi";
	}

    public String getDBName1() {
        return "DB1_MYSQL_JNDI";
    }

    public String getDBName2() {
        return "DB2_MYSQL_JNDI";
    }
}