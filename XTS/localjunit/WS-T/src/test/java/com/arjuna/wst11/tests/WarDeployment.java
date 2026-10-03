package com.arjuna.wst11.tests;

import java.io.File;

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
		.addClasses(args)
		.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
		.setWebXML(new File("src/test/resources/web.xml"));

		archive.delete(ArchivePaths.create("META-INF/MANIFEST.MF"));

		final String ManifestMF = "Manifest-Version: 1.0\n"
			+ "Dependencies: org.jboss.modules,deployment.arquillian-service,org.jboss.msc,"
			+ "org.jboss.jts,org.jboss.ws.api,javax.xml.ws.api,org.jboss.xts,"
			+ "org.jboss.ws.jaxws-client services export,org.jboss.ws.cxf.jbossws-cxf-client services export\n";
		archive.setManifest(new StringAsset(ManifestMF));

		return archive;
	}
}
