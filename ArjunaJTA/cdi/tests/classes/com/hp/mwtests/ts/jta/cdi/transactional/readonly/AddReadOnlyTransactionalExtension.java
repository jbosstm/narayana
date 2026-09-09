/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.cdi.transactional.readonly;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;

import com.hp.mwtests.ts.jta.cdi.transactional.stereotype.extension.AnnotatedTypeWrapper;

/**
 * Adds a read-only Transactional annotation to {@link ReadOnlyBean}.
 */
public class AddReadOnlyTransactionalExtension implements Extension {

    void processAnnotatedType(@Observes ProcessAnnotatedType<ReadOnlyBean> bean) {
        AnnotatedTypeWrapper<ReadOnlyBean> wrapper = new AnnotatedTypeWrapper<ReadOnlyBean>(bean);
        wrapper.addAnnotation(new ReadOnlyTransactionalLiteral());
        bean.setAnnotatedType(wrapper);
    }
}
