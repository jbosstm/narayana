/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.ts.jta.cdi.transactional.stereotype.extension;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;

import com.hp.mwtests.ts.jta.cdi.transactional.stereotype.TransactionalRequiredStereotype;


/**
 * Extension adding the {@link TransactionalRequiredStereotype}
 * at any processed bean.
 */
public class AddStereotypeAnnotationExtension implements Extension {

    void processAnnotatedType(@Observes ProcessAnnotatedType<NoAnnotationBean> bean) {
        AnnotatedTypeWrapper<NoAnnotationBean> wrapper = new AnnotatedTypeWrapper<NoAnnotationBean>(bean);
        wrapper.addAnnotation(TransactionalRequiredStereotype.class);
        bean.setAnnotatedType(wrapper);
    }
}