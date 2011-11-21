package org.jboss.jbossts.txframework.api.management;

import org.jboss.jbossts.txframework.impl.TXControlException;
import java.io.Serializable;

/**
 * Interface defining at the most generic level a transaction control object which can be injected into a
 * framework web service or lifecycle method. Instances of this type and its subtypes can be uses
 * to type fields which are the target of a TxManagement attribute. A TxControl provides a set of
 * operations which the web service or lifecycle method can use to influence the logic of the
 * transaction.
 */
public interface TxControl
{
    /**
     * this method can be called from a web service method while a transaction is active to notify the
     * framework that no changes have been made during execution of the service method.
     */
    public void readOnly() throws TXControlException;
    /**
     * this method can be called from a web service or lifecycle method to notify the framework that the
     * transaction has failed and may have left the service in an inconsistent state
     */
    public void fail() throws TXControlException;
}
