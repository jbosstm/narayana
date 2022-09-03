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

@TransitiveToRequiredTransactionalNever
public class StereotypeWithTransactionalBean {

    @Resource(mappedName = "java:jboss/TransactionManager")
    private TransactionManager txnMgr;

    /**
     * Having declared the {@link Transactional} annotation in {@link Stereotype}
     * last in the transitive row and in the middle one too.
     */
    public void process() throws SystemException, TestException {
        // expected: @TransitiveToRequiredTransactionalNever with NEVER
        if (Status.STATUS_NO_TRANSACTION != txnMgr.getStatus())
            throw new TestException();

        // not expected/fail:
        throw new AssertionError("There was active transaction even NEVER is expected to be taken"
            + " as Transactional attribute");
    }
}
