public class LockStore
{
    public abstract InputObjectState read_state (Uid u, String tName)
        throws LockStoreException;

    public abstract boolean remove_state (Uid u, String tname);
    public abstract boolean write_committed (Uid u, String tName,
                                             OutputObjectState state);
};