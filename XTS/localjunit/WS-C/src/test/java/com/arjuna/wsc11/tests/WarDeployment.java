package com.arjuna.wsc11.tests;

import java.io.File;

import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import com.arjuna.wsc.tests.TestUtil;

public class WarDeployment {
	public static WebArchive getDeployment(Class<?>...args){
		WebArchive archive = ShrinkWrap
		.create(WebArchive.class, "test.war")
		.addClass(TestInitialisation.class)
		.addClass(TestContextFactory.class)
		.addClass(TestUtil.class)
		.addClass(TestUtil11.class)
		.addClass(TestRegistrar.class)
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
