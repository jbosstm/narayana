public class ObjectStatus
{
    public static final int PASSIVE;
    public static final int PASSIVE_NEW;
    public static final int ACTIVE;
    public static final int ACTIVE_NEW;
    public static final int UNKNOWN_STATUS;
};

public class ObjectType
{
    public static final int RECOVERABLE;
    public static final int ANDPERSISTENT;
    public static final int NEITHER;
};

public abstract class StateManager
{
    public synchronized boolean activate ();
    public synchronized boolean activate (String storeRoot);
    public synchronized boolean deactivate ();
    public synchronized boolean deactivate (String storeRoot, boolean commit);

    public synchronized void destroy ();
 
    public final Uid get_uid ();

    public boolean restore_state (InputObjectState, int ObjectType);
    public boolean save_state (OutputObjectState, int ObjectType);
    public String type ();
    . . .

        protected StateManager ();
    protected StateManager (int ObjectType, int objectModel);
    protected StateManager (Uid uid);
    protected StateManager (Uid uid, int objectModel);
    . . .

        protected final void modified ();
    . . .
};

public class ObjectModel
{
    public static final int SINGLE;
    public static final int MULTIPLE;
};