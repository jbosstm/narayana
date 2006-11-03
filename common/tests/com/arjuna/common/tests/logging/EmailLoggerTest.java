/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors. 
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
* EmailLoggerTest.java
*
* Copyright (c) 2004 Arjuna Technologies Ltd.
* Arjuna Technologies Ltd. Confidential
*
* $Id: EmailLoggerTest.java 2342 2006-03-30 13:06:17Z  $
*/
package com.arjuna.common.tests.logging;

import com.arjuna.common.util.logging.Logi18n;
import com.arjuna.common.util.logging.LogFactory;

/**
 * This class demonstrates how to set up a CLF EMAIL logger.
 *
 * This emails debug messages over a configurable severity threshold to an administrator.
 *
 * Note: normally, the resource bundle file EmailLoggerTest.properties is generated automaticlaly during a product
 * build. Here, one is provided manually. To use this, run the program with working directory set to
 * e.g., c:\Projects\common and with the following parameters:
 *
 * -Dlog4j.configuration=file://localhost:/C:\Projects\common\tests\com\arjuna\common\tests\logging\log4j-config.xml
 * -Dcom.arjuna.common.util.logger=log4j
 *
 *
 * @author  Thomas Rischbeck <thomas.rischbeck@arjuna.com>
 * @version $Revision: 2342 $
 */
public class EmailLoggerTest {

    /** FQCN */
    private static String CLASS = EmailLoggerTest.class.getName();

    /** logger name for the email logger */
    private static String EMAIL_LOGGER_NAME = "com.arjuna.ams.internal.service.ha.email";

    /** create a logger that has the class name: com.arjuna.common.tests.logging.EmailLoggerTest */
    private static Logi18n log = LogFactory.getLogi18n(CLASS);

    /** create an email-logger that has a different name */
    private static Logi18n mailLog = LogFactory.getLogi18n(EMAIL_LOGGER_NAME);

    /**
     *
     * @param args
     *
     * @message msgTest logging {0} and {1} at the severity level {2}. Note: the severity level should not normally
     *  be part of the log message, since this is already recorded by CLF separately.
     * @message msgEmail this is a log message that is sent via {0} to an administrator.
     */
    public static void main(String[] args)
    {
        // this call type is using the logger's name for looking up the resource bundle
        // with the provided log4j configuration files, these log messages will go to the console only
        log.debug("msgTest", new Object[] {"foo", "bar", "debug"});
        log.info("msgTest", new Object[] {"foo", "bar", "info"});
        log.warn("msgTest", new Object[] {"foo", "bar", "warn"});
        log.error("msgTest", new Object[] {"foo", "bar", "error"});
        log.fatal("msgTest", new Object[] {"foo", "bar", "fatal"});

        // this is using CLASS as a resource bundle name -- same res bundle as the logger above.
        // with the provided log4j configuration files, these log messages will go to the console as well
        // as to the email logger (the email logger is using "info" as threshold.
        mailLog.debugb(CLASS, "msgTest", new Object[] {"email"});
        mailLog.infob(CLASS, "msgTest", new Object[] {"email"});
        mailLog.warnb(CLASS, "msgTest", new Object[] {"email"});
        mailLog.errorb(CLASS, "msgTest", new Object[] {"email"});
        mailLog.fatalb(CLASS, "msgTest", new Object[] {"email"});
    }
}
