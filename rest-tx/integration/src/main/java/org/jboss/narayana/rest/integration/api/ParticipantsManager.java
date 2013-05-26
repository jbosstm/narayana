package org.jboss.narayana.rest.integration.api;

import org.jboss.jbossts.star.util.TxSupport;

import com.arjuna.ats.arjuna.common.Uid;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public interface ParticipantsManager {

    String getBaseUrl();

    void setBaseUrl(String baseUrl);

    Uid enlist(String participantEnlistmentURL, Participant participant);

    Uid enlist(TxSupport txSupport, Participant participant);

    void reportHeuristic(Uid participantId, HeuristicType heuristicType);

}
