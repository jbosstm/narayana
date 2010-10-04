public int inspectValue (int index) throws UnderFlow,
                                           OverFlow, Conflict, QueueError
{
    AtomicAction A = new AtomicAction();
    boolean res = false;
    int val = -1;

    try
        {
            A.begin();

            if (setlock(new Lock(LockMode.READ), 0) == LockResult.GRANTED)
                {
                    if (index < 0)
                        {
                            A.rollback();
                            throw new UnderFlow();
                        }
                    else
                        {
                            // array is 0 - numberOfElements -1

                            if (index > numberOfElements -1)
                                {
                                    A.rollback();
                                    throw new OverFlow();
                                }
                            else
                                {
                                    val = elements[index];
                                    res = true;
                                }
                        }
                }

            if (res)
                A.commit(true);
            else
                {
                    A.rollback();
                    throw new Conflict();
                }
        }
    catch (Exception e1)
        {
            throw new QueueError();
        }

    return val;
}