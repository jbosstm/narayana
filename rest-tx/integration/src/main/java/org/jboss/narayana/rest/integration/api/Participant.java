package org.jboss.narayana.rest.integration.api;

import java.io.Serializable;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public interface Participant extends Serializable {

    Vote prepare();

    void commit() throws HeuristicException;

    void commitOnePhase();

    void rollback() throws HeuristicException;

}
