/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.junit.testgroup;

import org.junit.Ignore;
import org.junit.Test;

public class TestGroup_jdbcresources02_ibmdb2_jndi extends TestGroup_jdbcresources02_abstract
{
	public String getTestGroupName() {
		return "jdbcresources02_ibmdb2_jndi";
	}

    public String getDBName1() {
        return "DB1_IBMDB2_JNDI";
    }

    public String getDBName2() {
        return "DB2_IBMDB2_JNDI";
    }

    // these 4 tests deadlock on db2, disable until we can figure out how to force row level locking.
    @Ignore @Test public void JDBCResources02_abstract_Test014() {}
    @Ignore @Test public void JDBCResources02_abstract_Test016() {}
    @Ignore @Test public void JDBCResources02_abstract_Test019() {}
    @Ignore @Test public void JDBCResources02_abstract_Test021() {}
}