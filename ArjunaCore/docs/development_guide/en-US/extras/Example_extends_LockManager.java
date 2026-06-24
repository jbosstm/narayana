public class Example extends LockManager
{
    public boolean foobar ()
    {
        AtomicAction A = new AtomicAction;
        boolean result = false;

        A.begin();

        if (setlock(new Lock(LockMode.WRITE), 0) == Lock.GRANTED)
            {
                /*
                 * Do some work, and TXOJ will
                 * guarantee ACID properties.
                 */

                // automatically aborts if fails

                if (A.commit() == AtomicAction.COMMITTED)
                    {
                        result = true;
                    }
            }
        else
            A.rollback();

        return result;
    }
}
