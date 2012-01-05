package com.arjuna.qa.extension;

import org.jboss.arquillian.container.spi.ServerKillProcessor;
import org.jboss.arquillian.core.spi.LoadableExtension;

public class ServerExtension implements LoadableExtension {

	@Override
	public void register(ExtensionBuilder builder) {
		builder.service(ServerKillProcessor.class, JBossAS7ServerKillProcessor.class);
	}

}
