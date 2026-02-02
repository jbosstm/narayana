interface Current : CORBA::Current
{
    void begin () raises (SubtransactionsUnavailable);
    void commit (in boolean report_heuristics) raises (NoTransaction,
                                                       HeuristicMixed,
                                                       HeuristicHazard); 
    void rollback () raises (NoTransaction);
    void rollback_only () raises (NoTransaction);

    . . .

    Control get_control ();   
    Control suspend ();
    void resume (in Control which) raises (InvalidControl);
};