package org.jboss.narayana.rest.integration.api;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public interface Participant {

    Vote prepare();

    void commit() throws HeuristicException;

    void commitOnePhase();

    void rollback() throws HeuristicException;

}
