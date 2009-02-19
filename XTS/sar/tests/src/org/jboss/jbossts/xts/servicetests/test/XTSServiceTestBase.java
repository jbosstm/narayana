package org.jboss.jbossts.xts.servicetests.test;

/**
 * Class providing default behaviour for specific XTSServiceTest implementations
 */
public class XTSServiceTestBase
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
}
