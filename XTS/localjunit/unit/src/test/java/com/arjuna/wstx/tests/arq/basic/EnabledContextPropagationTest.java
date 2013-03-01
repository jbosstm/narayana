package com.arjuna.wstx.tests.arq.basic;

import javax.xml.ws.soap.SOAPFaultException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.mw.wst11.client.WSTXFeature;
import com.arjuna.wstx.tests.arq.WarDeployment;
import com.arjuna.wstx.tests.common.TestService;
import com.arjuna.wstx.tests.common.TestServiceAT;
import com.arjuna.wstx.tests.common.TestServiceATClient;
import com.arjuna.wstx.tests.common.TestServiceATImple;
import com.arjuna.wstx.tests.common.TestServiceClient;
import com.arjuna.wstx.tests.common.TestServiceImple;

/**
*
* Tests WSTXFeature and EnabledWSTXHandler with WS-AT protocol.
*
* <code>default-context-propagation</code> in standalone.xml has to be enabled.
*
* @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
*
*/
@RunWith(Arquillian.class)
public final class EnabledContextPropagationTest extends AbstractContextPropagationTest {

    @Deployment
    public static WebArchive getDeployment() {
        return WarDeployment.getDeployment(AbstractContextPropagationTest.class, TestServiceAT.class, TestServiceATImple.class,
                TestServiceATClient.class, TestService.class, TestServiceImple.class, TestServiceClient.class,
                WSTXFeature.class).addAsResource("context-handlers.xml");
    }

    /**
     * Tests commit without WSTXFeature and with enabled WSTX handler.
     *
     * Prepare and commit calls are expected.
     *
     * @throws Exception
     */
    @Test
    public void testCommitWithoutFeature() throws Exception {
        TestServiceAT client = getATClientWithoutFeature();

        beginTransaction();
        client.increment();
        commitTransaction();

        assertInvocations(client.getTwoPhaseCommitInvocations(), "prepare", "commit");
    }

    /**
     * Tests rollback without WSTXFeature and with enabled WSTX handler.
     *
     * Rollback call is expected.
     */
    @Test
    public void testRollbackWithoutFeature() {
        TestServiceAT client = getATClientWithoutFeature();

        beginTransaction();
        client.increment();
        rollbackTransaction();

        assertInvocations(client.getTwoPhaseCommitInvocations(), "rollback");
    }

    /**
     * Tests service invocation without transaction context, WSTXFeature, and with enabled WSTX handler.
     *
     * No two phase commit calls are expected.
     */
    @Test
    public void testNoTransactionWithoutFeature() {
        TestServiceAT client = getATClientWithoutFeature();

        client.increment();

        assertInvocations(client.getTwoPhaseCommitInvocations());
    }

    /**
     * Tests invocation to the non-transactional service without transaction context, WSTXFeature, and with enabled WSTX
     * handler.
     *
     * No two phase commit calls and no exception is expected.
     */
    @Test
    public void testNonTransactionalServiceWithoutFeature() {
        TestService client = getClientWithoutFeature();

        beginTransaction();
        client.increment();
        commitTransaction();
    }

    /**
     * Tests commit with enabled WSTXFeature and with enabled WSTX handler.
     *
     * Prepare and commit calls are expected.
     */
    @Test
    public void testCommitWithEnabledFeature() {
        TestServiceAT client = getATClientWithFeature(true);

        beginTransaction();
        client.increment();
        commitTransaction();

        assertInvocations(client.getTwoPhaseCommitInvocations(), "prepare", "commit");
    }

    /**
     * Tests rollback with enabled WSTXFeature and with enabled WSTX handler.
     *
     * Rollback call is expected.
     */
    @Test
    public void testRollbackWithEnabledFeature() throws Exception {
        TestServiceAT client = getATClientWithFeature(true);

        beginTransaction();
        client.increment();
        rollbackTransaction();

        assertInvocations(client.getTwoPhaseCommitInvocations(), "rollback");
    }

    /**
     * Tests service invocation without transaction context but with enabled WSTXFeature and with enabled WSTX handler.
     *
     * No two phase commit calls are expected.
     */
    @Test
    public void testNoTransactionWithEnabledFeature() {
        TestServiceAT client = getATClientWithFeature(true);

        client.increment();

        assertInvocations(client.getTwoPhaseCommitInvocations());
    }

    /**
     * Tests invocation to the non-transactional service without transaction context but with enabled WSTXFeature, and with
     * enabled WSTX handler.
     *
     * SOAPFaultException is expected.
     */
    @Test
    public void testNonTransactionalServiceWithEnabledFeature() {
        TestService client = getClientWithFeature(true);

        beginTransaction();

        try {
            client.increment();
            throw new RuntimeException("SOAPFaultException was expected");
        } catch (SOAPFaultException e) {
            // TODO for some reason <code>@Test(expected = SOAPFaultException.class)</code> did not work.
        }
    }

    /**
     * Tests commit with disabled WSTXFeature and with enabled WSTX handler.
     *
     * No two phase commit calls are expected.
     */
    @Test
    public void testCommitWithDisabledFeature() {
        TestServiceAT client = getATClientWithFeature(false);

        beginTransaction();
        client.increment();
        commitTransaction();

        assertInvocations(client.getTwoPhaseCommitInvocations());
    }

    /**
     * Tests rollback with disabled WSTXFeature and with enabled WSTX handler.
     *
     * No two phase commit calls are expected.
     */
    @Test
    public void testRollbackWithDisabledFeature() {
        TestServiceAT client = getATClientWithFeature(false);

        beginTransaction();
        client.increment();
        rollbackTransaction();

        assertInvocations(client.getTwoPhaseCommitInvocations());
    }

    /**
     * Tests service invocation without transaction context but with disabled WSTXFeature and enabled WSTX handler.
     *
     * No two phase commit calls are expected.
     */
    @Test
    public void testNoTransactionWithDisabledFeature() {
        TestServiceAT client = getATClientWithFeature(false);

        client.increment();

        assertInvocations(client.getTwoPhaseCommitInvocations());
    }

    /**
     * Tests invocation to the non-transactional service without transaction context but with disabled WSTXFeature, and with
     * enabled WSTX handler.
     *
     * No two phase commit calls and no exceptions are expected.
     */
    @Test
    public void testNonTransactionalServiceWithDisabledFeature() {
        TestService client = getClientWithFeature(false);

        beginTransaction();
        client.increment();
        commitTransaction();
    }

    /**
     * Tests service invocation using client which registers JaxWSHeaderContextProcessor with binding provider manually.
     *
     * Prepare and commit calls are expected.
     *
     * @throws InterruptedException
     */
    @Test
    public void testCommitWithFeatureUnawareClient() throws InterruptedException {
        TestServiceAT client = getATClientWithManuallyAddedHandler();

        beginTransaction();
        client.increment();
        commitTransaction();

        assertInvocations(client.getTwoPhaseCommitInvocations(), "prepare", "commit");
    }

    /**
     * Tests service invocation using client which registers JaxWSHeaderContextProcessor with binding provider manually.
     *
     * Rollback call is expected.
     *
     * @throws InterruptedException
     */
    @Test
    public void testRollbackWithFeatureUnawareClient() {
        TestServiceAT client = getATClientWithManuallyAddedHandler();

        beginTransaction();
        client.increment();
        rollbackTransaction();

        assertInvocations(client.getTwoPhaseCommitInvocations(), "rollback");
    }

}
