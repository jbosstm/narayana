package com.arjuna.qa.extension;

import org.jboss.arquillian.container.spi.ServerKillProcessor;
import org.jboss.arquillian.core.spi.LoadableExtension;

public class ServerExtension implements LoadableExtension {

	@Override
	public void register(ExtensionBuilder builder) {
		if (isWindows()) {
			builder.service(ServerKillProcessor.class, JBossAS7ServerKillProcessorWin.class);
		} else {
			builder.service(ServerKillProcessor.class, JBossAS7ServerKillProcessor.class);
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
}
