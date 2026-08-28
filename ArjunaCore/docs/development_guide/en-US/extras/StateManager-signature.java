public abstract class StateManager
{
    public boolean activate ();
    public boolean deactivate (boolean commit);

    public Uid get_uid (); // object’s identifier.

    // methods to be provided by a derived class

    public boolean restore_state (InputObjectState os);
    public boolean save_state (OutputObjectState os);

    protected StateManager ();
    protected StateManager (Uid id);
};