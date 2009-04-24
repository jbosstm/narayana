package com.jboss.transaction.wstf.interop.states;

import com.arjuna.webservices11.wsat.AtomicTransactionConstants;

/**
 * A conversation state for prepare after timeout test.
 */
public class Sc007PreparedAfterTimeoutState extends BaseState
{
    /**
     * The prepared count.
     */
    private int preparedCount ;
    /**
     * The rollback count.
     */
    private int rollbackCount ;
    /**
     * Are we dropping other messages?
     */
    private boolean drop ;
    /**
     * Aborted flag.
     */
    private boolean aborted ;
    /**
     * Soap Fault flag.
     */
    private boolean soapFault ;
    
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
            if (++preparedCount == 2)
            {
                drop = true ;
            }
        }
        else if (AtomicTransactionConstants.WSAT_ACTION_ROLLBACK.equals(action))
        {
            if (++rollbackCount == 2)
            {
                drop = false ;
                return true ;
            }
        }
        else if (AtomicTransactionConstants.WSAT_ACTION_ABORTED.equals(action))
        {
            aborted = true ;
        }
        else if (AtomicTransactionConstants.WSAT_ACTION_FAULT.equals(action))
        {
            soapFault = true ;
        }
        
        if (aborted && soapFault)
        {
            success() ;
        }
        
        return drop ;
    }
}
