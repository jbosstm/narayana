/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.jboss.transaction.wstf.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Message logging via the proxy.
 */
public class ProxyConversation
{
    /**
     * The conversation id.
     */
    private static int currentConversationId ;
    /**
     * The conversation map.
     */
    private static final Map<String, List<String>> conversationMap = new TreeMap<>() ;
    /**
     * The conversation state map.
     */
    private static final Map<String,ProxyConversationState> conversationStateMap = new TreeMap<>() ;
    
    /**
     * The marker for an internal id.
     */
    private static final char INTERNAL_ID = 'c' ;
    /**
     * The marker for an external id.
     */
    private static final char EXTERNAL_ID = 'C' ;
    
    /**
     * Create a conversation.
     * @return The id of the conversation. 
     */
    public static synchronized String createConversation()
    {
        final String conversationId = INTERNAL_ID + Integer.toString(++currentConversationId) ;
        conversationMap.put(mapConversationId(conversationId), new ArrayList<>()) ;
System.out.println("KEV: created conversation " + conversationId) ;        
        return conversationId ;
    }

    /**
     * Remove the conversation.
     * @param conversationId The conversation id.
     * @return The conversation messages in sequence.
     */
    public static synchronized String[] removeConversation(final String conversationId)
    {
        final List<String> list = conversationMap.remove(mapConversationId(conversationId)) ;
System.out.println("KEV: removed conversation " + conversationId) ;        
        return (list == null ? new String[0] : list.toArray(new String[0])) ;
    }
    
    /**
     * Append a message to the conversation.
     * @param conversationId The conversation id.
     * @param message The message to append to the conversation.
     */
    public static synchronized void appendConversation(final String conversationId, final String message)
    {
        final List<String> list = conversationMap.get(mapConversationId(conversationId)) ;
        if (list != null)
        {
            list.add(message) ;
        }
    }
    
    /**
     * Set the conversation state for the specified conversation.
     * @param conversationId The conversation id.
     * @param conversationState The conversation state.
     */
    public static synchronized void setConversationState(final String conversationId, final ProxyConversationState conversationState)
    {
        conversationStateMap.put(mapConversationId(conversationId), conversationState) ;
    }
    
    /**
     * Get the conversation state for the specified conversation.
     * @param conversationId The conversation id.
     * @return The conversation state or null.
     */
    public static synchronized ProxyConversationState getConversationState(final String conversationId)
    {
        return conversationStateMap.get(mapConversationId(conversationId)) ;
    }
    
    /**
     * Clear the conversation state for the specified conversation.
     * @param conversationId The conversation id.
     */
    public static synchronized void clearConversationState(final String conversationId)
    {
        conversationStateMap.remove(mapConversationId(conversationId)) ;
    }
    
    /**
     * Is the conversation id an internal id?
     * @param conversationId The conversation id.
     * @return True if the conversation id is internal, false if it is external.
     */
    public static boolean isInternalConversationId(final String conversationId)
    {
        return ((conversationId != null) && (conversationId.length() != 0) && (conversationId.charAt(0) == INTERNAL_ID)) ;
    }
    
    /**
     * Get the alternate conversation id.
     * @param conversationId The current conversation id.
     * @return The alternate conversation id.
     */
    public static String getAlternateConversationId(final String conversationId)
    {
        if ((conversationId == null) || (conversationId.length() == 0))
        {
            return conversationId ;
        }
        if (conversationId.charAt(0) == INTERNAL_ID)
        {
            return EXTERNAL_ID + mapConversationId(conversationId) ;
        }
        else
        {
            return INTERNAL_ID + mapConversationId(conversationId) ;
        }
    }
    
    /**
     * Get the map conversation id.
     * @param conversationId The full conversation id.
     * @return The conversation id for the map.
     */
    private static String mapConversationId(final String conversationId)
    {
        if ((conversationId == null) || (conversationId.length() == 0))
        {
            return conversationId ;
        }
        return conversationId.substring(1) ;
    }
}