public class AtomicTransaction
{
    public AtomicTransaction ();
    public void begin () throws SystemException, SubtransactionsUnavailable,
                                NoTransaction;
    public void commit (boolean report_heuristics) throws SystemException, 
                                                          NoTransaction, HeuristicMixed,
                                                          HeuristicHazard,TransactionRolledBack;
    public void rollback () throws SystemException, NoTransaction;
    public Control control () throws SystemException, NoTransaction;
    public Status get_status () throws SystemException;
    /* Allow action commit to be supressed */    
    public void rollbackOnly () throws SystemException, NoTransaction;

    public void registerResource (Resource r) throws SystemException, Inactive;
    public void
        registerSubtransactionAwareResource (SubtransactionAwareResource)
        throws SystemException, NotSubtransaction;
    public void
        registerSynchronization(Synchronization s) throws SystemException,
                                                          Inactive;
};