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

package com.arjuna.mw.wstx.logging;

public final class SuppressedExceptionHelper {
    private static boolean canSuppress;

    private SuppressedExceptionHelper() {
        // no instance
    }

    static {
        try {
            Throwable.class.getMethod("addSuppressed", Throwable.class);
            canSuppress = true;
        } catch (Throwable e) {
            if (wstxLogger.logger.isTraceEnabled()) {
                wstxLogger.logger.trace("Can't suppress throwables (likely running on JDK6 or lower). They will be logged as warnings.");
            }
        }
    }

    /**
     * Add an exception as suppressed to one provided as to be enhanced.
     * This is used as the JDK6 does know the suppressed exceptions,
     * if they are not available then the suppressed one is only logged as warning.
     *
     * @param exceptionToBeEnhanced an exception that will enhanced with suppressedException and it will be returned
     * @param suppressedException exception that will be added as suppressed to exceptionToBeEnhanced if JDK makes it possible
     * @return the exceptionToBeEnhanced which is enhanced with the suppressedException,
     *         or left as it's when suppressed feature is not available in JDK
     */
    public static <T extends Exception> T addSuppressedThrowable(T exceptionToBeEnhanced, Exception suppressedException) {
        if (canSuppress) {
            exceptionToBeEnhanced.addSuppressed(suppressedException);
        } else {
            wstxLogger.logger.warnf(suppressedException,"Suppressed exception as cause of the %s", exceptionToBeEnhanced);
        }
        return exceptionToBeEnhanced;
    }
}
