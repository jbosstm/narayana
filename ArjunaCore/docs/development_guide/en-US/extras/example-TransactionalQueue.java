public TransactionalQueue (Uid u)
{
    super(u);

    numberOfElements = 0;
}
The constructor that creates a new persistent object is similar:
    public TransactionalQueue ()
{
    super(ObjectType.ANDPERSISTENT);

    numberOfElements = 0;

    try
        {
            AtomicAction A = new AtomicAction();

            A.begin(0); // Try to start atomic action

            // Try to set lock

            if (setlock(new Lock(LockMode.WRITE), 0) == LockResult.GRANTED)
                {
                    A.commit(true); // Commit
                }
            else           // Lock refused so abort the atomic action
                A.rollback();
        }
    catch (Exception e)
        {
            System.err.println(“Object construction error: “+e);
            System.exit(1);
        }
}