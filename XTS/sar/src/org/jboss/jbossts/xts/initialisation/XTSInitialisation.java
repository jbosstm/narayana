package org.jboss.jbossts.xts.initialisation;

/**
 * interface allowing initialisation code to eb ;plugged into the XTS Service startup
 */
public interface XTSInitialisation
{
    public void startup() throws Exception;
    public void shutdown()throws Exception;
}
