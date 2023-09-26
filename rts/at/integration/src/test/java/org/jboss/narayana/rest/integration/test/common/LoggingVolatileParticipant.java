/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.narayana.rest.integration.test.common;

import org.jboss.jbossts.star.util.TxStatus;
import org.jboss.narayana.rest.integration.api.VolatileParticipant;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public final class LoggingVolatileParticipant implements VolatileParticipant {

    private final List<String> invocations = new ArrayList<String>();

    private TxStatus txStatus;

    @Override
    public void beforeCompletion() {
        invocations.add("beforeCompletion");
    }

    @Override
    public void afterCompletion(TxStatus txStatus) {
        invocations.add("afterCompletion");
        this.txStatus = txStatus;
    }

    public List<String> getInvocations() {
        return invocations;
    }

    public TxStatus getTxStatus() {
        return txStatus;
    }
}
