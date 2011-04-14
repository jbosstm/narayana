interface ArjunaSubtranAwareResource : 
    CosTransactions::SubtransactionAwareResource
{
    CosTransactions::Vote prepare_subtransaction ();
};