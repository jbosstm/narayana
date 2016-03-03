package org.jboss.narayana.rest.integration.api;

import org.jboss.jbossts.star.util.TxStatus;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public interface VolatileParticipant {

    void beforeCompletion();

    void afterCompletion(final TxStatus txStatus);

}
