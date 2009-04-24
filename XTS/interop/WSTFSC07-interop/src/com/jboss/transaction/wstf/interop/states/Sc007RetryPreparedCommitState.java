package com.jboss.transaction.wstf.interop.states;

import com.arjuna.webservices11.wsat.AtomicTransactionConstants;

/**
 * A conversation state for retry prepared commit state.
 */
public class Sc007RetryPreparedCommitState extends BaseState
{
    /**
     * The prepared count.
     */
    private int preparedCount ;
    /**
     * The committed count.
     */
    private int committedCount ;
    /**
     * The first identifier.
     */
    private String firstIdentifier ;
    /**
     * Are we dropping other messages?
     */
    private boolean drop ;
    
    /**
     * Handle the next action in the sequence.
     * @param action The SOAP action.
     * @param identifier The identifier associated with the endpoint.
     * @return true if the message should be dropped, false otherwise.
     */
    public boolean handleAction(final String action, final String identifier)
    {
        if (AtomicTransactionConstants.WSAT_ACTION_PREPARED.equals(action))
        {
            preparedCount++ ;
            if (preparedCount == 1)
            {
                firstIdentifier = identifier ;
            }
            else if (preparedCount == 2)
            {
                drop = true ;
            }
            else if (firstIdentifier.equals(identifier))
            {
                drop = false ;
            }
        }
        else if (AtomicTransactionConstants.WSAT_ACTION_COMMITTED.equals(action))
        {
            if (++committedCount == 2)
            {
                success() ;
            }
        }
        return drop ;
    }
}
