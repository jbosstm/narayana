package org.jboss.jbossts.txframework.api.management;

import org.jboss.jbossts.txframework.api.configuration.trigger.BALifecycleEvent;
import org.jboss.jbossts.txframework.impl.TXControlException;

/**
 * Interface defining a transaction control object which can be injected into a framework web service or
 * lifecycle method for one of the Business Activity protocols.
 */
public interface BATxControl extends TxControl
{
    public void readOnly(BALifecycleEvent event) throws TXControlException;
    public void completed() throws TXControlException;
}