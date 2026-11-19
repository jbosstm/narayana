/*  A BankAccount2 is an object with external resources that inherits from the TransactionalObject interface: */
interface BankAccount2: CosTransactions::TransactionalObject
{
    ...
    void makeDeposit(in float amt);
    ...
};

public class BankAccount2
{
public void makeDeposit(float amt);
    ...
}
/*
Upon entering, the context of the transaction is implicitly associated with the object’s thread. The makeDeposit
operation performs some transactional requests on external, recoverable servers. The objects res1 and res2 are
recoverable objects. The current transaction context is implicitly propagated to these objects.
*/
void makeDeposit(float amt)
{
    balance = res1.get_balance(amt);
    balance = balance + amt;
    res1.set_balance(balance);
    res2.increment_num_transactions();
} // end of transactional operation