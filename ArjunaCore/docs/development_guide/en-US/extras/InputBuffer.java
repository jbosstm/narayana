public class InputBuffer
{
    public InputBuffer ();

    public final synchronized boolean valid ();
    public synchronized byte[] buffer();
    public synchronized int length ();

    /* unpack operations for standard Java types */

    public synchronized byte unpackByte () throws IOException;
    public synchronized byte[] unpackBytes () throws IOException;
    public synchronized boolean unpackBoolean () throws IOException;
    public synchronized char unpackChar () throws IOException;
    public synchronized short unpackShort () throws IOException;
    public synchronized int unpackInt () throws IOException;
    public synchronized long unpackLong () throws IOException;
    public synchronized float unpackFloat () throws IOException;
    public synchronized double unpackDouble () throws IOException;
    public synchronized String unpackString () throws IOException;
};