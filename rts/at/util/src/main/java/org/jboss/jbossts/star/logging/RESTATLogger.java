/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.star.logging;


import org.jboss.logging.Logger;



public final class RESTATLogger {
        private RESTATLogger() {
        }
        public static final Logger logger = Logger.getLogger("org.jboss.jbossts.star.logging");
        public static final atI18NLogger atI18NLogger = Logger.getMessageLogger(atI18NLogger.class, "org.jboss.jbossts.star.logging");

    }