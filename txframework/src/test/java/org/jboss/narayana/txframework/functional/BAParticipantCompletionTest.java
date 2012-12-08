package org.jboss.narayana.txframework.functional;

import com.arjuna.mw.wst11.UserBusinessActivity;
import com.arjuna.mw.wst11.UserBusinessActivityFactory;
import com.arjuna.wst.TransactionRolledBackException;
import junit.framework.Assert;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.xts.bytemanSupport.BMScript;
import org.jboss.jbossts.xts.bytemanSupport.participantCompletion.ParticipantCompletionCoordinatorRules;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Close;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Compensate;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.ConfirmCompleted;
import org.jboss.narayana.txframework.functional.clients.BAParticipantCompletionClient;
import org.jboss.narayana.txframework.functional.common.ServiceCommand;
import org.jboss.narayana.txframework.functional.common.SomeApplicationException;
import org.jboss.narayana.txframework.functional.interfaces.BAParticipantCompletion;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

@RunWith(Arquillian.class)
public class BAParticipantCompletionTest extends BaseFunctionalTest
{
    UserBusinessActivity uba;
    BAParticipantCompletion client;


    @BeforeClass()
    public static void submitBytemanScript() throws Exception {
        BMScript.submit(ParticipantCompletionCoordinatorRules.RESOURCE_PATH);
    }

    @AfterClass()
    public static void removeBytemanScript() {
        BMScript.remove(ParticipantCompletionCoordinatorRules.RESOURCE_PATH);
    }

    @Before
    public void setupTest() throws Exception
    {
        uba = UserBusinessActivityFactory.userBusinessActivity();
        client = BAParticipantCompletionClient.newInstance();
    }

    @After
    public void teardownTest() throws Exception
    {
        assertDataAvailable();
        client.clearEventLog();
        cancelIfActive(uba);
    }

    @Test
    public void testAutoComplete() throws Exception
    {
        ParticipantCompletionCoordinatorRules.setParticipantCount(1);
        
        uba.begin();
        client.saveDataAutoComplete();
        uba.close();

        assertOrder(ConfirmCompleted.class, Close.class);

    }

    @Test
    public void testManualComplete() throws Exception
    {
        ParticipantCompletionCoordinatorRules.setParticipantCount(1);

        uba.begin();
        client.saveDataManualComplete(ServiceCommand.COMPLETE);
        uba.close();

        assertOrder(ConfirmCompleted.class, Close.class);
    }

    @Test
    public void testMultiInvoke() throws Exception
    {
        ParticipantCompletionCoordinatorRules.setParticipantCount(1);
        
        uba.begin();
        client.saveDataManualComplete();
        client.saveDataManualComplete(ServiceCommand.COMPLETE);
        uba.close();

        assertOrder(ConfirmCompleted.class, Close.class);
    }

    @Test
    public void testClientDrivenCompensate() throws Exception
    {
        ParticipantCompletionCoordinatorRules.setParticipantCount(1);

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
        catch (SomeApplicationException e)
        {
            //Exception expected
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
