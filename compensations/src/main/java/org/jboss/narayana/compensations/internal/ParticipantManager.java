package org.jboss.narayana.compensations.internal;


import javax.xml.namespace.QName;

/**
 * Manager used to notify transaction coordinator about participant actions.
 *
 * @author paul.robinson@redhat.com 19/04/2014
 */
public interface ParticipantManager {

    /**
     * Notify transaction coordinator that participant has decided to leave the transaction and that it shouldn't be contacted.
     *
     * @throws Exception
     */
    public void exit() throws Exception;


    /**
     * Notify transaction coordinator that all work was completed and participant is ready to complete the transaction.
     *
     * @throws Exception
     */
    public void completed() throws Exception;

    /**
     * Notify transaction coordinator that participant cannot complete.
     *
     * @throws Exception
     */
    public void cannotComplete() throws Exception;

    /**
     * Notify transaction coordinator that participant failed.
     *
     * @param exceptionIdentifier
     * @throws Exception
     */
    public void fail(final QName exceptionIdentifier) throws Exception;
}
