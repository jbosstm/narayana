package com.arjuna.ats.internal.jta.tools.osb.mbean.jts;

import com.arjuna.ats.jta.common.jtaPropertyManager;

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
