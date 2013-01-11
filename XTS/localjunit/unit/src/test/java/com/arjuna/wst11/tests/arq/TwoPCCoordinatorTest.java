/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.arjuna.wst11.tests.arq;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.wst11.tests.WarDeployment;
import com.arjuna.wst11.tests.arq.TestParticipantProcessor.ParticipantDetails;

@RunWith(Arquillian.class)
public class TwoPCCoordinatorTest extends BaseWSTTest {
	@Inject
	TwoPCCoordinatorTestCase test;

	@Deployment
	public static WebArchive createDeployment() {
		return WarDeployment.getDeployment(
				ParticipantDetails.class,
				TestParticipantProcessor.class,
				TwoPCCoordinatorTestCase.class);
	}

	@Before
	public void setUp() throws Exception{
		test.setUp();
	}

	@Test
	public void testSendPrepare() throws Exception {
		test.testSendPrepare();
	}
	
	@Test
	public void testSendCommit() throws Exception {
		test.testSendCommit();
	}
	
	@Test
	public void testSendRollback() throws Exception {
		test.testSendRollback();
	}
	
	@Test
	public void testSendError() throws Exception {
		test.testSendError();
	}

	@After
	public void tearDown() throws Exception {
		test.tearDown();
	}
}
