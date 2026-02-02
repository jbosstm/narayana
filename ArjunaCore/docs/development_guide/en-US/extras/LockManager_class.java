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
    public static final int defaultRetry;
    public static final int defaultTimeout;
    public static final int waitTotalTimeout;

    public final synchronized boolean releaselock (Uid lockUid);
    public final synchronized int setlock (Lock toSet);
    public final synchronized int setlock (Lock toSet, int retry);
    public final synchronized int setlock (Lock toSet, int retry, int sleepTime);
    public void print (PrintStream strm);
    public String type ();
    public boolean save_state (OutputObjectState os, int ObjectType);
    public boolean restore_state (InputObjectState os, int ObjectType);

    protected LockManager ();
    protected LockManager (int ot);
    protected LockManager (int ot, int objectModel);
    protected LockManager (Uid storeUid);
    protected LockManager (Uid storeUid, int ot);
    protected LockManager (Uid storeUid, int ot, int objectModel);

    protected void terminate ();
};