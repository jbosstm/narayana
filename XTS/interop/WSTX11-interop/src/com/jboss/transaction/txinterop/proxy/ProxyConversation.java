/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.jboss.transaction.txinterop.proxy;

import java.util.ArrayList;
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
    private static Map conversationMap = new TreeMap() ;
    /**
     * The conversation state map.
     */
    private static Map conversationStateMap = new TreeMap() ;
    
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
        conversationMap.put(mapConversationId(conversationId), new ArrayList()) ;
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
        final ArrayList list = (ArrayList)conversationMap.remove(mapConversationId(conversationId)) ;
System.out.println("KEV: removed conversation " + conversationId) ;        
        return (list == null ? new String[0] : (String[])list.toArray(new String[list.size()])) ;
    }
    
    /**
     * Append a message to the conversation.
     * @param conversationId The conversation id.
     * @param message The message to append to the conversation.
     */
    public static synchronized void appendConversation(final String conversationId, final String message)
    {
        final ArrayList list = (ArrayList)conversationMap.get(mapConversationId(conversationId)) ;
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
        return (ProxyConversationState)conversationStateMap.get(mapConversationId(conversationId)) ;
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
