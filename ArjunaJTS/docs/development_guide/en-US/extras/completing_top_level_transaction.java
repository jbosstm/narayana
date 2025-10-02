interface Resource
{
    Vote prepare ();
    void rollback () raises (HeuristicCommit, HeuristicMixed,
                             HeuristicHazard);
    void commit () raises (NotPrepared, HeuristicRollback,
                           HeuristicMixed, HeuristicHazard);
    void commit_one_phase () raises (HeuristicRollback, HeuristicMixed,
                                    HeuristicHazard);
    void forget ();
};