package org.jboss.narayana.txframework.functional;

import com.arjuna.mw.wst11.UserTransaction;
import com.arjuna.mw.wst11.UserTransactionFactory;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.narayana.txframework.functional.clients.ATBridgeClient;
import org.jboss.narayana.txframework.functional.interfaces.ATBridge;
import org.junit.*;
import org.junit.runner.RunWith;


@RunWith(Arquillian.class)
public class ATBridgeTest extends BaseFunctionalTest {
    private UserTransaction ut;
    private ATBridge client;

    @Before
    public void setupTest() throws Exception {
        ut = UserTransactionFactory.userTransaction();
        client = ATBridgeClient.newInstance();
    }

    @After
    public void teardownTest() throws Exception {

        try {
            ut.begin();
            client.reset();
            ut.commit();
        } finally {
            rollbackIfActive(ut);
        }

    }

    @Test
    public void testSimple() throws Exception {
        try {

            ut.begin();
            client.incrementCounter(1);
            ut.commit();

            ut.begin();
            int counter = client.getCounter();
            ut.commit();

            Assert.assertEquals(1, counter);
        } finally {
            rollbackIfActive(ut);
        }
    }

    @Test
    public void testClientDrivenRollback() throws Exception {
        try {
            ut.begin();
            client.incrementCounter(1);
            ut.rollback();

            ut.begin();
            int counter = client.getCounter();
            ut.commit();

            Assert.assertEquals(0, counter);
        } finally {
            rollbackIfActive(ut);
        }
    }

    private void rollbackIfActive(UserTransaction ut) {
        try {
            ut.rollback();
        } catch (Throwable th2) {
            // do nothing, not active
        }
    }

}

