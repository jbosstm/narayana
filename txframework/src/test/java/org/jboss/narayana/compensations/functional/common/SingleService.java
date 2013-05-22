/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package org.jboss.narayana.compensations.functional.common;

import org.jboss.narayana.compensations.api.Compensatable;
import org.jboss.narayana.compensations.api.TxCompensate;
import org.jboss.narayana.compensations.api.TxLogged;
import org.jboss.narayana.compensations.api.TxConfirm;

import javax.inject.Inject;

/**
 * @author paul.robinson@redhat.com 22/03/2013
 */
public class SingleService {

    @Inject
    DummyData dummyData;

    @Compensatable
    @TxCompensate(DummyCompensationHandler1.class)
    @TxConfirm(DummyConfirmationHandler1.class)
    @TxLogged(DummyTransactionLoggedHandler1.class)
    public void testSingle1(boolean throwException) throws MyRuntimeException {

        dummyData.setValue("blah1");

        if (throwException) {
            throw new MyRuntimeException();
        }
    }

    @Compensatable
    @TxCompensate(DummyCompensationHandler2.class)
    @TxConfirm(DummyConfirmationHandler2.class)
    @TxLogged(DummyTransactionLoggedHandler2.class)
    public void testSingle2(boolean throwException) throws MyRuntimeException {

        dummyData.setValue("blah2");

        if (throwException) {
            throw new MyRuntimeException();
        }
    }

    @Compensatable
    @TxCompensate(DummyCompensationHandler3.class)
    @TxConfirm(DummyConfirmationHandler3.class)
    @TxLogged(DummyTransactionLoggedHandler3.class)
    public void testSingle3(boolean throwException) throws MyRuntimeException {

        dummyData.setValue("blah2");

        if (throwException) {
            throw new MyRuntimeException();
        }
    }

    @TxCompensate(DummyCompensationHandler1.class)
    public void noTransactionPresent() throws MyRuntimeException {

    }
}
