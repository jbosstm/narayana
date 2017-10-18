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

package org.jboss.narayana.compensations.functional.recovery;

import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.narayana.compensations.functional.recovery.deployment.ExecutionResource;
import org.jboss.narayana.compensations.functional.recovery.deployment.Executor;
import org.jboss.narayana.compensations.functional.recovery.deployment.Options;
import org.jboss.narayana.compensations.functional.recovery.deployment.Result;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;

import static org.jboss.narayana.compensations.functional.recovery.ArquillianRecoveryTestUtils.BASE_VM_ARGUMENTS;
import static org.jboss.narayana.compensations.functional.recovery.ArquillianRecoveryTestUtils.BYTEMAN_VM_ARGUMENTS;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * All test cases in this class follow the same pattern:
 * 1. An application server is started.
 * 2. Transaction log is cleared.
 * 3. Test options are passed to the test executor.
 * 4. Executor triggers a specific Byteman rule required for the specific test.
 * 5. Executor triggers a specific functionality defined in the options.
 * 6. The Byteman rule crashes the server.
 * 7. After restart crash recovery is executed and expected outcome is validated.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@RunAsClient
@RunWith(Arquillian.class)
public class CrashRecoveryTestLocal {

    private static final String CONTAINER_NAME = "jboss-manual";

    private static final String DEPLOYMENT_NAME = "test";

    private static final String JBOSS_HOME = System.getenv("JBOSS_HOME");

    private static final int RECOVERY_WAIT_PERIOD = 20000;

    private static final int RECOVERY_WAIT_ITERATIONS = 5;

    private static final String TEST_ENTRY = "test-entry";

    private static final String EXECUTOR_URL = "http://localhost:8080/" + DEPLOYMENT_NAME + "/" + ExecutionResource.PATH;

    private static final String CRASH_RECOVERY_RULES = "crash-recovery-rules.btm";

    private static final String DELAYED_DESERIALIZER_REGISTRATION_RULES = "delayed-deserializer-registration-rules.btm";

    @ArquillianResource
    private ContainerController containerController;

    @ArquillianResource
    private Deployer deployer;

    private Client client;

    private ArquillianRecoveryTestUtils testUtils;

    @Deployment(name = DEPLOYMENT_NAME, managed = false, testable = false)
    public static WebArchive createTestArchive() {
        return ShrinkWrap.create(WebArchive.class, DEPLOYMENT_NAME + ".war")
                .addPackage(Executor.class.getPackage())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebInfResource("web.xml", "web.xml");
    }

    @Before
    public void before() {
        client = ClientBuilder.newClient();
        testUtils = new ArquillianRecoveryTestUtils(containerController, deployer, CONTAINER_NAME, DEPLOYMENT_NAME, JBOSS_HOME);
    }

    @After
    public void after() {
        client.close();
        testUtils.stopContainer();
    }

    /**
     * Application server is crashed after all participants` confirmCompleted operation is invoked. All participant records are
     * persisted, but transaction record is not available.
     * Expected outcome: two compensated entry.
     */
    @Test
    public void testCrashAfterConfirmCompleted() {
        Options options = Options.builder()
                .testName("CRASH_AFTER_CONFIRM_COMPLETED")
                .isCompensate(false)
                .isTxConfirm(true)
                .isTxCompensate(true)
                .isCompensatableActionConfirmation(true)
                .isCompensatableActionCompensation(true)
                .compensatableActionData(TEST_ENTRY)
                .compensationScopedData(TEST_ENTRY)
                .isDistributed(isDistributed()).build();

        Result expectedResult = Result.builder().compensatedDate(Arrays.asList(TEST_ENTRY, TEST_ENTRY))
                .confirmedData(Collections.emptyList()).build();
        prepareAndExecuteTest(options, () -> expectedResult.equals(getResult()),
                Collections.singletonList(CRASH_RECOVERY_RULES), Collections.emptyList());
    }

    /**
     * Application server is crashed after all participants` confirmCompleted operation is invoked. All participant records are
     * persisted, but transaction record is not available. In this scenario only @TxCompensate hadler is enlisted, thus one
     * phase commit optimisation would be used.
     * Expected outcome: one compensated entry.
     */
    @Test
    public void testCrashAfterConfirmCompletedOnePhase() {
        Options options = Options.builder()
                .testName("CRASH_AFTER_CONFIRM_COMPLETED")
                .isCompensate(false)
                .isTxConfirm(false)
                .isTxCompensate(true)
                .isCompensatableActionConfirmation(false)
                .isCompensatableActionCompensation(false)
                .compensatableActionData(TEST_ENTRY)
                .compensationScopedData(TEST_ENTRY)
                .isDistributed(isDistributed()).build();

        Result expectedResult = Result.builder().compensatedDate(Collections.singletonList(TEST_ENTRY))
                .confirmedData(Collections.emptyList()).build();
        prepareAndExecuteTest(options, () -> expectedResult.equals(getResult()),
                Collections.singletonList(CRASH_RECOVERY_RULES), Collections.emptyList());
    }

    /**
     * Application server is crashed before the first participant`s complete operation is invoked. All participant and
     * transaction records are persisted.
     * Expected outcome: two confirmed entry.
     */
    @Test
    public void testCrashBeforeClose() {
        Options options = Options.builder()
                .testName("CRASH_BEFORE_CLOSE")
                .isCompensate(false)
                .isTxConfirm(true)
                .isTxCompensate(true)
                .isCompensatableActionConfirmation(true)
                .isCompensatableActionCompensation(true)
                .compensatableActionData(TEST_ENTRY)
                .compensationScopedData(TEST_ENTRY)
                .isDistributed(isDistributed()).build();

        Result expectedResult = Result.builder().compensatedDate(Collections.emptyList())
                .confirmedData(Arrays.asList(TEST_ENTRY, TEST_ENTRY)).build();
        prepareAndExecuteTest(options, () -> expectedResult.equals(getResult()),
                Collections.singletonList(CRASH_RECOVERY_RULES), Collections.emptyList());
    }

