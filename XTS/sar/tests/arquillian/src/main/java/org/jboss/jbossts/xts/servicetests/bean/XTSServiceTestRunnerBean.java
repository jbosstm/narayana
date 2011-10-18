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

package org.jboss.jbossts.xts.servicetests.bean;

import org.jboss.logging.Logger;
import org.jboss.jbossts.xts.servicetests.ejb.XTSServiceTestRunnerEJB;
import javax.inject.Inject;

/**
 * A service bean used to run XTS service tests at JBoss startup. A specific test is configurerd by setting an
 * environment variable. It will normally execute in a JVM configured to use the Byteman agent and an appropriately
 * defined Byteman rule set.
 */

public class XTSServiceTestRunnerBean implements XTSServiceTestRunner
{

	private final Logger log = org.jboss.logging.Logger.getLogger(XTSServiceTestRunnerBean.class);

	@Inject
	private XTSServiceTestRunnerEJB testRunnerEJB;
	
	public void runTest(String testName) throws Exception {
		log.info("XTSServiceTestRunnerBean");
		testRunnerEJB.runTest(testName);
	}
}