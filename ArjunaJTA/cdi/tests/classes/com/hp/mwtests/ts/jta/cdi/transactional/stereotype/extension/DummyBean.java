/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.ts.jta.cdi.transactional.stereotype.extension;

import jakarta.transaction.Transactional;

/**
 * <p>
 * This bean with {@link Transactional} annotation is needed to be part
 * of the test deployment, otherwise the CDI Narayana transactional
 * interceptors won't be activated (set in WFLY integration code at
 * <code>org.jboss.as.txn.deployment.TransactionDependenciesProcessor.deploy(DeploymentPhaseContext)</code>).
 * <p>
 * The {@link NoAnnotationBean} is deliberately defined without the annotation
 * and the annotation is add later by extension which is not seen by the processor in WFLY.
 */
@Transactional
public class DummyBean {
}