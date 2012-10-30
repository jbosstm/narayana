package org.jboss.narayana.txframework.impl.handlers.wsba;

import com.arjuna.wst11.BAParticipantManager;
import org.jboss.narayana.txframework.api.configuration.trigger.BALifecycleEvent;
import org.jboss.narayana.txframework.api.exception.TXControlException;
import org.jboss.narayana.txframework.api.management.WSBATxControl;

public class WSBATxControlImpl implements WSBATxControl
{
    static final ThreadLocal<BAParticipantManager> baParticipantManagerThreadLocal = new ThreadLocal<BAParticipantManager>();

    //todo: Need to hook into lifecycle or record it here.
    static final ThreadLocal<Boolean> cannotCompleteThreadLocal = new ThreadLocal<Boolean>();

    public WSBATxControlImpl()
    {
        
    }


    public static void resume(BAParticipantManager baParticipantManager)
    {
        baParticipantManagerThreadLocal.set(baParticipantManager);
        cannotCompleteThreadLocal.set(false);
    }

    public static void suspend()
    {
        baParticipantManagerThreadLocal.remove();
    }

    public void exit() throws TXControlException
    {
        try
        {
            baParticipantManagerThreadLocal.get().exit();
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
            baParticipantManagerThreadLocal.get().cannotComplete();
            cannotCompleteThreadLocal.set(true);
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
            baParticipantManagerThreadLocal.get().exit();
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
            baParticipantManagerThreadLocal.get().completed();
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
            baParticipantManagerThreadLocal.get().exit();
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
            baParticipantManagerThreadLocal.get().fail(null);
        }
        catch (Exception e)
        {
            throw new TXControlException("Exception when calling 'fail' on participant manager", e);
        }
    }

    public boolean isCannotComplete()
    {
        return cannotCompleteThreadLocal.get();
    }
}
