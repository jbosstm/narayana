package com.jboss.transaction.txinterop.interop.states;

/**
 * A conversation state that waits for a specified action before completing.
 */
public class InteropWaitState extends BaseState
{
    /**
     * The last action.
     */
    private final String lastAction ;
    /**
     * The action count.
     */
    private int count ;
    
    /**
     * Construct the conversation state.
     * @param lastAction The last action of the conversation.
     */
    public InteropWaitState(final String lastAction)
    {
        this(lastAction, 1) ;
    }
    
    /**
     * Construct the conversation state.
     * @param lastAction The last action of the conversation.
     * @param count The occurrance to trigger on.
     */
    public InteropWaitState(final String lastAction, final int count)
    {
        this.lastAction = lastAction ;
        this.count = count ;
    }
    
    /**
     * Handle the next action in the sequence.
     * @param action The SOAP action.
     * @param identifier The identifier associated with the endpoint.
     * @return true if the message should be dropped, false otherwise.
     */
    public boolean handleAction(final String action, final String identifier)
    {
        if (lastAction.equals(action))
        {
            if (--count == 0)
            {
                success() ;
            }
        }
        return false ;
    }
}
