public class ObjectStatus
{
    public static final int PASSIVE;
    public static final int PASSIVE_NEW;
    public static final int ACTIVE;
    public static final int ACTIVE_NEW;
};

public class ObjectType
{
    public static final int RECOVERABLE;
    public static final int ANDPERSISTENT;
    public static final int NEITHER;
};
 
public abstract class StateManager
{
    public boolean restore_state (InputObjectState os, int ot);
    public boolean save_state (OutputObjectState os, int ot);
    public String type ();

    public synchronized boolean activate ();
    public synchronized boolean activate (String rootName);
    public synchronized boolean deactivate ();
    public synchronized boolean deactivate (String rootName);
    public synchronized boolean deactivate (String rootName, boolean commit);

    public synchronized int status ();
    public final Uid get_uid ();
    public void destroy ();
    public void print (PrintStream strm);

    protected void terminate ();

    protected StateManager ();
    protected StateManager (int ot);
    protected StateManager (int ot, int objectModel);
    protected StateManager (Uid objUid);
    protected StateManager (Uid objUid, int ot);
    protected StateManager (Uid objUid, int ot, int objectModel);
    protected synchronized final void modified ();
};