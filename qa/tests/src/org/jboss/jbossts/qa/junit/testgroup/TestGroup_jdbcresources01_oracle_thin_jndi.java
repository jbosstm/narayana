/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.junit.testgroup;

public class TestGroup_jdbcresources01_oracle_thin_jndi extends TestGroup_jdbcresources01_abstract
{
    public String getTestGroupName() {
        return "jdbcresources01_oracle_thin_jndi";
    }

    public String getDBName1() {
        return "DB1_THIN_JNDI";
    }

    public String getDBName2() {
        return "DB2_THIN_JNDI";
    }
}