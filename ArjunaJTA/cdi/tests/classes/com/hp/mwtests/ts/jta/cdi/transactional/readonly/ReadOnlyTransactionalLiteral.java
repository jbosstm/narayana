/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.cdi.transactional.readonly;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.transaction.Transactional;

/**
 * A Transactional literal marked read-only. The isReadOnly method is
 * deliberately not annotated with @Override so this compiles whether or not
 * the Transactional interface declares it (it only does from
 * jakarta.transaction-api 2.0.2); the interceptor only sees the value when
 * the runtime API declares the method.
 */
class ReadOnlyTransactionalLiteral extends AnnotationLiteral<Transactional> implements Transactional {

    @Override
    public TxType value() {
        return TxType.REQUIRED;
    }

    @Override
    public Class[] rollbackOn() {
        return new Class[] {};
    }

    @Override
    public Class[] dontRollbackOn() {
        return new Class[] {};
    }

    public boolean isReadOnly() {
        return true;
    }

}
