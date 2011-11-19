package org.jboss.jbossts.txframework.impl.handlers.wsat;

import com.arjuna.wst11.BAParticipantManager;
import org.jboss.jbossts.txframework.api.configuration.trigger.ATLifecycleEvent;
import org.jboss.jbossts.txframework.api.configuration.trigger.BALifecycleEvent;
import org.jboss.jbossts.txframework.api.management.ATTxControl;
import org.jboss.jbossts.txframework.api.management.WSBATxControl;
import org.jboss.jbossts.txframework.impl.TXControlException;

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
