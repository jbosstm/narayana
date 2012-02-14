package org.jboss.narayana.txframework.functional;

import com.arjuna.mw.wst11.UserBusinessActivity;
import com.arjuna.mw.wst11.UserBusinessActivityFactory;
import com.arjuna.wst.TransactionRolledBackException;
import junit.framework.Assert;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.narayana.txframework.api.annotation.lifecycle.wsba.Cancel;
import org.jboss.narayana.txframework.api.annotation.lifecycle.wsba.Close;
import org.jboss.narayana.txframework.functional.clients.BACoordinatorCompletionClient;
import org.jboss.narayana.txframework.functional.interfaces.BACoordinatorCompletion;
import org.jboss.narayana.txframework.api.annotation.lifecycle.wsba.Complete;
import org.jboss.narayana.txframework.api.annotation.lifecycle.wsba.ConfirmCompleted;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import javax.xml.ws.soap.SOAPFaultException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

import static org.jboss.narayana.txframework.functional.common.ServiceCommand.*;

@RunWith(Arquillian.class)
public class BACoordinatorCompletionTest extends BaseFunctionalTest
{
    UserBusinessActivity uba;
    BACoordinatorCompletion client;

    @Before
    public void setupTest() throws Exception
    {
        uba = UserBusinessActivityFactory.userBusinessActivity();
        client = BACoordinatorCompletionClient.newInstance();
    }

    @After
    public void teardownTest() throws Exception
    {
        assertDataAvailable();
        client.clearEventLog();
        cancelIfActive(uba);
    }

    @Test
    public void testSimple() throws Exception
    {
        uba.begin();
        client.saveData();
        uba.close();

        assertOrder(Complete.class, ConfirmCompleted.class, Close.class);
    }

    @Test
    public void testMultiInvoke() throws Exception
    {
        uba.begin();
        client.saveData();
        client.saveData();
        uba.close();

        assertOrder(Complete.class, ConfirmCompleted.class, Close.class);
    }

    @Test
    public void testClientDrivenCancel() throws Exception
    {
        uba.begin();
        client.saveData();
        uba.cancel();

        assertOrder(Cancel.class);
    }

    @Test
    public void testApplicationException() throws Exception
    {
        try
        {
            uba.begin();
            client.saveData(THROW_APPLICATION_EXCEPTION);
            Assert.fail("Exception should have been thrown by now");
        }
        catch (SOAPFaultException e)
        {
            //todo: can we pass application exceptions over SOAP when using an EJB exposed as a JAX-WS ws?
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
        client.saveData(CANNOT_COMPLETE);
        uba.close();

        assertOrder();
    }

    private void assertOrder(Class<? extends Annotation>... expectedOrder)
    {
        org.junit.Assert.assertEquals(Arrays.asList(expectedOrder), client.getEventLog().getEventLog());
    }

    private void assertDataAvailable()
    {
        List<Class<? extends Annotation>> log = client.getEventLog().getDataUnavailableLog();
        if (!log.isEmpty())
        {
            org.junit.Assert.fail("One or more lifecycle methods could not access the managed data: " + log.toString());
        }
    }
}
