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

package com.arjuna.wscf11.tests;

import java.io.File;

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
