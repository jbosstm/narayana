package com.arjuna.wstx.tests.arq.basic;

import java.net.MalformedURLException;
import java.util.List;

import org.junit.After;
import org.junit.Assert;

import com.arjuna.mw.wst11.UserTransaction;
import com.arjuna.mw.wst11.UserTransactionFactory;
import com.arjuna.wstx.tests.common.TestService;
import com.arjuna.wstx.tests.common.TestServiceAT;
import com.arjuna.wstx.tests.common.TestServiceATClient;
import com.arjuna.wstx.tests.common.TestServiceClient;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public abstract class AbstractContextPropagationTest {

    @After
    public void after() {
        UserTransaction userTransaction = UserTransactionFactory.userTransaction();

        rollbackIfActive(userTransaction);
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
     * @return TestServiceClient
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
     * Creates and resets WS-AT aware client.
     *
     * @return TestServiceAT
     */
    protected TestServiceAT getATClientWithoutFeature() {
        TestServiceAT client;

        try {
            client = TestServiceATClient.getClientWithoutFeature();
        } catch (MalformedURLException e) {
            throw new RuntimeException("Client creation failed.", e);
        }

        client.reset();

        return client;
    }

    /**
     * Creates and resets WS-AT aware client with WSTXFeature.
     *
     * @param isWSTXFeatureEnabled
     * @return TestServiceAT
     */
    protected TestServiceAT getATClientWithFeature(final boolean isWSTXFeatureEnabled) {
        TestServiceAT client;

        try {
            client = TestServiceATClient.getClientWithWSTXFeature(isWSTXFeatureEnabled);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Client creation failed.", e);
        }

        client.reset();

        return client;
    }

    /**
     * Creates and resets WS-AT client with manually added handler.
     *
     * @return TestServiceAT
     */
    protected TestServiceAT getATClientWithManuallyAddedHandler() {
        TestServiceAT client;

        try {
            client = TestServiceATClient.getClientWithManuallyAddedHandler();
        } catch (MalformedURLException e) {
            throw new RuntimeException("Client creation failed.", e);
        }

        client.reset();

        return client;
    }

    /**
     * Begins WS-AT transaction.
     */
    protected void beginTransaction() {
        UserTransaction userTransaction = UserTransactionFactory.userTransaction();

        try {
            userTransaction.begin();
        } catch (Exception e) {
            throw new RuntimeException("Begin transaction failed.", e);
        }
    }

    /**
     * Commits current WS-AT transaction.
     */
    protected void commitTransaction() {
        UserTransaction userTransaction = UserTransactionFactory.userTransaction();

        try {
            userTransaction.commit();
        } catch (Exception e) {
            throw new RuntimeException("Commit transaction failed.", e);
        }
    }

    /**
     * Rolls back current WS-AT transaction.
     */
    protected void rollbackTransaction() {
        UserTransaction userTransaction = UserTransactionFactory.userTransaction();

        try {
            userTransaction.rollback();
        } catch (Exception e) {
            throw new RuntimeException("Rollback transaction failed.", e);
        }
    }

    /**
     * Rolls back given transaction if it's active.
     *
     * @param userTransaction
     */
    protected void rollbackIfActive(UserTransaction userTransaction) {
        try {
            userTransaction.rollback();
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
