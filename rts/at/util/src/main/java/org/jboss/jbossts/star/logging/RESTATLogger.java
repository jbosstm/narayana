/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2021, Red Hat, Inc.
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.jbossts.star.logging;


import org.jboss.logging.Logger;



public final class RESTATLogger {
        private RESTATLogger() {
        }
        public static final Logger logger = Logger.getLogger("org.jboss.jbossts.star.logging");
        public static final atI18NLogger atI18NLogger = Logger.getMessageLogger(atI18NLogger.class, "org.jboss.jbossts.star.logging");

    }
