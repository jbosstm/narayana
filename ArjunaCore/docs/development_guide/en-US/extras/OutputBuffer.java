public class OutputBuffer
{
    public OutputBuffer ();

    public final synchronized boolean valid ();
    public synchronized byte[] buffer();
    public synchronized int length ();

    /* pack operations for standard Java types */

    public synchronized void packByte (byte b) throws IOException;
    public synchronized void packBytes (byte[] b) throws IOException;
    public synchronized void packBoolean (boolean b) throws IOException;
    public synchronized void packChar (char c) throws IOException;
    public synchronized void packShort (short s) throws IOException;
    public synchronized void packInt (int i) throws IOException;
    public synchronized void packLong (long l) throws IOException;
    public synchronized void packFloat (float f) throws IOException;
    public synchronized void packDouble (double d) throws IOException;
    public synchronized void packString (String s) throws IOException;
};