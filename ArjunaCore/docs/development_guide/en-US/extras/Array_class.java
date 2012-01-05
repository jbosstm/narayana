public class Array extends StateManager
{
    public Array ();
    public Array (Uid objUid);
    public void finalize ( super.terminate(); };

    /* Class specific operations. */

    public boolean set (int index, int value);
    public int get (int index);

    /* State management specific operations. */

    public boolean save_state (OutputObjectState os, int ObjectType);
    public boolean restore_state (InputObjectState os, int ObjectType);
    public String type ();

    public static final int ARRAY_SIZE = 10;

    private int[] elements = new int[ARRAY_SIZE];
    private int highestIndex;
};
The save_state, restore_state and type operations can be defined as follows:
    /* Ignore ObjectType parameter for simplicity */

    public boolean save_state (OutputObjectState os, int ObjectType)
    {
        if (!super.save_state(os, ObjectType))
            return false;

        try
            {
                packInt(highestIndex);

                /*
                 * Traverse array state that we wish to save. Only save active elements
                 */

                for (int i = 0; i <= highestIndex; i++)
                    os.packInt(elements[i]);

                return true;
            }
        catch (IOException e)
            {
                return false;
            }
    }
public boolean restore_state (InputObjectState os, int ObjectType)
{
    if (!super.restore_state(os, ObjectType))
        return false;

    try
        {
            int i = 0;

            highestIndex = os.unpackInt();

            while (i < ARRAY_SIZE)
                {
                    if (i <= highestIndex)
                        elements[i] =  os.unpackInt();
                    else
                        elements[i] = 0;
                    i++;
                }

            return true;
        }
    catch (IOException e)
        {
            return false;
        }
}
public String type ()
{
    return "/StateManager/Array";
}
