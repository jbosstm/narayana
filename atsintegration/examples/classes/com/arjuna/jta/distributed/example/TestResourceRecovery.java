/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package com.arjuna.jta.distributed.example;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.xa.XAResource;

import org.jboss.tm.XAResourceRecovery;

/**
 * This is a simple TestResource XAResourceRecovery helper, any knowledge it has
 * of the rest of the example is purely for debugging. It should be considered a
 * black box.
 */
public class TestResourceRecovery implements XAResourceRecovery {

	private List<TestResource> resources = new ArrayList<TestResource>();

	public TestResourceRecovery(String nodeName) throws IOException {
		File file = new File(System.getProperty("user.dir") + "/distributedjta-examples/TestResource/" + nodeName + "/");
		if (file.exists() && file.isDirectory()) {
			File[] listFiles = file.listFiles();
			for (int i = 0; i < listFiles.length; i++) {
				File currentFile = listFiles[i];
				if (currentFile.getAbsolutePath().endsWith("_")) {
					resources.add(new TestResource(nodeName, currentFile));
				}
			}
		}
	}

	@Override
	public XAResource[] getXAResources() {
		return resources.toArray(new XAResource[] {});
	}

}
