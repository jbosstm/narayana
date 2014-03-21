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

}
