public class Uid implements Cloneable
{
    public Uid ();
    public Uid (Uid copyFrom);
    public Uid (String uidString);
    public Uid (String uidString, boolean errorsOk);
    public synchronized void pack (OutputBuffer packInto) throws IOException;
    public synchronized void unpack (InputBuffer unpackFrom) throws IOException;

    public void print (PrintStream strm);
    public String toString ();
    public Object clone () throws CloneNotSupportedException;
    public synchronized void copy (Uid toCopy) throws UidException;
    public boolean equals (Uid u);
    public boolean notEquals (Uid u);
    public boolean lessThan (Uid u);
    public boolean greaterThan (Uid u);

    public synchronized final boolean valid ();
    public static synchronized Uid nullUid ();
};