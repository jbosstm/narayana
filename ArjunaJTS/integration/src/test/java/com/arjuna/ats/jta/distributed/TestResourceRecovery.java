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
package com.arjuna.ats.jta.distributed;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.xa.XAResource;

import org.jboss.tm.XAResourceRecovery;

public class TestResourceRecovery implements XAResourceRecovery {

	private String nodeName;

	public TestResourceRecovery(String nodeName) {
		this.nodeName = nodeName;
		System.out.println("[" + Thread.currentThread().getName() + "] TestResourceRecovery (" + nodeName + ")");
	}

	@Override
	public XAResource[] getXAResources() throws RuntimeException {
	    List<TestResource> resources = new ArrayList<TestResource>();
        File file = new File(System.getProperty("user.dir") + "/distributedjta-tests/TestResource/" + nodeName + "/");
        if (file.exists() && file.isDirectory()) {
            File[] listFiles = file.listFiles();
            for (int i = 0; i < listFiles.length; i++) {
                File currentFile = listFiles[i];
                if (currentFile.getAbsolutePath().endsWith("_")) {
                    try {
                        resources.add(new TestResource(nodeName, currentFile));
                    } catch (IOException e) {
                        throw new RuntimeException (e);
                    }
                    System.out.println("[" + Thread.currentThread().getName() + "] TestResourceRecovery (" + nodeName + ") Added TestResource: " + currentFile.getName());
                }
            }
        }
		System.out.println("[" + Thread.currentThread().getName() + "] TestResourceRecovery (" + nodeName + ") returning list of TestResources of length: " + resources.size());
		return resources.toArray(new XAResource[] {});
	}

}
