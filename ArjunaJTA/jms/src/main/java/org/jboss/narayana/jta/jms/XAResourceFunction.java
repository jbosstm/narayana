/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.jta.jms;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@FunctionalInterface
public interface XAResourceFunction<T> {

    T apply(XAResource xaResource) throws XAException;

}