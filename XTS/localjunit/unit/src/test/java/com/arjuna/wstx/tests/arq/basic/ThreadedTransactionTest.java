package com.arjuna.wstx.tests.arq.basic;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.mw.wst11.UserTransaction;
import com.arjuna.wstx.tests.arq.WarDeployment;

class ThreadedObject extends Thread
{
    Exception exception;

    public ThreadedObject ()
    {
        exception = null;
    }

    public void run ()
    {
        try
        {
            UserTransaction ut = UserTransaction.getUserTransaction();

            ut.begin();

            System.out.println("Thread "+Thread.currentThread()+" started "+ut);

            Thread.yield();

            System.out.println("\nThread "+Thread.currentThread()+" committing "+ut);

            ut.commit();

            Thread.yield();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            exception = ex;
        }
    }
}

@Ignore // @TODO JBTM-2193
@RunWith(Arquillian.class)
public class ThreadedTransactionTest {

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment(
                ThreadedObject.class);
    }

    @Test
    public void testThreadedTransaction()
            throws Exception
            {
        int size = 10;
        ThreadedObject objs[] = new ThreadedObject[size];

        for (int i = 0; i < size; i++)
            objs[i] = new ThreadedObject();

        for (int j = 0; j < size; j++)
            objs[j].start();

        for (int k = 0; k < size; k++)
            objs[k].join();

        for (int k = 0; k < size; k++) {
            if (objs[k].exception != null) {
                throw objs[k].exception;
            }
        }
            }
}
