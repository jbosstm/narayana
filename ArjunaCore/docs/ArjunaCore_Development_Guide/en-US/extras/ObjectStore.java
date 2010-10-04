public abstract class ObjectStore 
{
    /* The abstract interface */
    public abstract boolean commit_state (Uid u, String name)
        throws ObjectStoreException;
    public abstract InputObjectState read_committed (Uid u, String name)
        throws ObjectStoreException;
    public abstract boolean write_uncommitted (Uid u, String name,
                                               OutputObjectState os) throws ObjectStoreException;
    . . .
};