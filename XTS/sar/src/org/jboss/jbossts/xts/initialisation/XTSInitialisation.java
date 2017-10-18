package org.jboss.jbossts.xts.initialisation;

/**
 * interface allowing initialization code to be plugged into the XTS Service startup
 */
public interface XTSInitialisation
{
    public void startup() throws Exception;
    public void shutdown()throws Exception;
}
