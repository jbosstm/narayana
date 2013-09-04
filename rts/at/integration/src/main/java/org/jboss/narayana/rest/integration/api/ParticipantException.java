package org.jboss.narayana.rest.integration.api;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class ParticipantException extends RuntimeException {

    public ParticipantException() {
        super();
    }

    public ParticipantException(final String message) {
        super(message);
    }

    public ParticipantException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