    /**
     * Application server is crashed before the participant`s complete operation is invoked. In this scenario only @TxCompensate
     * handler is enlisted, thus one phase commit optimisation would be used. As a result only participant record is persisted.
     * Expected outcome: one compensated entry.
     */
    @Test
    public void testCrashBeforeCloseOnePhase() {
        Options options = Options.builder()
                .testName("CRASH_BEFORE_CLOSE")
                .isCompensate(false)
                .isTxConfirm(false)
                .isTxCompensate(true)
                .isCompensatableActionConfirmation(false)
                .isCompensatableActionCompensation(false)
                .compensatableActionData(TEST_ENTRY)
                .compensationScopedData(TEST_ENTRY)
                .isDistributed(isDistributed()).build();

        Result expectedResult = Result.builder().compensatedDate(Collections.singletonList(TEST_ENTRY))
                .confirmedData(Collections.emptyList()).build();
        prepareAndExecuteTest(options, () -> expectedResult.equals(getResult()),
                Collections.singletonList(CRASH_RECOVERY_RULES), Collections.emptyList());
    }

    @Test
    public void testCrashBeforeCloseWithDelayedDeserializerRegistration() {
        Options options = Options.builder()
                .testName("CRASH_BEFORE_CLOSE")
                .isCompensate(false)
                .isTxConfirm(true)
                .isTxCompensate(true)
                .isCompensatableActionConfirmation(true)
                .isCompensatableActionCompensation(true)
                .compensatableActionData(TEST_ENTRY)
                .compensationScopedData(TEST_ENTRY)
                .isDistributed(isDistributed()).build();

        Result expectedResult = Result.builder().compensatedDate(Collections.emptyList())
                .confirmedData(Arrays.asList(TEST_ENTRY, TEST_ENTRY)).build();
        prepareAndExecuteTest(options, () -> expectedResult.equals(getResult()),
                Arrays.asList(CRASH_RECOVERY_RULES, DELAYED_DESERIALIZER_REGISTRATION_RULES),
                Collections.singletonList(DELAYED_DESERIALIZER_REGISTRATION_RULES));
    }

    /**
     * Application server is crashed before the first participant`s compensate operation is invoked. All participant records are
     * persisted, but no transaction record is available because of the presumed abort optimisation.
     * Expected outcome: two compensated entry.
     */
    @Test
    public void testCrashBeforeCompensate() {
        Options options = Options.builder()
                .testName("CRASH_BEFORE_COMPENSATE")
                .isCompensate(true)
                .isTxConfirm(true)
                .isTxCompensate(true)
                .isCompensatableActionConfirmation(true)
                .isCompensatableActionCompensation(true)
                .compensatableActionData(TEST_ENTRY)
                .compensationScopedData(TEST_ENTRY)
                .isDistributed(isDistributed()).build();

        Result expectedResult = Result.builder().compensatedDate(Arrays.asList(TEST_ENTRY, TEST_ENTRY))
                .confirmedData(Collections.emptyList()).build();
        prepareAndExecuteTest(options, () -> expectedResult.equals(getResult()),
                Collections.singletonList(CRASH_RECOVERY_RULES), Collections.emptyList());
    }

    /**
     * Application server is crashed before the participant`s compensate operation is invoked. Participant records are
     * persisted, but no transaction record is available because of the presumed abort optimisation. In this scenario only
     * @TxCompensate handler is enlisted, thus one phase commit optimisation would be used.
     * Expected outcome: one compensated entry.
     */
    @Test
    public void testCrashBeforeCompensateOnePhase() {
        Options options = Options.builder()
                .testName("CRASH_BEFORE_COMPENSATE")
                .isCompensate(true)
                .isTxConfirm(false)
                .isTxCompensate(true)
                .isCompensatableActionConfirmation(false)
                .isCompensatableActionCompensation(false)
                .compensatableActionData(TEST_ENTRY)
                .compensationScopedData(TEST_ENTRY)
                .isDistributed(isDistributed()).build();

        Result expectedResult = Result.builder().compensatedDate(Collections.singletonList(TEST_ENTRY))
                .confirmedData(Collections.emptyList()).build();
        prepareAndExecuteTest(options, () -> expectedResult.equals(getResult()),
                Collections.singletonList(CRASH_RECOVERY_RULES), Collections.emptyList());
    }

    protected boolean isDistributed() {
        return false;
    }

    private void prepareAndExecuteTest(Options options, BooleanSupplier validator, List<String> bytemanFilesForStart,
            List<String> bytemanFilesForRestart) {
        testUtils.startContainer(bytemanFilesForStart);
        reset();
        execute(options);
        testUtils.restartContainer(bytemanFilesForRestart);
        for (int i = 0; i < RECOVERY_WAIT_ITERATIONS; i++) {
            testUtils.sleep(RECOVERY_WAIT_PERIOD);
            if (validator.getAsBoolean()) {
                return;
            }
        }
        fail("Recovery failed");
    }

    private void execute(Options options) {
        try {
            client.target(EXECUTOR_URL).request().post(Entity.entity(options, MediaType.APPLICATION_JSON));
            fail("Exception was expected");
        } catch (Exception ignored) {
        }
    }

    private void reset() {
        client.target(EXECUTOR_URL).request().delete();
    }

    private Result getResult() {
        return client.target(EXECUTOR_URL).request().get(Result.class);
    }

}