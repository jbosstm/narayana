package org.jboss.jbossts.xts.logging;

import java.util.Locale;
import java.util.ResourceBundle;

import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.common.internal.util.logging.commonPropertyManager;
import com.arjuna.common.util.logging.LogFactory;
import com.arjuna.common.util.logging.LogNoi18n;
import com.arjuna.common.util.logging.Logi18n;

public class XTSLogger
{
    public static LogNoi18n arjLogger;
    public static Logi18n arjLoggerI18N;
    public static ResourceBundle log_mesg;

    static
    {
    	arjLogger = LogFactory.getLogNoi18n("com.arjuna.webservices.logging.XTSLogger");

        final String language = commonPropertyManager.getLoggingEnvironmentBean().getLanguage();
        final String country  = commonPropertyManager.getLoggingEnvironmentBean().getCountry();

    	final Locale currentLocale = new Locale(language, country);
    	log_mesg = ResourceBundle.getBundle("recovery_msg",currentLocale);

    	arjLoggerI18N = LogFactory.getLogi18n("com.arjuna.webservices.logging.XTSLoggerI18N",
    					     "recovery_msg_"+language+"_"+country);
    }
}
