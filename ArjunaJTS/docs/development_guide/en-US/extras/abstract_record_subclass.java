public class SimpleRecord extends AbstractRecord
{
    private int _value = 0;

    .....
   
        public void increase()
    {
        _value++;
    }

    public int get()
    {
        return _value;
    }

    public String type()
    {
        return “/StateManager/AbstractRecord/SimpleRecord”;
    }

    public boolean restore_state(InputObjectState os, int i)
    {
        boolean returnValue = true;
   
        try
            {
                _value = os.unpackInt();
            }
        catch (java.io.IOException e)
            {
                returnValue = false;
            }

        return returnValue;
    }

    public boolean save_state(OutputObjectState os, int i)
    {
        boolean returnValue = true;

        try
            {
                os.packInt(_value);
            }
        catch (java.io.IOException e)
            {
                returnValue = false;
            }

        return returnValue;
    }
}