/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.junit.testgroup;

public class TestGroup_jdbcresources01_mssqlserver_jndi extends TestGroup_jdbcresources01_abstract
{
	public String getTestGroupName() {
		return "jdbcresources01_mssqlserver_jndi";
	}

    public String getDBName1() {
        return "DB_SQL1_JNDI";
    }

    public String getDBName2() {
        return "DB_SQL2_JNDI";
    }
}