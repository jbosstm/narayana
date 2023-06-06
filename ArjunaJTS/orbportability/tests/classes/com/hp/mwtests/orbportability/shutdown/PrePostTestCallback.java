/*
 * SPDX short identifier: Apache-2.0
 */



package com.hp.mwtests.orbportability.shutdown;

/**
 * @author Richard Begg
 */
public interface PrePostTestCallback
{
    public void preShutdownCalled(String name);
    
    public void postShutdownCalled(String name);
}