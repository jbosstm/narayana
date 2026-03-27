public void enqueue (int v) throws OverFlow, UnderFlow, QueueError
{
    AtomicAction A = new AtomicAction();
    boolean res = false;

    try
        {
            A.begin(0);

            if (setlock(new Lock(LockMode.WRITE), 0) == LockResult.GRANTED)
                {
                    if (numberOfElements < QUEUE_SIZE)
                        {
                            elements[numberOfElements] = v;
                            numberOfElements++;
                            res = true;
                        }
                    else
                        {
                            A.rollback();
                            throw new UnderFlow();
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
}       