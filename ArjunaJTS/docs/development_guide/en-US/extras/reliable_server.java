/* 
  BankAccount1 is an object with internal resources. It inherits from both the TransactionalObject and the Resource interfaces:
*/
interface BankAccount1:
                    CosTransactions::TransactionalObject, CosTransactions::Resource
{
    ...
    void makeDeposit (in float amt);
    ...
};
/* The corresponding Java class is: */
public class BankAccount1
{
public void makeDeposit(float amt);
    ...
};
/*
  Upon entering, the context of the transaction is implicitly associated with the object’s thread. The pseudo object
  supporting the Current interface is used to retrieve the Coordinator object associated with the transaction.
*/
void makeDeposit (float amt)
{
    org.omg.CosTransactions.Control c;
    org.omg.CosTransactions.Coordinator co;
    c = txn_crt.get_control();
    co = c.get_coordinator();
    ...
/*
  Before registering the resource the object should check whether it has already been registered for the same
  transaction. This is done using the hash_transaction and is_same_transaction operations.  that this object registers
  itself as a resource. This imposes the restriction that the object may only be involved in one transaction at a
  time. This is not the recommended way for recoverable objects to participate within transactions, and is only used as an
  example.  If more parallelism is required, separate resource objects should be registered for involvement in the same
  transaction.
*/
    RecoveryCoordinator r;
    r = co.register_resource(this);

    // performs some transactional activity locally
    balance = balance + f;
    num_transactions++;
    ...
    // end of transactional operation
};