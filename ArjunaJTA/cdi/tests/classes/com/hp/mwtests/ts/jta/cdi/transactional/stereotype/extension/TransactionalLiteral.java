/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.ts.jta.cdi.transactional.stereotype.extension;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.transaction.Transactional;

class TransactionalLiteral extends AnnotationLiteral<Transactional> implements Transactional {

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

}