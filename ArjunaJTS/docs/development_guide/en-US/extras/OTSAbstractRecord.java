interface OTSAbstractRecord : ArjunaSubtranAwareResource
{
    readonly attribute long typeId;
    readonly attribute string uid;

    boolean propagateOnAbort ();
    boolean propagateOnCommit ();

    boolean saveRecord ();
    void merge (in OTSAbstractRecord record);
    void alter (in OTSAbstractRecord record);

    boolean shouldAdd (in OTSAbstractRecord record);
    boolean shouldAlter (in OTSAbstractRecord record);
    boolean shouldMerge (in OTSAbstractRecord record);
    boolean shouldReplace (in OTSAbstractRecord record);
};