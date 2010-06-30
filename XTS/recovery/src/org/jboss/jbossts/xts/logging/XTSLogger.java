package org.jboss.jbossts.xts.logging;

import org.jboss.logging.Logger;

public class XTSLogger
{
    public static final Logger logger = Logger.getLogger("com.arjuna.wsrecovery");
    public static final xtsrecoveryI18NLogger i18NLogger  = new xtsrecoveryI18NLoggerImpl(logger);
}
