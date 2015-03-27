package org.jboss.narayana.rest.integration.test.common;

import org.jboss.logging.Logger;
import org.jboss.narayana.rest.integration.api.Participant;
import org.jboss.narayana.rest.integration.api.Vote;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public final class LoggingParticipant implements Participant, Serializable {

    private static final Logger LOGGER = Logger.getLogger(LoggingParticipant.class);

    private static final long serialVersionUID = 7584938841973602732L;

    private final List<String> invocations;

    private final Vote outcome;

    public LoggingParticipant(final Vote outcome) {
        this.outcome = outcome;
        this.invocations = new ArrayList<String>();
    }

    @Override
    public Vote prepare() {
        LOGGER.info("LoggingParticipant.prepare");
        invocations.add("prepare");
        return outcome;
    }

    @Override
    public void commit() {
        LOGGER.info("LoggingParticipant.commit");
        invocations.add("commit");
    }

    @Override
    public void commitOnePhase() {
        LOGGER.info("LoggingParticipant.commitOnePhase");
        invocations.add("commitOnePhase");
    }

    @Override
    public void rollback() {
        LOGGER.info("LoggingParticipant.rollback");
        invocations.add("rollback");
    }

    public List<String> getInvocations() {
        return invocations;
    }

    public Vote getOutcome() {
        return outcome;
    }

}
