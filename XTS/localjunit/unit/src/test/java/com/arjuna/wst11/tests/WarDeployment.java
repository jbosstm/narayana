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

package com.arjuna.wst11.tests;

import java.io.File;

import com.arjuna.wst11.tests.arq.BaseWSTTest;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import com.arjuna.wst.tests.common.TestAbortedVoteParticipant;
import com.arjuna.wst.tests.common.TestFaultedExceptionBusinessAgreementWithCoordinatorCompletionParticipant;
import com.arjuna.wst.tests.common.TestFaultedExceptionBusinessAgreementWithParticipantCompletionParticipant;
import com.arjuna.wst.tests.common.TestNoExceptionBusinessAgreementWithCoordinatorCompletionParticipant;
import com.arjuna.wst.tests.common.TestNoExceptionBusinessAgreementWithParticipantCompletionParticipant;
import com.arjuna.wst.tests.common.TestNoExceptionParticipant;
import com.arjuna.wst.tests.common.TestPreparedVoteParticipant;
import com.arjuna.wst.tests.common.TestReadOnlyVoteParticipant;
import com.arjuna.wst.tests.common.TestSystemExceptionBusinessAgreementWithCoordinatorCompletionParticipant;
import com.arjuna.wst.tests.common.TestSystemExceptionBusinessAgreementWithParticipantCompletionParticipant;
import com.arjuna.wst.tests.common.TestSystemExceptionParticipant;
import com.arjuna.wst.tests.common.TestTransactionRolledBackExceptionParticipant;
import com.arjuna.wst.tests.common.TestWrongStateExceptionBusinessAgreementWithCoordinatorCompletionParticipant;
import com.arjuna.wst.tests.common.TestWrongStateExceptionBusinessAgreementWithParticipantCompletionParticipant;
import com.arjuna.wst.tests.common.TestWrongStateExceptionParticipant;

public class WarDeployment {
	public static WebArchive getDeployment(Class<?>...args){
		WebArchive archive = ShrinkWrap
		.create(WebArchive.class, "test.war")
		.addClass(TestInitialisation.class)
		.addClass(TestUtil.class)
		.addClass(TestAbortedVoteParticipant.class)
		.addClass(TestFaultedExceptionBusinessAgreementWithCoordinatorCompletionParticipant.class)
		.addClass(TestFaultedExceptionBusinessAgreementWithParticipantCompletionParticipant.class)
		.addClass(TestNoExceptionBusinessAgreementWithCoordinatorCompletionParticipant.class)
		.addClass(TestNoExceptionBusinessAgreementWithParticipantCompletionParticipant.class)
		.addClass(TestNoExceptionParticipant.class)
		.addClass(TestPreparedVoteParticipant.class)
		.addClass(TestReadOnlyVoteParticipant.class)
		.addClass(TestTransactionRolledBackExceptionParticipant.class)
		.addClass(TestWrongStateExceptionParticipant.class)
		.addClass(TestSystemExceptionParticipant.class)
		.addClass(TestSystemExceptionBusinessAgreementWithParticipantCompletionParticipant.class)
		.addClass(TestWrongStateExceptionBusinessAgreementWithParticipantCompletionParticipant.class)
		.addClass(TestSystemExceptionBusinessAgreementWithCoordinatorCompletionParticipant.class)
		.addClass(TestWrongStateExceptionBusinessAgreementWithCoordinatorCompletionParticipant.class)
		.addClass(TestNoExceptionCompletionCoordinatorParticipant.class)
		.addClass(TestTransactionRolledBackExceptionCompletionCoordinatorParticipant.class)
		.addClass(TestUnknownTransactionExceptionCompletionCoordinatorParticipant.class)
		.addClass(TestSystemExceptionCompletionCoordinatorParticipant.class)
		.addClass(TestNoExceptionBAPMParticipant.class)
		.addClass(TestWrongStateExceptionBAPMParticipant.class)
		.addClass(TestSystemExceptionBAPMParticipant.class)
		.addClass(TestNoExceptionBusinessActivityTerminator.class)
		.addClass(TestUnknownTransactionExceptionBusinessActivityTerminator.class)
		.addClass(TestTransactionRolledBackExceptionBusinessActivityTerminator.class)
		.addClass(TestSystemExceptionBusinessActivityTerminator.class)
        .addClass(BaseWSTTest.class)
		.addClasses(args)
		.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

		archive.delete(ArchivePaths.create("META-INF/MANIFEST.MF"));

		final String ManifestMF = "Manifest-Version: 1.0\n"
			+ "Dependencies: org.jboss.modules,deployment.arquillian-service,org.jboss.msc,"
			+ "org.jboss.jts,org.jboss.ws.api,javax.xml.ws.api,org.jboss.xts,"
			+ "org.jboss.ws.jaxws-client services export,org.jboss.ws.cxf.jbossws-cxf-client services export\n";
		archive.setManifest(new StringAsset(ManifestMF));

		return archive;
	}
}
