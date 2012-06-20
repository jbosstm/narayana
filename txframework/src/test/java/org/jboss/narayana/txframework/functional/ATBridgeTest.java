package org.jboss.narayana.txframework.functional;

import com.arjuna.mw.wst11.UserTransaction;
import com.arjuna.mw.wst11.UserTransactionFactory;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.narayana.txframework.functional.clients.ATBridgeClient;
import org.jboss.narayana.txframework.functional.interfaces.ATBridge;
import org.junit.*;
import org.junit.runner.RunWith;


@RunWith(Arquillian.class)
public class ATBridgeTest extends BaseFunctionalTest
{
    private UserTransaction ut;
    private ATBridge client;

    @Before
    public void setupTest() throws Exception
    {
        ut = UserTransactionFactory.userTransaction();
        client = ATBridgeClient.newInstance();
    }

    @After
    public void teardownTest() throws Exception
    {
        ut.begin();
        client.reset();
        ut.commit();
        rollbackIfActive(ut);
    }

    @Test
    public void testSimple() throws Exception
    {
        ut.begin();
        client.incrementCounter(1);
        ut.commit();

        ut.begin();
        int counter = client.getCounter();
        ut.commit();

        Assert.assertEquals(1, counter);
    }

    @Test
    public void testClientDrivenRollback() throws Exception
    {
        ut.begin();
        client.incrementCounter(1);
        ut.rollback();

        ut.begin();
        int counter = client.getCounter();
        ut.commit();

        Assert.assertEquals(0, counter);
    }
}

