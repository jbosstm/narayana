/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.webservices11.wsaddr;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import java.security.PrivilegedExceptionAction;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class NativeEndpointReferenceContextAction implements PrivilegedExceptionAction<JAXBContext> {

    private static final NativeEndpointReferenceContextAction INSTANCE = new NativeEndpointReferenceContextAction();

    private NativeEndpointReferenceContextAction() {

    }

    public static NativeEndpointReferenceContextAction getInstance() {
        return INSTANCE;
    }

    @Override
    public JAXBContext run() throws JAXBException {
        return JAXBContext.newInstance(NativeEndpointReference.class);
    }

}