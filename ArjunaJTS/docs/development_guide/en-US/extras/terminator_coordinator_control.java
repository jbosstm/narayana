interface Terminator
{
    void commit (in boolean report_heuristics) raises (HeuristicMixed, HeuristicHazard); 
    void rollback ();
};

interface Coordinator
{
    Status get_status ();
    Status get_parent_status ();
    Status get_top_level_status ();

    RecoveryCoordinator register_resource (in Resource r) raises (Inactive);
    Control create_subtransaction () raises (SubtransactionsUnavailable,
                                             Inactive);

    void rollback_only () raises (Inactive);

    ...
};

interface Control
{
    Terminator get_terminator () raises (Unavailable);
    Coordinator get_coordinator () raises (Unavailable);
};

interface TransactionFactory
{
    Control create (in unsigned long time_out);
};
