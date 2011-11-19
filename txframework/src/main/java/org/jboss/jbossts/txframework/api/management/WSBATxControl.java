package org.jboss.jbossts.txframework.api.management;

import org.jboss.jbossts.txframework.api.exception.TXFrameworkException;
import org.jboss.jbossts.txframework.impl.TXControlException;

/**
 * Interface defining a transaction control object which can be injected into a framework web service or
 * lifecycle method for the Web Service Business Activity protocol.
 */
public interface WSBATxControl extends BATxControl
{
    public void exit() throws TXControlException;
    public void cannotComplete() throws TXControlException;
}
