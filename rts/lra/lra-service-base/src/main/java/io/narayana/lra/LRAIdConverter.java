/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2019, Red Hat, Inc., and individual contributors
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
package io.narayana.lra;

import io.narayana.lra.logging.LRALogger;
import org.eclipse.microprofile.lra.client.GenericLRAException;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

public abstract class LRAIdConverter {

    /**
     * Transforming the LRA id to {@link URL} format.
     *
     * @param lraId  LRA id to be transformed to URL
     */
    public static URL lraToURL(String lraId) {
        return lraToURL(lraId, "Invalid LRA id");
    }

    /**
     * Transforming the LRA id to {@link URL} format.
     *
     * @param lraId  LRA id to be transformed to URL
     * @param errorMessage  error message which will be included under
     *   {@link GenericLRAException} message
     */
    public static URL lraToURL(String lraId, String errorMessage) {
        try {
            return new URL(lraId);
        } catch (MalformedURLException e) {
            LRALogger.i18NLogger.error_urlConstructionFromStringLraId(lraId, e);
            throw new GenericLRAException(null, BAD_REQUEST.getStatusCode(), errorMessage + ": lra=" + lraId, e);
        }
    }

    /**
     * Transforming the LRA id to {@link URL} format with use of the {@link URLEncoder}.
     *
     * @param lraId  LRA id to be transformed to URL
     * @param errorMessage  error message which will be included under
     *   {@link GenericLRAException} message
     */
    public static String encodeURL(URL lraId, String errorMessage) {
        try {
            return URLEncoder.encode(lraId.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LRALogger.i18NLogger.error_invalidFormatToEncodeUrl(lraId, e);
            throw new GenericLRAException(lraId, BAD_REQUEST.getStatusCode(), errorMessage, e);
        }
    }
}
