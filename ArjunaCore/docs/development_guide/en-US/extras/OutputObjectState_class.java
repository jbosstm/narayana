class OutputObjectState extends OutputBuffer
{
    public OutputObjectState (Uid newUid, String typeName);

    public boolean notempty ();
    public int size ();
    public Uid stateUid ();
    public String type ();
};
class InputObjectState extends ObjectState
{
    public OutputObjectState (Uid newUid, String typeName, byte[] b);

    public boolean notempty ();
    public int size ();
    public Uid stateUid ();
    public String type ();
};