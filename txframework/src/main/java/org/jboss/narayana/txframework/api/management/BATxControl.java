package org.jboss.narayana.txframework.api.management;

import org.jboss.narayana.txframework.api.configuration.trigger.BALifecycleEvent;
import org.jboss.narayana.txframework.api.exception.TXControlException;

/**
 * Interface defining a transaction control object which can be injected into a framework web service or
 * lifecycle method for one of the Business Activity protocols.
 */
public interface BATxControl extends TxControl
{
    public void readOnly(BALifecycleEvent event) throws TXControlException;
    public void completed() throws TXControlException;
}