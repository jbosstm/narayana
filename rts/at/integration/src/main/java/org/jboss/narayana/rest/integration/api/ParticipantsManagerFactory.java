/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.narayana.rest.integration.api;

import org.jboss.narayana.rest.integration.ParticipantsManagerImpl;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public final class ParticipantsManagerFactory {

    private static final ParticipantsManager INSTANCE = new ParticipantsManagerImpl();

    public static ParticipantsManager getInstance() {
        return INSTANCE;
    }

}
