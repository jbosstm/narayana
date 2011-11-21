package org.jboss.jbossts.txframework.impl.handlers.wsba;

import com.arjuna.wst.SystemException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst11.BAParticipantManager;
import org.jboss.jbossts.txframework.api.configuration.trigger.BALifecycleEvent;
import org.jboss.jbossts.txframework.api.exception.TXFrameworkException;
import org.jboss.jbossts.txframework.api.management.WSBATxControl;
import org.jboss.jbossts.txframework.impl.TXControlException;

public class WSBATxControlImpl implements WSBATxControl
{
    BAParticipantManager baParticipantManager;
    //todo: Need to hook into lifecycle or record it here.
    private boolean cannotComplete = false;

    public WSBATxControlImpl(BAParticipantManager baParticipantManager)
    {
        this.baParticipantManager = baParticipantManager;
    }

    public void exit() throws TXControlException
    {
        try
        {
            baParticipantManager.exit();
        }
        catch (Exception e)
        {
            throw new TXControlException("Exception when calling 'exit' on participant manager", e);
        }
    }

    public void cannotComplete() throws TXControlException
    {
        try
        {
            baParticipantManager.cannotComplete();
            cannotComplete = true;
        }
        catch (Exception e)
        {
            throw new TXControlException("Exception when calling 'cannotComplete' on participant manager", e);
        }
    }

    public void readOnly(BALifecycleEvent event) throws TXControlException
    {
        //todo: what is the BALifecycleEvent for?
        try
        {
            baParticipantManager.exit();
        }
        catch (Exception e)
        {
            throw new TXControlException("Exception when calling 'exit' on participant manager", e);
        }
    }

    public void completed() throws TXControlException
    {
        try
        {
            baParticipantManager.completed();
        }
        catch (Exception e)
        {
            throw new TXControlException("Exception when calling 'completed' on participant manager", e);
        }
    }

    public void readOnly() throws TXControlException
    {
        try
        {
            baParticipantManager.exit();
        }
        catch (Exception e)
        {
            throw new TXControlException("Exception when calling 'exit' on participant manager", e);
        }
    }

    public void fail() throws TXControlException
    {
        try
        {
            //todo: Why does this take a QName?
            baParticipantManager.fail(null);
        }
        catch (Exception e)
        {
            throw new TXControlException("Exception when calling 'fail' on participant manager", e);
        }
    }

    public boolean isCannotComplete()
    {
        return cannotComplete;
    }
}
