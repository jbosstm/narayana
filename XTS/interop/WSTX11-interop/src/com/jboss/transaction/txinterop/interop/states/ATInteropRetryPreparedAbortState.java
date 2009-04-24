package com.jboss.transaction.txinterop.interop.states;

import com.arjuna.webservices11.wsat.AtomicTransactionConstants;

/**
 * A conversation state for retry prepared abort test.
 */
public class ATInteropRetryPreparedAbortState extends BaseState
{
    /**
     * Have we had the first prepare?
     */
    private boolean firstPrepare ;
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
        if (AtomicTransactionConstants.WSAT_ACTION_PREPARE.equals(action))
        {
            if (!firstPrepare)
            {
                firstPrepare = true ;
                drop = true ;
                return false ;
            }
            return true ;
        }
        if (AtomicTransactionConstants.WSAT_ACTION_ROLLBACK.equals(action))
        {
            if (drop)
            {
                drop = false ;
                return true ;
            }
        }
        else if (AtomicTransactionConstants.WSAT_ACTION_ABORTED.equals(action))
        {
            success() ;
        }
        return drop ;
    }
}
