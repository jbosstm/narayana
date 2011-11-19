package org.jboss.jbossts.txframework.functional;

import com.arjuna.mw.wst11.UserBusinessActivity;
import com.arjuna.mw.wst11.UserBusinessActivityFactory;
import com.arjuna.wst.TransactionRolledBackException;
import junit.framework.Assert;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.txframework.api.annotation.lifecycle.wsba.Close;
import org.jboss.jbossts.txframework.api.annotation.lifecycle.wsba.Compensate;
import org.jboss.jbossts.txframework.api.annotation.lifecycle.wsba.ConfirmCompleted;
import org.jboss.jbossts.txframework.functional.clients.BAParticipantCompletionClient;
import org.jboss.jbossts.txframework.functional.common.ServiceCommand;
import org.jboss.jbossts.txframework.functional.interfaces.BAParticipantCompletion;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import javax.xml.ws.soap.SOAPFaultException;
import java.lang.annotation.Annotation;
import java.util.Arrays;

@RunWith(Arquillian.class)
public class BAParticipantCompletionTest extends BaseFunctionalTest
{
    UserBusinessActivity uba;
    BAParticipantCompletion client;

    @Before
    public void setupTest() throws Exception
    {
        uba = UserBusinessActivityFactory.userBusinessActivity();
        client = BAParticipantCompletionClient.newInstance();
    }

    @After
    public void teardownTest() throws Exception
    {
        client.clearEventLog();
    }

    @Test
    public void testAutoComplete() throws Exception
    {
        uba.begin();
        client.saveDataAutoComplete();
        uba.close();

        assertOrder(ConfirmCompleted.class, Close.class);

    }

    @Test
    public void testManualComplete() throws Exception
    {
        uba.begin();
        client.saveDataManualComplete(ServiceCommand.COMPLETE);
        uba.close();

        assertOrder(ConfirmCompleted.class, Close.class);
    }

    //todo: support multi invocation
    /*@Test
    public void testManualCompleteMultiInvoke() throws Exception
    {
        UserBusinessActivity uba = UserBusinessActivityFactory.userBusinessActivity();
        BAParticipantCompletion client = BAParticipantCompletionClient.newInstance();

        Assert.assertTrue(!client.contains());
        Assert.assertTrue(!client.contains("b"));

        uba.begin();
        client.saveDataManualComplete();
        client.saveDataManualComplete("b", ServiceCommand.COMPLETE);
        uba.close();

        Assert.assertTrue(client.contains());
        Assert.assertTrue(client.contains("b"));

        client.clearData();
        Assert.assertTrue(!client.contains());
        Assert.assertTrue(!client.contains("b"));
    }*/

    @Test
    public void testClientDrivenCompensate() throws Exception
    {
        uba.begin();
        client.saveDataAutoComplete();
        uba.cancel();

        assertOrder(ConfirmCompleted.class, Compensate.class);
    }

    @Test
    public void testApplicationException() throws Exception
    {
        try
        {
            uba.begin();
            client.saveDataAutoComplete(ServiceCommand.THROW_APPLICATION_EXCEPTION);
            Assert.fail("Exception should have been thrown by now");
        }
        catch (SOAPFaultException e)
        {
            //todo: can we pass application exceptions over SOAP when using an EJB exposed as a JAX-WS ws?
            System.out.println("Caught exception");
        }
        finally
        {
            uba.cancel();
        }
        assertOrder();
    }

    @Test(expected = TransactionRolledBackException.class)
    public void testCannotComplete() throws Exception
    {
        uba.begin();
        client.saveDataAutoComplete(ServiceCommand.CANNOT_COMPLETE);
        uba.close();
        assertOrder();
    }


    private void assertOrder(Class<? extends Annotation>... expectedOrder)
    {
        org.junit.Assert.assertEquals(Arrays.asList(expectedOrder), client.getEventLog().getLog());
    }
}
