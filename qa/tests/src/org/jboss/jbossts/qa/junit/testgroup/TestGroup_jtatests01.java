/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2009,
 * @author JBoss Inc.
 */
package org.jboss.jbossts.qa.junit.testgroup;

import org.jboss.jbossts.qa.junit.*;
import org.junit.*;

public class TestGroup_jtatests01 extends TestGroupBase
{
    public TestGroup_jtatests01() {
        isRecoveryManagerNeeded = true;
    }

	@Test public void JTATests01_Test001()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.JTA01Tests.Test01.class, "$(LOCAL_PARAMETER)");
	}

	@Test public void JTATests01_Test002()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.JTA01Tests.Test02.class, "$(LOCAL_PARAMETER)");
	}

	@Test public void JTATests01_Test003()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.JTA01Tests.Test03.class, "$(LOCAL_PARAMETER)");
	}

	@Test public void JTATests01_Test004()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.JTA01Tests.Test04.class, "$(LOCAL_PARAMETER)", "1000");
	}

	@Test public void JTATests01_Test005()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.JTA01Tests.Test05.class, "$(LOCAL_PARAMETER)", "1000");
	}

	@Test public void JTATests01_Test006()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.JTA01Tests.Test06.class, "$(LOCAL_PARAMETER)", "32", "1000");
	}

}