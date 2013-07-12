package org.jboss.narayana.rest.integration.test.common;

import java.util.List;

import org.jboss.narayana.rest.integration.api.HeuristicException;
import org.jboss.narayana.rest.integration.api.HeuristicType;
import org.jboss.narayana.rest.integration.api.Participant;
import org.jboss.narayana.rest.integration.api.Vote;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public final class HeuristicParticipant implements Participant {

    private static final long serialVersionUID = 7717601991407596309L;

    private final LoggingParticipant loggingParticipant;

    private final HeuristicType heuristicType;

    public HeuristicParticipant(final HeuristicType heuristicType, final Vote outcome) {
        this.heuristicType = heuristicType;
        loggingParticipant = new LoggingParticipant(outcome);
    }

    @Override
    public Vote prepare() {
        return loggingParticipant.prepare();
    }

    @Override
    public void commit() throws HeuristicException {
        loggingParticipant.commit();

        if (heuristicType != null) {
            throw new HeuristicException(heuristicType);
        }
    }

    @Override
    public void commitOnePhase() {
        loggingParticipant.commitOnePhase();
    }

    @Override
    public void rollback() throws HeuristicException {
        loggingParticipant.rollback();

        if (heuristicType != null) {
            throw new HeuristicException(heuristicType);
        }
    }

    public List<String> getInvocations() {
        return loggingParticipant.getInvocations();
    }

}
