/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
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
 * (C) 2010,
 * @author JBoss, by Red Hat.
 */
package org.jboss.jbossts.xts.logging;

import org.jboss.jbossts.xts.recovery.logging.recoveryI18NLogger;
import org.jboss.logging.Logger;
import static org.jboss.logging.Logger.Level.*;

/**
 * i18n log messages implementation for the xts service module.
 *
 * @author adinn
 */
public class xtsI18NLoggerImpl implements xtsI18NLogger
{
    private final Logger logger;

    xtsI18NLoggerImpl(Logger logger) {
        this.logger = logger;
    }

    public void error_XTSService_1(String arg0, Throwable arg1) {
        logger.logv(ERROR, arg1, "Unable to load XTS initialisation class {0}", arg0);
    }

    public void error_XTSService_2(String arg0) {
        logger.logv(ERROR, "Not an XTS initialisation class {0}", arg0);
    }

    public void error_XTSService_3(String arg0, Throwable arg1) {
        logger.logv(ERROR, arg1, "Unable to instantiate XTS initialisation class {0}", arg0);
    }

    public void error_XTSService_4(String arg0, Throwable arg1) {
        logger.logv(ERROR, arg1, "Unable to access XTS initialisation class {0}", arg0);
    }
}
