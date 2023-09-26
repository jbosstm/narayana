/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.mw.wst11.common;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import java.security.PrivilegedExceptionAction;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class CoordinationContextAction implements PrivilegedExceptionAction<JAXBContext> {

    private static final CoordinationContextAction INSTANCE = new CoordinationContextAction();

    private CoordinationContextAction() {

    }

    public static CoordinationContextAction getInstance() {
        return INSTANCE;
    }

    @Override
    public JAXBContext run() throws JAXBException {
        return JAXBContext.newInstance("org.oasis_open.docs.ws_tx.wscoor._2006._06");
    }

}