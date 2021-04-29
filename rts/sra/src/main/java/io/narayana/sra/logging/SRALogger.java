package io.narayana.sra.logging;

import org.jboss.logging.Logger;

public class SRALogger {
	private SRALogger() {
    }

    public static final Logger logger = Logger.getLogger("io.narayana.sra");
    public static final sraI18NLogger i18NLogger = Logger.getMessageLogger(sraI18NLogger.class, "io.narayana.sra");

}
