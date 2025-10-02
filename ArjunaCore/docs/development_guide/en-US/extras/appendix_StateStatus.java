
/*
 * This is the base class from which all object store types are derived.
 * Note that because object store instances are stateless, to improve
 * efficiency we try to only create one instance of each type per process.
 * Therefore, the create and destroy methods are used instead of new
 * and delete. If an object store is accessed via create it *must* be
 * deleted using destroy. Of course it is still possible to make use of
 * new and delete directly and to create instances on the stack.
 */

public class StateStatus
{
    public static final int OS_ORIGINAL;
    public static final int OS_SHADOW;
    public static final int OS_UNCOMMITTED;
    public static final int OS_UNCOMMITTED_HIDDEN;
    public static final int OS_UNKNOWN;
}

public class StateType
{
    public static final int OS_COMMITTED;
    public static final int OS_COMMITTED_HIDDEN;
    public static final int OS_HIDDEN;
    public static final int OS_INVISIBLE;
}

public abstract class ObjectStore implements BaseStore, ParticipantStore,
                                             RecoveryStore, TxLog
{
    public ObjectStore (String osRoot);
    public synchronized boolean allObjUids (String s, InputObjectState buff)
        throws ObjectStoreException;
    public synchronized boolean allObjUids (String s, InputObjectState buff,
                                            int m) throws ObjectStoreException;

    public synchronized boolean allTypes (InputObjectState buff)
        throws ObjectStoreException;
    public synchronized int currentState(Uid u, String tn)
        throws ObjectStoreException;
    public synchronized boolean commit_state (Uid u, String tn)
        throws ObjectStoreException;
    public synchronized boolean hide_state (Uid u, String tn)
        throws ObjectStoreException;
    public synchronized boolean reveal_state (Uid u, String tn)
        throws ObjectStoreException;
    public synchronized InputObjectState read_committed (Uid u, String tn)
        throws ObjectStoreException;
    public synchronized InputObjectState read_uncommitted (Uid u, String tn)
        throws ObjectStoreException;
    public synchronized boolean remove_committed (Uid u, String tn)
        throws ObjectStoreException;
    public synchronized boolean remove_uncommitted (Uid u, String tn)
        throws ObjectStoreException;
    public synchronized boolean write_committed (Uid u, String tn,
                                                 OutputObjectState buff)
        throws ObjectStoreException;
    public synchronized boolean write_uncommitted (Uid u, String tn,
                                                   OutputObjectState buff)
        throws ObjectStoreException;
    public static void printState (PrintStream strm, int res);
};