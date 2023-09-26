/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.webservices.base.processors;

import java.util.HashMap;
import java.util.Map;

import com.arjuna.webservices.logging.WSCLogger;

/**
 * Utility class handling common callback functionality.
 * @author kevin
 */
public abstract class BaseProcessor
{
    /**
     * The callback map.
     */
    private final Map callbackMap = new HashMap() ;
    
    /**
     * Register the callback for the message ID.
     * @param messageID The message ID.
     * @param callback The callback.
     */
    protected void register(final String messageID, final Callback callback)
    {
        synchronized(callbackMap)
        {
            callbackMap.put(messageID, callback) ;
        }
    }
    
    /**
     * Remove the callback for the specified message ID.
     * @param messageID The message ID.
     */
    public void removeCallback(final String messageID)
    {
        synchronized(callbackMap)
        {
            callbackMap.remove(messageID) ;
        }
    }

    /**
     * Handle the callbacks for the specified addressing context.
     * @param executor The callback executor.
     * @param ids The message ids.
     * 
     */
    protected void handleCallbacks(final CallbackExecutor executor, final String[] ids)
    {
        final Callback[] callbacks = getCallbacks(ids) ;
        if (callbacks != null)
        {
            boolean executed = false ;
            final int numCallbacks = callbacks.length ;
            for(int count = 0 ; count < numCallbacks ; count++)
            {
                final Callback callback = callbacks[count] ;
                if (callback != null)
                {
                    executed = true ;
                    try
                    {
                        executor.execute(callback) ;
                        callback.setTriggered() ;
                    }
                    catch (final Throwable th)
                    {
                        if (WSCLogger.logger.isTraceEnabled())
                        {
                            WSCLogger.logger.tracev("Unexpected throwable while executing callback:", th) ;
                        }
                        callback.setFailed() ;
                    }
                }
            }
            if (!executed && WSCLogger.logger.isTraceEnabled())
            {
                executor.executeUnknownIds(ids) ;
            }
        }
    }
    
    /**
     * Get the callbacks associated with the message ids.
     * @param ids The message ids.
     * @return The callbacks associated with the message ids in the addressing context.
     */
    private Callback[] getCallbacks(final String[] ids)
    {
        if (ids == null)
        {
            return null ;
        }
        
        final int numIDs = ids.length ;
        final Callback[] callbacks = new Callback[numIDs] ;
        synchronized(callbackMap)
        {
            for(int count = 0 ; count < numIDs ; count++)
            {
                callbacks[count] = (Callback)callbackMap.get(ids[count]) ;
            }
        }
        return callbacks ;
    }
    
    /**
     * Interface for executing a specific callback.
     * @author kevin
     */
    protected static interface CallbackExecutor
    {
        /**
         * The execute method.
         * @param callback The callback instance.
         */
        public void execute(final Callback callback) ;
        /**
         * Execute method for an unknown identifier.
         * @param ids The current ids.
         */
        public void executeUnknownIds(final String[] ids) ;
    }
    
    /**
     * Adapter for the callback executor.
     * @author kevin
     */
    protected abstract static class CallbackExecutorAdapter implements CallbackExecutor
    {
        /**
         * Execute method for an unknown identifier.
         * @param ids The current ids.
         */
        public void executeUnknownIds(final String[] ids)
        {
            WSCLogger.logger.tracev("Received a response for non existent message IDs {0}", new Object[] {toString(ids)}) ;
        }
        
        /**
         * Convert an array of IDs to a comma separated string representation.
         * @param ids The ids.
         * @return The string representation.
         */
        private String toString(final String[] ids)
        {
            final int numIDs = (ids == null ? 0 : ids.length) ;
            if (numIDs == 0)
            {
                return "" ;
            }
            else if (numIDs == 1)
            {
                return ids[0] ;
            }
            else
            {
                final StringBuffer buffer = new StringBuffer(ids[0]) ;
                for(int count = 1 ; count < numIDs ; count++)
                {
                    buffer.append(", ") ;
                    buffer.append(ids[count]) ;
                }
                return buffer.toString() ;
            }
        }
    }
}