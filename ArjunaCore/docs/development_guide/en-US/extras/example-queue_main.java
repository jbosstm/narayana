public static void main (String[] args)
{
    TransactionalQueue myQueue = new TransactionalQueue();
    Before invoking one of the queue’s operations, the client starts a transaction. The queueSize operation is shown below:
    AtomicAction A = new AtomicAction();
    int size = 0;
 
    try
        {
            A.begin(0);

            try
                {
                    size = queue.queueSize();
                }
            catch (Exception e)
                {
                }

            if (size >= 0)
                {
                    A.commit(true);

                    System.out.println(“Size of queue: “+size);
                }
            else
                A.rollback();
        }
    catch (Exception e)
        {
            System.err.println(“Caught unexpected exception!”);
        }
}