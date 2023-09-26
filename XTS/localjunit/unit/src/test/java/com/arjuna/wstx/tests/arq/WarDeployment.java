/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.wstx.tests.arq;

import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public class WarDeployment {
	public static WebArchive getDeployment(Class<?>...args){
		WebArchive archive = ShrinkWrap
		.create(WebArchive.class, "test.war")
		.addClasses(args)
        .addAsWebInfResource(new StringAsset("<beans bean-discovery-mode=\"all\"></beans>"), "beans.xml");

		archive.delete(ArchivePaths.create("META-INF/MANIFEST.MF"));

		final String ManifestMF = "Manifest-Version: 1.0\n"
			+ "Dependencies: org.jboss.xts\n";
		archive.setManifest(new StringAsset(ManifestMF));

		return archive;
	}
}
