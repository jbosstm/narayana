/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.ts.jta.cdi.transactional.stereotype.extension;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.transaction.Transactional;


/**
 * Extension adding the {@link Transactional} at any processed bean.
 */
public class AddTransactionalAnnotationExtension implements Extension {

    void processAnnotatedType(@Observes ProcessAnnotatedType<NoAnnotationBean> bean) {
        AnnotatedTypeWrapper<NoAnnotationBean> wrapper = new AnnotatedTypeWrapper<NoAnnotationBean>(bean);
        wrapper.addAnnotation(new TransactionalLiteral());
        bean.setAnnotatedType(wrapper);
    }
}