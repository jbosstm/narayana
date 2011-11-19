package org.jboss.jbossts.txframework.api.management;

import org.jboss.jbossts.txframework.api.configuration.trigger.ATLifecycleEvent;

/**
 * Interface defining a transaction control object which can be injected into a framework web service or
 * lifecycle method for one of the Atomic Transaction protocols.
 */
public interface ATTxControl extends TxControl
{
    public void readOnly(ATLifecycleEvent event);
}