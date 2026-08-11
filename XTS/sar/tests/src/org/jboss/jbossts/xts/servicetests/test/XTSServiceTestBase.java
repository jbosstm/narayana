package org.jboss.jbossts.xts.servicetests.test;

import org.jboss.jbossts.xts.servicetests.service.XTSServiceTestInterpreter;

/**
 * Class providing default behaviour for specific XTSServiceTest implementations. n.b. this class
 * extends
 */
public class XTSServiceTestBase extends XTSServiceTestInterpreter
{
    protected boolean isSuccessful;
    protected Exception exception;

    protected XTSServiceTestBase()
    {
        isSuccessful = false;
        exception = null;
    }

    public boolean isSuccessful() {
        return isSuccessful;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Exception getException() {
        return exception;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void error(String message)
    {
        System.err.println(this.getClass().getName() + " : " + message);
    }

    public void message(String message)
    {
        System.out.println(this.getClass().getName() + " : " + message);
    }
}
