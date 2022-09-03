/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package com.arjuna.webservices11.wsaddr;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import javax.xml.transform.Source;
import jakarta.xml.ws.EndpointReference;
import jakarta.xml.ws.WebServiceException;
import java.security.PrivilegedAction;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public final class JAXBContextUnmarshalAction<T extends EndpointReference> implements PrivilegedAction<T> {

    private final JAXBContext jaxbContext;

    private final Source source;

    private final Class<T> endpointReferenceClass;

    private JAXBContextUnmarshalAction(final JAXBContext jaxbContext, final Source source,
            final Class<T> endpointReferenceClass) {

        this.jaxbContext = jaxbContext;
        this.source = source;
        this.endpointReferenceClass = endpointReferenceClass;
    }

    public static <T extends EndpointReference> JAXBContextUnmarshalAction<T> getInstance(
            final JAXBContext jaxbContext, final Source source, final Class<T> endpointReferenceClass) {

        return new JAXBContextUnmarshalAction<>(jaxbContext, source, endpointReferenceClass);
    }

    @Override
    public T run() {
        try {
            return jaxbContext.createUnmarshaller().unmarshal(source, endpointReferenceClass).getValue();
        } catch (final JAXBException e) {
            throw new WebServiceException("Error unmarshalling NativeEndpointReference ", e);
        } catch (final ClassCastException e) {
            throw new WebServiceException("Source did not contain NativeEndpointReference", e);
        }
    }

}
