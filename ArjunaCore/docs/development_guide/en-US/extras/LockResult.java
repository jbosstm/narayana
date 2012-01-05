public class LockResult
{
    public static final int GRANTED;
    public static final int REFUSED;
    public static final int RELEASED;
};

public class ConflictType
{
    public static final int CONFLICT;
    public static final int COMPATIBLE;
    public static final int PRESENT;
};

public abstract class LockManager extends StateManager
{
    public static final int defaultTimeout;
    public static final int defaultRetry;
    public static final int waitTotalTimeout;

    public synchronized int setlock (Lock l);
    public synchronized int setlock (Lock l, int retry);
    public synchronized int setlock (Lock l, int retry, int sleepTime);
    public synchronized boolean releaselock (Uid uid);
 
    /* abstract methods inherited from StateManager */

    public boolean restore_state (InputObjectState os, int ObjectType);
    public boolean save_state (OutputObjectState os, int ObjectType);
    public String type ();

    protected LockManager ();
    protected LockManager (int ObjectType, int objectModel);
    protected LockManager (Uid storeUid);
    protected LockManager (Uid storeUid, int ObjectType, int objectModel);
    . . .
};