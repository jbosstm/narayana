public interface XAResourceRecovery
{
    public XAResource getXAResource () throws SQLException;

    public boolean initialise (String p);

    public boolean hasMoreResources ();
};