class InputObjectState extends InputBuffer
{
    public OutputObjectState (Uid newUid, String typeName, byte[] b);

    public boolean notempty ();
    public int size ();
    public Uid stateUid ();
    public String type ();
};