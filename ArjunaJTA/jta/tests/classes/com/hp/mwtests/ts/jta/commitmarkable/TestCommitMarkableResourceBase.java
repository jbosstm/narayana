/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.commitmarkable;

import java.io.File;

import org.jnp.server.NamingBeanImpl;
import org.junit.After;
import org.junit.Before;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;

public class TestCommitMarkableResourceBase {

	private NamingBeanImpl namingBeanImpl = null;

	private String resetPropertiesFile;

	protected RecoveryManager manager;

	private static boolean inAS = false;

	public static void setInAS(boolean inAS) {
		TestCommitMarkableResourceBase.inAS = inAS;
	}

	@Before
	public final void setup() throws Exception {

		if (inAS)
			return;

		File file = new File(System.getProperty("user.dir") + "/ObjectStore");
		if (file.exists()) {
			Utils.removeRecursive(file.toPath());
		}

		System.setProperty("java.naming.factory.initial",
				"org.jnp.interfaces.NamingContextFactory");
		System.setProperty("java.naming.factory.url.pkgs",
				"org.jboss.naming:org.jnp.interfaces");
		namingBeanImpl = new NamingBeanImpl();
		namingBeanImpl.start();

		resetPropertiesFile = System
				.getProperty("com.arjuna.ats.arjuna.common.propertiesFile");
		System.setProperty("com.arjuna.ats.arjuna.common.propertiesFile",
					"commitmarkableresourcejbossts-properties.xml");

		manager = RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT);
	}

	@After
	public final void tearDown() {
		if (inAS)
			return;

		namingBeanImpl.stop();
		namingBeanImpl = null;

		if (resetPropertiesFile != null) {
			System.setProperty("com.arjuna.ats.arjuna.common.propertiesFile",
					resetPropertiesFile);
		} else {
			System.clearProperty("com.arjuna.ats.arjuna.common.propertiesFile");
		}
	}

}