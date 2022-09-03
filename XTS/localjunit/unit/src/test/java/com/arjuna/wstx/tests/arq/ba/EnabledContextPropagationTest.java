/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.arjuna.wstx.tests.arq.ba;

import jakarta.xml.ws.soap.SOAPFaultException;

import com.arjuna.mw.wst11.UserBusinessActivity;
import com.arjuna.mw.wst11.UserBusinessActivityFactory;
import com.arjuna.mw.wst11.UserTransaction;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst.WrongStateException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
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

import java.net.MalformedURLException;
import java.util.List;

/**
 * Tests WSTXFeature and EnabledWSTXHandler with WS-BA protocol.
 *
 * <code>default-context-propagation</code> in standalone.xml has to be enabled.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@RunWith(Arquillian.class)
public final class EnabledContextPropagationTest {

    @Deployment
    public static WebArchive getDeployment() {
        return WarDeployment.getDeployment(TestService.class, TestServiceImple.class,
                TestServiceClient.class, TestServiceBA.class, TestServiceBAImple.class, TestServiceBAClient.class,
                WSTXFeature.class).addAsResource("context-handlers.xml");
    }

    @After
    public void after() {
        UserBusinessActivity userBusinessActivity = UserBusinessActivityFactory.userBusinessActivity();

        cancelIfActive(userBusinessActivity);
    }

    /**
     * Tests close without WSTXFeature and with enabled WSTX handler.
     *
     * "complete", "confirmCompleted", and "close" calls are expected.
     */
    @Test
    public void testCloseWithoutFeature() {
        TestServiceBA client = getBAClientWithoutFeature();

        beginActivity();
        client.increment();
        closeActivity();

        assertInvocations(client.getBusinessActivityInvocations(), "complete", "confirmCompleted", "close");
    }

    /**
     * Tests cancel without WSTXFeature and with enabled WSTX handler.
     *
     * "cancel" call is expected.
     */
    @Test
    public void testCancelWithoutFeature() throws Exception {
        TestServiceBA client = getBAClientWithoutFeature();

        beginActivity();
        client.increment();
        cancelActivity();

        assertInvocations(client.getBusinessActivityInvocations(), "cancel");
    }

    /**
     * Tests service invocation without BA context, WSTXFeature, and with enabled WSTX handler.
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
     * Tests invocation to the BA-unaware service without BA context, WSTXFeature, and with enabled WSTX handler.
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
     * Tests close with enabled WSTXFeature and with enabled WSTX handler.
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
     * Tests cancel with enabled WSTXFeature and with enabled WSTX handler.
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
     * Tests service invocation without BA context but with enabled WSTXFeature and with enabled WSTX handler.
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
     * Tests invocation to the BA-unaware service without BA context but with enabled WSTXFeature and with enabled WSTX handler.
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
     * Tests close with disabled WSTXFeature and with enabled WSTX handler.
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
     * Tests cancel with disabled WSTXFeature and with enabled WSTX handler.
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
     * Tests service invocation without BA context but with disabled WSTXFeature and with enabled WSTX handler.
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
     * Tests invocation to the BA-unaware service without BA context but with disabled WSTXFeature and with enabled WSTX
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

    @Test
    public void testActivityWithActiveJTA() throws WrongStateException, SystemException, UnknownTransactionException {
        TestServiceBA client = getBAClientWithFeature(true);

        UserTransaction.getUserTransaction().begin();
        beginActivity();
        client.increment();
        closeActivity();
        UserTransaction.getUserTransaction().rollback();

        assertInvocations(client.getBusinessActivityInvocations(), "complete", "confirmCompleted", "close");
    }

    /**
     * Creates and resets WS-AT and WS-BA unaware client.
     *
     * @return TestService
     */
    private TestService getClientWithoutFeature() {
        TestService client;

        try {
            client = TestServiceClient.getClientWithoutFeature();
        } catch (MalformedURLException e) {
            throw new RuntimeException("Client creation failed.", e);
        }

        client.reset();

        return client;
    }

    /**
     * Creates and resets WS-AT and WS-BA unaware client with WSTXFeature.
     *
     * @param isWSTXFeatureEnabled
     * @return TestService
     */
    private TestService getClientWithFeature(final boolean isWSTXFeatureEnabled) {
        TestService client;

        try {
            client = TestServiceClient.getClientWithWSTXFeature(isWSTXFeatureEnabled);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Client creation failed.", e);
        }

        client.reset();

        return client;
    }

    /**
     * Creates and resets WS-BA aware client.
     *
     * @return TestServiceBA
     */
    private TestServiceBA getBAClientWithoutFeature() {
        TestServiceBA client;

        try {
            client = TestServiceBAClient.getClientWithoutFeature();
        } catch (MalformedURLException e) {
            throw new RuntimeException("Client creation failed.", e);
        }

        client.reset();

        return client;
    }

    /**
     * Creates and resets WS-BA aware client with WSTXFeature.
     *
     * @param isWSTXFeatureEnabled
     * @return TestServiceBA
     */
    private TestServiceBA getBAClientWithFeature(final boolean isWSTXFeatureEnabled) {
        TestServiceBA client;

        try {
            client = TestServiceBAClient.getClientWithWSTXFeature(isWSTXFeatureEnabled);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Client creation failed.", e);
        }

        client.reset();

        return client;
    }

    /**
     * Creates and resets WS-BA aware client with manually added handler.
     *
     * @return TestServiceBA
     */
    private TestServiceBA getBAClientWithManuallyAddedHandler() {
        TestServiceBA client;

        try {
            client = TestServiceBAClient.getClientWithManuallyAddedHandler();
        } catch (MalformedURLException e) {
            throw new RuntimeException("Client creation failed.", e);
        }

        client.reset();

        return client;
    }

    /**
     * Begins business activity.
     */
    private void beginActivity() {
        UserBusinessActivity userBusinessActivity = UserBusinessActivityFactory.userBusinessActivity();

        try {
            userBusinessActivity.begin();
        } catch (Exception e) {
            throw new RuntimeException("Begin business activity failed.", e);
        }
    }

    /**
     * Closes current business activity.
     */
    private void closeActivity() {
        UserBusinessActivity userBusinessActivity = UserBusinessActivityFactory.userBusinessActivity();

        try {
            userBusinessActivity.close();
        } catch (Exception e) {
            throw new RuntimeException("Close business activity failed.", e);
        }
    }

    /**
     * Cancels current business activity.
     */
    private void cancelActivity() {
        UserBusinessActivity userBusinessActivity = UserBusinessActivityFactory.userBusinessActivity();

        try {
            userBusinessActivity.cancel();
        } catch (Exception e) {
            throw new RuntimeException("Cancel business activity failed.", e);
        }
    }

    /**
     * Cancels given activity if it's active.
     *
     * @param userBusinessActivity
     */
    private void cancelIfActive(UserBusinessActivity userBusinessActivity) {
        try {
            userBusinessActivity.cancel();
        } catch (Throwable t) {
        }
    }

    /**
     * Compares and asserts two invocation lists.
     *
     * @param actual
     * @param expected
     */
    private void assertInvocations(List<String> actual, String... expected) {
        Assert.assertArrayEquals(expected, actual.toArray());
    }

}
