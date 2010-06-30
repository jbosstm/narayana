package org.jboss.jbossts.xts.logging;

import com.arjuna.common.util.logging.LogFactory;
import com.arjuna.common.util.logging.LogNoi18n;
import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;

public class XTSLogger
{
    public static LogNoi18n arjLogger;

    public static BasicLogger logger;
    public static xtsrecoveryI18NLogger i18NLogger;

    public static void initialize(LogNoi18n noi18n)
    {
        arjLogger = noi18n;
        logger = Logger.getLogger("com.arjuna.wsrecovery");
        i18NLogger = new xtsrecoveryI18NLoggerImpl(Logger.getLogger("com.arjuna.wsrecovery"));
    }

    static
    {
        LogFactory.initializeModuleLogger(XTSLogger.class, "com.arjuna.wsrecovery");
    }
}
