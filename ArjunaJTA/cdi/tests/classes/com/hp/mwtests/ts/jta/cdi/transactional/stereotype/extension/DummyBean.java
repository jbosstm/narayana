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
