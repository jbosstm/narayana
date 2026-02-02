public int queueSize () throws QueueError, Conflict
{
    AtomicAction A = new AtomicAction();
    int size = -1;

    try
        {
            A.begin(0);

            if (setlock(new Lock(LockMode.READ), 0) == LockResult.GRANTED)
                size = numberOfElements;
    
            if (size != -1)
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

    return size;
}       