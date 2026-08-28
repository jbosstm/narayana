public class ExplicitInterposition
{
    public ExplicitInterposition ();

    public void registerTransaction (Control control) throws InterpositionFailed, SystemException;

    public void unregisterTransaction () throws InvalidTransaction,
                                                SystemException;
};