package com.arjuna.wstx.tests.arq.ba;

import javax.xml.ws.soap.SOAPFaultException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.mw.wst11.client.WSTXFeature;
import com.arjuna.wstx.tests.arq.WarDeployment;
import com.arjuna.wstx.tests.common.TestService;
import com.arjuna.wstx.tests.common.TestServiceBA;
import com.arjuna.wstx.tests.common.TestServiceBAClient;
import com.arjuna.wstx.tests.common.TestServiceBAImple;
import com.arjuna.wstx.tests.common.TestServiceClient;
import com.arjuna.wstx.tests.common.TestServiceImple;

/**
 *
 * Tests WSTXFeature and DisabledWSTXHandler with WS-BA protocol.
 *
 * <code>default-context-propagation</code> in standalone.xml has to be disabled.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
@RunWith(Arquillian.class)
public final class DisabledContextPropagationTest extends AbstractContextPropagationTest {

    @Deployment
    public static WebArchive getDeployment() {
        return WarDeployment.getDeployment(AbstractContextPropagationTest.class, TestService.class, TestServiceImple.class,
                TestServiceClient.class, TestServiceBA.class, TestServiceBAImple.class, TestServiceBAClient.class,
                WSTXFeature.class).addAsResource("context-handlers.xml");
    }

    /**
     * Tests close without WSTXFeature and with disabled WSTX handler.
     *
     * No coordination calls are expected.
     */
    @Test
    public void testCloseWithoutFeature() {
        TestServiceBA client = getBAClientWithoutFeature();

        beginActivity();
        client.increment();
        closeActivity();

        assertInvocations(client.getBusinessActivityInvocations());
    }

    /**
     * Tests cancel without WSTXFeature and with disabled WSTX handler.
     *
     * No coordination calls are expected.
     */
    @Test
    public void testCancelWithoutFeature() throws Exception {
        TestServiceBA client = getBAClientWithoutFeature();

        beginActivity();
        client.increment();
        cancelActivity();

        assertInvocations(client.getBusinessActivityInvocations());
    }

    /**
     * Tests service invocation without BA context, WSTXFeature, and with disabled WSTX handler.
     *
     * No coordination calls are expected.
     */
    @Test
    public void testNoActivityWithoutFeature() {
        TestServiceBA client = getBAClientWithoutFeature();

        client.increment();

        assertInvocations(client.getBusinessActivityInvocations());
    }

    /**
     * Tests invocation to the BA-unaware service without BA context, WSTXFeature, and with disabled WSTX handler.
     *
     * No coordination calls are expected.
     */
    @Test
    public void testNonTransactionalServiceWithoutFeature() {
        TestService client = getClientWithoutFeature();

        beginActivity();
        client.increment();
        closeActivity();
    }

    /**
     * Tests close with enabled WSTXFeature and with disabled WSTX handler.
     *
     * "complete", "confirmCompleted", and "close" calls are expected.
     */
    @Test
    public void testCloseWithEnabledFeature() {
        TestServiceBA client = getBAClientWithFeature(true);

        beginActivity();
        client.increment();
        closeActivity();

        assertInvocations(client.getBusinessActivityInvocations(), "complete", "confirmCompleted", "close");
    }

    /**
     * Tests cancel with enabled WSTXFeature and with disabled WSTX handler.
     *
     * "cancel" call is expected.
     */
    @Test
    public void testCancelWithEnabledFeature() throws Exception {
        TestServiceBA client = getBAClientWithFeature(true);

        beginActivity();
        client.increment();
        cancelActivity();

        assertInvocations(client.getBusinessActivityInvocations(), "cancel");
    }

    /**
     * Tests service invocation without BA context but with enabled WSTXFeature and with disabled WSTX handler.
     *
     * No coordination calls are expected.
     */
    @Test
    public void testNoActivityWithEnabledFeature() {
        TestServiceBA client = getBAClientWithFeature(true);

        client.increment();

        assertInvocations(client.getBusinessActivityInvocations());
    }

    /**
     * Tests invocation to the BA-unaware service without BA context but with enabled WSTXFeature and with disabled WSTX
     * handler.
     *
     * SOAPFaultException is expected.
     */
    @Test
    public void testNonTransactionalServiceWithEnabledFeature() {
        TestService client = getClientWithFeature(true);

        beginActivity();

        try {
            client.increment();
            throw new RuntimeException("SOAPFaultException was expected");
        } catch (SOAPFaultException e) {
            // TODO for some reason <code>@Test(expected = SOAPFaultException.class)</code> did not work.
        }
    }

    /**
     * Tests close with disabled WSTXFeature and with disabled WSTX handler.
     *
     * No coordination calls are expected.
     */
    @Test
    public void testCloseWithDisabledFeature() {
        TestServiceBA client = getBAClientWithFeature(false);

        beginActivity();
        client.increment();
        closeActivity();

        assertInvocations(client.getBusinessActivityInvocations());
    }

    /**
     * Tests cancel with disabled WSTXFeature and with disabled WSTX handler.
     *
     * No coordination calls are expected.
     */
    @Test
    public void testCancelWithDisabledFeature() throws Exception {
        TestServiceBA client = getBAClientWithFeature(false);

        beginActivity();
        client.increment();
        cancelActivity();

        assertInvocations(client.getBusinessActivityInvocations());
    }

    /**
     * Tests service invocation without BA context but with disabled WSTXFeature and with disabled WSTX handler.
     *
     * No coordination calls are expected.
     */
    @Test
    public void testNoActivityWithDisabledFeature() {
        TestServiceBA client = getBAClientWithFeature(false);

        client.increment();

        assertInvocations(client.getBusinessActivityInvocations());
    }

    /**
     * Tests invocation to the BA-unaware service without BA context but with disabled WSTXFeature and with disabled WSTX
     * handler.
     *
     * No coordination calls are expected.
     */
    @Test
    public void testNonTransactionalServiceWithDisabledFeature() {
        TestService client = getClientWithFeature(false);

        beginActivity();
        client.increment();
        closeActivity();
    }

    /**
     * Tests service invocation using client which registers JaxWSHeaderContextProcessor with binding provider manually.
     *
     * "complete", "confirmCompleted", and "close" calls are expected.
     *
     * @throws InterruptedException
     */
    @Test
    public void testCloseWithFeatureUnawareClient() throws InterruptedException {
        TestServiceBA client = getBAClientWithManuallyAddedHandler();

        beginActivity();
        client.increment();
        closeActivity();

        assertInvocations(client.getBusinessActivityInvocations(), "complete", "confirmCompleted", "close");
    }

    /**
     * Tests service invocation using client which registers JaxWSHeaderContextProcessor with binding provider manually.
     *
     * "cancel" call is expected.
     *
     * @throws InterruptedException
     */
    @Test
    public void testCancelWithFeatureUnawareClient() {
        TestServiceBA client = getBAClientWithManuallyAddedHandler();

        beginActivity();
        client.increment();
        cancelActivity();

        assertInvocations(client.getBusinessActivityInvocations(), "cancel");
    }

}
