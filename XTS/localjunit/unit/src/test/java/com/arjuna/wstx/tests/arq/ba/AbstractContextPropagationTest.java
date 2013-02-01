package com.arjuna.wstx.tests.arq.ba;

import java.net.MalformedURLException;
import java.util.List;

import org.junit.After;
import org.junit.Assert;

import com.arjuna.mw.wst11.UserBusinessActivity;
import com.arjuna.mw.wst11.UserBusinessActivityFactory;
import com.arjuna.wstx.tests.common.TestService;
import com.arjuna.wstx.tests.common.TestServiceBA;
import com.arjuna.wstx.tests.common.TestServiceBAClient;
import com.arjuna.wstx.tests.common.TestServiceClient;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public abstract class AbstractContextPropagationTest {

    @After
    public void after() {
        UserBusinessActivity userBusinessActivity = UserBusinessActivityFactory.userBusinessActivity();

        cancelIfActive(userBusinessActivity);
    }

    /**
     * Creates and resets WS-AT and WS-BA unaware client.
     *
     * @return TestService
     */
    protected TestService getClientWithoutFeature() {
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
    protected TestService getClientWithFeature(final boolean isWSTXFeatureEnabled) {
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
    protected TestServiceBA getBAClientWithoutFeature() {
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
    protected TestServiceBA getBAClientWithFeature(final boolean isWSTXFeatureEnabled) {
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
    protected TestServiceBA getBAClientWithManuallyAddedHandler() {
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
    protected void beginActivity() {
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
    protected void closeActivity() {
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
    protected void cancelActivity() {
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
    protected void cancelIfActive(UserBusinessActivity userBusinessActivity) {
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
    protected void assertInvocations(List<String> actual, String... expected) {
        Assert.assertArrayEquals(expected, actual.toArray());
    }

}
