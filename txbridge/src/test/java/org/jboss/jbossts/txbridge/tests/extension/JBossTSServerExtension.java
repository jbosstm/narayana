/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.txbridge.tests.extension;

import org.jboss.arquillian.container.spi.ServerKillProcessor;
import org.jboss.arquillian.core.spi.LoadableExtension;


/**
 * Server extension for JBossTSAS7ServerKillProcessor.
 *
 * @author <a href="mailto:istudens@redhat.com">Ivo Studensky</a>
 * @author <a href="mailto:hhovsepy@redhat.com">Hayk Hovsepyan</a>
 */
public class JBossTSServerExtension implements LoadableExtension {

	@Override
	public void register(ExtensionBuilder builder) {
		if (isWindows()) {
			builder.service(ServerKillProcessor.class, JBossTSAS7ServerKillProcessorWin.class);
		} else {
			builder.service(ServerKillProcessor.class, JBossTSAS7ServerKillProcessor.class);
		}
	}
	
	public static boolean isWindows() {
        String osName = System.getProperty("os.name");

        return osName != null  && ((osName.indexOf("Windows") > -1) || (osName.indexOf("windows") > -1));
    }
	
    public static boolean isLinux() {
        String osName = System.getProperty("os.name");

        return osName != null  && ((osName.indexOf("Linux") > -1) || (osName.indexOf("linux") > -1));
    }

    public static boolean isSolaris() {
        String osName = System.getProperty("os.name");

        return osName != null  && ((osName.indexOf("Solaris") > -1) || (osName.indexOf("solaris") > -1) || (osName.indexOf("SunOS") > -1));
    }

    public static boolean isHpux() {
        String osName = System.getProperty("os.name");

        return osName != null  && ((osName.toUpperCase().indexOf("HP-UX") > -1));
    }
    
    public static boolean isIbmJdk() {
        return System.getProperty("java.vendor").contains("IBM Corporation");
    }
    
    enum OSType {
    	
    	WINDOWS {
    		@Override
    		public String getPSCommand() {
    			return "wmic PROCESS GET ProcessId,CommandLine,Name";
    		}
   		},
    	LINUX {
    		@Override
    		public String getPSCommand() {
    			return "ps aux";
    		}
    		
    		@Override
    		String getPSIDIndex() {
    			return "{print $2}";
    		}
    	},
    	SOLARIS {
    		@Override
    		public String getPSCommand() {
    			return  JBossTSServerExtension.isIbmJdk() ? "/usr/ucb/ps aux" : "jps";
    		}
    		
    		@Override
    		String getPSIDIndex() {
    			return JBossTSServerExtension.isIbmJdk() ? "{print $2}" : "{print $1}";
    		}
    	},
    	HPUX {
    		@Override
    		public String getPSCommand() {
    			return  JBossTSServerExtension.isIbmJdk() ? "/usr/ucb/ps aux" : "jps";
    		}
    		
    		@Override
    		String getPSIDIndex() {
    			return JBossTSServerExtension.isIbmJdk() ? "{print $2}" : "{print $1}";
    		}
    	};
    	
    	// IBM JDK does not have "jps" so using "ps", Solaris have "ps" in "/usr/ucb/".
    	String getPSCommand() {
    		return null;
    	}
    	
    	String getPSIDIndex() {
    		return null;
    	}
    	
    	static OSType getOSType() {
    		if (JBossTSServerExtension.isHpux()) return HPUX;
    		if (JBossTSServerExtension.isWindows()) return WINDOWS;
    		if (JBossTSServerExtension.isSolaris()) return SOLARIS;
    		return LINUX;
    	}
    	
    }
}