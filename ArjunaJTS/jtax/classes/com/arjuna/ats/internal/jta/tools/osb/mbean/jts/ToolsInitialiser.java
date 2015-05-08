/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package com.arjuna.ats.internal.jta.tools.osb.mbean.jts;

import com.arjuna.ats.jta.common.jtaPropertyManager;

/**
 * @deprecated as of 5.0.5.Final - no longer required
 */
@Deprecated // no longer required
public class ToolsInitialiser {
	static private String JTS_TM_CLASSNAME_STANDALONE =
		"com.arjuna.ats.internal.jta.transaction.jts.TransactionManagerImple";
	static private String JTS_TM_CLASSNAME_ATS =
		"com.arjuna.ats.jbossatx.jts.TransactionManagerDelegate";
    static private String ORB_NAME = "tools-orb";
	static boolean initOrb = false;

	private boolean isJTS;

	public boolean isJTS() {
		return isJTS;
	}

    public ToolsInitialiser() throws Exception {
		String tmClassName = jtaPropertyManager.getJTAEnvironmentBean().getTransactionManagerClassName();

		isJTS = (JTS_TM_CLASSNAME_STANDALONE.equals(tmClassName)
			|| JTS_TM_CLASSNAME_ATS.equals(tmClassName));

		if (initOrb) {
        	try {
            	com.arjuna.orbportability.ORB _orb;
            	if (!com.arjuna.ats.internal.jts.ORBManager.isInitialised()) {
                	_orb = com.arjuna.orbportability.ORB.getInstance(ORB_NAME);
                	com.arjuna.orbportability.OA oa = com.arjuna.orbportability.OA.getRootOA(_orb);

                	_orb.initORB((String[]) null, null);
                	oa.initPOA(null);
            	}
        	} catch (Exception e) {
            	
        	}
        }

		if (isJTS) {
        	try {
            	Class c = Class.forName("com.arjuna.ats.internal.jts.Implementations");
				//Class<?> c2 = Class.forName("com.arjuna.ats.internal.jta.Implementationsx"); // needed for XAResourceRecord

            	c.getMethod("initialise").invoke(null);
        	} catch (Exception e) {
        	}
		}
    }
}
