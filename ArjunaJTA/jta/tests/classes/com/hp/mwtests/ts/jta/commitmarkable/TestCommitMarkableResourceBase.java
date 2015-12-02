/*
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2013
 * @author JBoss Inc.
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
