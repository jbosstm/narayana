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
public interface XAResourceConsumer {

    void accept(XAResource xaResource) throws XAException;

}