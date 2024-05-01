public abstract class StateManager {
    protected StateManager();

    protected StateManager(Uid id);

    public boolean activate();

    // methods to be provided by a derived class

    public boolean deactivate(boolean commit);

    public Uid get_uid(); // object’s identifier.

    public boolean restore_state(InputObjectState os);

    public boolean save_state(OutputObjectState os);
};
