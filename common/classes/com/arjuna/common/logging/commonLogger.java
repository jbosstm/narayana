package com.arjuna.common.logging;


import org.jboss.logging.Logger;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class commonLogger {
    public static final Logger logger = Logger.getLogger("com.arjuna.ats.common");
    public static final commonI18NLogger i18NLogger = Logger.getMessageLogger(commonI18NLogger.class, "com.arjuna.ats.common");
}
