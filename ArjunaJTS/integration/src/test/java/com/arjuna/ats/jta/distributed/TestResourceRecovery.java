/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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