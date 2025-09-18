The constructor
persistent o
that createsbject
public Tra
a new
is similarnsactionalQueue(Uid u) {
    super(u);

    numberOfElements = 0;
}:

public TransactionalQueue() {
    super(ObjectType.ANDPERSISTENT);

    numberOfElements = 0;

    try {
        AtomicAction A = new AtomicAction();

        // Try to start atomic action
        A.begin(0);

        // Try to set lock

        if (setlock(new Lock(LockMode.WRITE), 0) == LockResult.GRANTED) {
            // Commit
            A.commit(true);
        } else
            // Lock refused so abort the atomic action
            A.rollback();
    } catch (Exception e) {
        System.err.println("Object construction error: "+e);
        System.exit(1);
    }
}
