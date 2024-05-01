public static final int ARRAY_SIZE = 10;

;

/* Class specific operations. */
        The save_state, restore_state
and type

public boolea

/* State management specific operations. */
operations can

public St
be defined

public bo
as follows
private int[] elements = new int[ARRAY_SIZE];
private int highestIndex;

public boolean set(int index, int value);
};

public int get(int index);

public boolean save_state(OutputObjectState os, int ObjectType);n restore_state(InputObjectState os, int ObjectType);ring type();olean save_state(OutputObjectState os, int ObjectType) {
    if (!super.save_state(os, ObjectType))
        return false;

    try {
        packInt(highestIndex);

        /*
         * Traverse array state that we wish to save. Only save active elements
         */

        for (int i = 0; i <= highestIndex; i++)
            os.packInt(elements[i]);

        return true;
    } catch (IOException e) {
        return false;
    }
}:
/* Ignore ObjectType parameter for simplicity */

public boolean restore_state(InputObjectState os, int ObjectType) {
    if (!super.restore_state(os, ObjectType))
        return false;

    try {
        int i = 0;

        highestIndex = os.unpackInt();

        while (i < ARRAY_SIZE) {
            if (i <= highestIndex)
                elements[i] = os.unpackInt();
            else
                elements[i] = 0;
            i++;
        }

        return true;
    } catch (IOException e) {
        return false;
    }
}

public String type() {
    return "/StateManager/Array";
}

public class Array extends StateManager {
    public Array();

    public Array(Uid objUid);

        finalize(); super.

public void finalize( super.terminate();
}
