package org.jboss.narayana.txframework.impl.handlers.wsat;

import org.jboss.narayana.txframework.api.configuration.trigger.ATLifecycleEvent;
import org.jboss.narayana.txframework.api.management.ATTxControl;
import org.jboss.narayana.txframework.impl.TXControlException;

public class ATTxControlImpl implements ATTxControl
{

    public ATTxControlImpl()
    {
    }

    public void readOnly(ATLifecycleEvent event)
    {
        //todo: implement
    }

    public void readOnly() throws TXControlException
    {
        //todo: implement
    }

    public void fail() throws TXControlException
    {
        //todo: implement
    }
}
