/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat Middleware LLC, and individual contributors
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

package com.hp.mwtests.ts.jta.cdi.transactional.stereotype;

import jakarta.annotation.Resource;
import jakarta.enterprise.inject.Stereotype;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

@TransactionalRequiredStereotype
public class StereotypeBean {

    @Resource(mappedName = "java:jboss/TransactionManager")
    private TransactionManager txnMgr;

    /**
     * Method using {@link Transactional} declaration inherited from
     * {@link Stereotype} defined at the class level.
     */
    public void stereotypeRequiredAtBean() throws SystemException {
        // expected @TransactionalRequiredStereotype with REQUIRED
        if (Status.STATUS_ACTIVE != txnMgr.getStatus())
            throw new AssertionError("@TransactionalRequiredStereotype defines having an active txn");
    }

    /**
     * Method rewriting the {@link Transactional} annotation in {@link Stereotype}
     * from the class level and changing it by direct usage of the annotation.
     */
    @Transactional(value = TxType.NEVER)
    public void transactionalAtMethod() throws SystemException {
        // expected @Transactional with NEVER
        if (Status.STATUS_NO_TRANSACTION != txnMgr.getStatus())
            throw new AssertionError("@Transactional/NEVER defines no active transaction expected");
    }
}
