/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2020, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package com.hp.mwtests.ts.jta;

import com.arjuna.ats.jta.logging.jtaLogger;

/**
 * A test helper class which is used to find out if the Throwable API call
 * to use suppressed exceptions is available on JDK the test is executed at.
 */
public class SuppressedExceptionsHelper {
    private static boolean canSuppress = false;

    static {
        try {
            Throwable.class.getMethod("addSuppressed", Throwable.class);
            canSuppress = true;
        } catch (Throwable e) {
            if (jtaLogger.logger.isTraceEnabled()) {
                jtaLogger.logger.trace("Can't suppress throwables (likely running on JDK6 or lower)");
            }
        }
    }

    /**
     * Informing the test if the suppressed exceptions are available.
     * The suppressed exceptions can be used at JDK7+.
     * For JDK6 they have to be omitted.
     *
     * @return bolean if {@code Throwable#addSuppressed(Throwable)} can be used, false otherwise
     */
    public static boolean canSuppress() {
        return canSuppress;
    }
}
