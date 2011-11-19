package org.jboss.jbossts.txframework.functional;

import com.arjuna.mw.wst11.UserTransaction;
import com.arjuna.mw.wst11.UserTransactionFactory;
import com.arjuna.wst.TransactionRolledBackException;
import static org.jboss.jbossts.txframework.functional.common.ServiceCommand.*;
import org.junit.After;
import org.junit.Assert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.txframework.api.annotation.lifecycle.wsat.*;
import org.jboss.jbossts.txframework.functional.clients.ATClient;
import org.jboss.jbossts.txframework.functional.interfaces.AT;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import javax.xml.ws.soap.SOAPFaultException;
import java.lang.annotation.Annotation;
import java.util.Arrays;

@RunWith(Arquillian.class)
public class ATTest extends BaseFunctionalTest
{
    private UserTransaction ut;
    private AT client;

    @Before
    public void setupTest() throws Exception
    {
        ut = UserTransactionFactory.userTransaction();
        client = ATClient.newInstance();
    }

    @After
    public void teardownTest() throws Exception
    {
        client.clearEventLog();
    }

    @Test
    public void testSimple() throws Exception
    {
        ut.begin();
        client.invoke();
        ut.commit();

        assertOrder(Prepare.class, Commit.class);
    }

    @Test
    public void testClientDrivenRollback() throws Exception
    {
        ut.begin();
        client.invoke();
        ut.rollback();

        //todo: should rollback be called twice? once for volatile and once for durable
        assertOrder(Rollback.class, Rollback.class);

    }

    @Test(expected = TransactionRolledBackException.class)
    public void testParticipantDrivenRollback() throws Exception
    {
        try
        {
            ut.begin();
            client.invoke(VOTE_ROLLBACK);
            ut.commit();
        }
        catch (TransactionRolledBackException e)
        {
            //todo: should rollback be called twice? once for volatile and once for durable
            assertOrder(Prepare.class, Rollback.class);
            throw e;
        }
    }

    @Test
    public void testApplicationException() throws Exception
    {
        try
        {
            ut.begin();
            client.invoke(THROW_APPLICATION_EXCEPTION);
            Assert.fail("Exception should have been thrown by now");
        }
        catch (SOAPFaultException e)
        {
            //todo: can we pass application exceptions over SOAP when using an EJB exposed as a JAX-WS ws?
            System.out.println("Caught exception");
        }
        finally
        {
            ut.rollback();
        }
        //todo: should this cause Rollback?
        assertOrder(Rollback.class, Rollback.class);
    }

    private void assertOrder(Class<? extends Annotation>... expectedOrder)
    {
        Assert.assertEquals(Arrays.asList(expectedOrder), client.getEventLog().getLog());
    }
}

//todo: support multi invocation
/*@Test
public void testManualCompleteMultiInvoke() throws Exception
{
    UserBusinessActivity uba = UserBusinessActivityFactory.userBusinessActivity();
    BAParticipantCompletion client = BAParticipantCompletionClient.newInstance();

    Assert.assertTrue(!client.contains("a"));
    Assert.assertTrue(!client.contains("b"));

    uba.begin();
    client.saveDataManualComplete("a");
    client.saveDataManualComplete("b", ServiceCommand.COMPLETE);
    uba.close();

    Assert.assertTrue(client.contains("a"));
    Assert.assertTrue(client.contains("b"));

    client.clearData();
    Assert.assertTrue(!client.contains("a"));
    Assert.assertTrue(!client.contains("b"));
}*/
