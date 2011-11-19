package org.jboss.jbossts.txframework.api.configuration.trigger;

/**
 * values identifying each of the possible atomic transaction lifecycle events for which lifecycle handlees
 * can be registered
 */
public enum ATLifecycleEvent
{
    /**
     * lifecycle event which happens just prior to the prepare operation while the transaction is still running
     */
    PRE_PREPARE,
    /**
     * lifecycle event which happens at prepare when the transaction is no longer running
     */
    PREPARE,
    /**
     * lifecycle event which happens if all service requests executed in the transaction have read only outcomes
     */
    READ_ONLY,
    /**
     * lifecycle event which happens if all the transaction is rolled back
     */
    ABORT,
    /**
     * lifecycle event which happens if the transaction commits
     */
    COMMIT,
    /**
     * lifecycle event which happens after commit or rollback
     */
    POST_COMMIT
}
