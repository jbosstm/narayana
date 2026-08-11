public class TransactionalQueue extends LockManager
{
    public TransactionalQueue (Uid uid);
    public TransactionalQueue ();
    public void finalize ();

    public void enqueue (int v) throws OverFlow, UnderFlow,
                                       QueueError, Conflict;
    public int dequeue  () throws OverFlow, UnderFlow,
                                  QueueError, Conflict;

    public int queueSize ();
    public int inspectValue (int i) throws OverFlow,
                                           UnderFlow, QueueError, Conflict;
    public void setValue (int i, int v) throws OverFlow,
                                               UnderFlow, QueueError, Conflict;

    public boolean save_state (OutputObjectState os, int ObjectType);
    public boolean restore_state (InputObjectState os, int ObjectType);
    public String type ();

    public static final int QUEUE_SIZE = 40; // maximum size of the queue

    private int[QUEUE_SIZE] elements;
    private int numberOfElements;
};