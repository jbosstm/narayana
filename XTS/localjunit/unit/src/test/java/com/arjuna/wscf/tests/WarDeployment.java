/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.arjuna.wscf.tests;

import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public class WarDeployment {
	public static WebArchive getDeployment(Class<?>...args){
		WebArchive archive = ShrinkWrap
		.create(WebArchive.class, "test.war")
		.addClass(SagasParticipant.class)
		.addClass(TwoPhaseParticipant.class)
		.addClass(TwoPhaseSynchronization.class)
		.addClass(WSCF11TestUtils.class)
		.addClasses(args)
        .addAsWebInfResource(new StringAsset("<beans bean-discovery-mode=\"all\"></beans>"), "beans.xml");

		archive.delete(ArchivePaths.create("META-INF/MANIFEST.MF"));

		final String ManifestMF = "Manifest-Version: 1.0\n"
			+ "Dependencies: org.jboss.modules,org.jboss.msc,"
			+ "org.jboss.jts,org.jboss.ws.api,jakarta.xml.ws.api,org.jboss.xts,"
			+ "org.jboss.ws.jaxws-client services export,org.jboss.ws.cxf.jbossws-cxf-client services export\n";
		archive.setManifest(new StringAsset(ManifestMF));

		return archive;
	}
}