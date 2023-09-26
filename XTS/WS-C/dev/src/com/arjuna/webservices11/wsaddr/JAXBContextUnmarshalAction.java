/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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