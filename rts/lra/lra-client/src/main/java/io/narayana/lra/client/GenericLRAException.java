/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
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
package io.narayana.lra.client;

import javax.ws.rs.WebApplicationException;
import java.net.URL;

public class GenericLRAException extends WebApplicationException {
    private final String lraId;
    private final int statusCode;

    /**
     * Generic LRA exception.
     *
     * @param lraId  URL of lra id that this exception relates to
     * @param statusCode  error status code behind the creation of this exception
     * @param message  error exception message
     * @param cause  cause of the exception
     */
    public GenericLRAException(URL lraId, int statusCode, String message, Throwable cause) {
        this((String) (lraId == null ? null : lraId.toString()), statusCode, message, cause);
    }

    /**
     * Generic LRA exception.
     *
     * @param statusCode  error status code behind the creation of this exception
     * @param message  error exception message
     */
    public GenericLRAException(int statusCode, String message) {
        super(message);

        this.lraId = null;
        this.statusCode = statusCode;
    }

    /**
     * Generic LRA exception.
     *
     * @param lraId  URL of lra id that this exception relates to
     * @param statusCode  error status code behind the creation of this exception
     */
    public GenericLRAException(URL lraId, int statusCode) {
        this.lraId = lraId == null ? null : lraId.toString();
        this.statusCode = statusCode;
    }

    /**
     * Generic LRA exception.
     *
     * @param lraId  URL of lra id that this exception relates to
     * @param statusCode  error status code behind the creation of this exception
     * @param message  error exception message
     */
    public GenericLRAException(URL lraId, int statusCode, String message) {
        super(String.format("%s, lra id: %s", message, lraId));

        this.lraId = lraId == null ? null : lraId.toString();
        this.statusCode = statusCode;
    }

    /**
     * Generic LRA exception.
     *
     * @param statusCode  error status code behind the creation of this exception
     * @param message  error exception message
     * @param cause  cause of why this exception is created
     */
    public GenericLRAException(int statusCode, String message, Throwable cause) {
        super(message, cause);

        this.lraId = null;
        this.statusCode = statusCode;
    }

    /**
     * Generic LRA exception.
     *
     * @param lraId  URL of lra id that this exception relates to
     * @param statusCode  error status code behind the creation of this exception
     * @param message  error exception message
     * @param cause  cause of the exception
     */
    public GenericLRAException(String lraId, int statusCode, String message, Throwable cause) {
        super(String.format("%s, lra id: %s", message, lraId), cause);

        this.lraId = lraId;
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getLraId() {
        return lraId;
    }
}
