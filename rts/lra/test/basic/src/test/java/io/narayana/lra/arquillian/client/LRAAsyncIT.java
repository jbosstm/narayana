/*
 * SPDX short identifier: Apache-2.0
 */


package io.narayana.lra.arquillian.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import io.narayana.lra.arquillian.Deployer;
import io.narayana.lra.arquillian.TestBase;
import io.narayana.lra.arquillian.resource.LRAParticipant;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;

public class LRAAsyncIT extends TestBase {

    private static final Logger log = Logger.getLogger(LRAAsyncIT.class);
    private static final String SHOULD_NOT_BE_ASSOCIATED = "The narayana implementation (of the MP-LRA specification) still thinks that there is "
            + "an active LRA associated with the current thread even though all LRAs should now be finished";
    //Default maximum number of concurrent calls to LRA coordinator is 10 (see org.eclipse.microprofile.faulttolerance.Bulkhead @interface),
    //you can change the default number in the microprofile-config.properties file
    private static final int NUMBER_OF_TASKS = 10;
    //In order to have calls as much concurrent as possible
    //the number of threads should be equals or greater to the number of tasks
    private ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_TASKS);

    @ArquillianResource
    public URL baseURL;

    @Rule
    public TestName testName = new TestName();

    @Override
    public void before() {
        super.before();
        log.info("Running test " + testName.getMethodName());
    }
    @Override
    public void after() {
        super.after();
        executorService.shutdown();
    }
    @Deployment
    public static WebArchive deploy() {
        return Deployer.deploy(LRAAsyncIT.class.getSimpleName(), LRAParticipant.class);
    }

    /**
     * Invoke a resource method which in turn invokes other resources. The various
     * resource invocations are called in new or existing LRAs. Various tests are
     * performed to verify that the correct LRAs are used and the LRAs have the
     * expected status (see the resource method for the detail).
     */
    @Test
    public void testChainOfInvocations() {
        Callable<URI> callableTask = () -> {

                // Invoke a method which starts a transaction
                // (note that the method LRAParticipant.CREATE_OR_CONTINUE_LRA also invokes
                // other resource methods)
                URI lra1 = invokeInTransaction(null, LRAParticipant.CREATE_OR_CONTINUE_LRA);
                    lrasToAfterFinish.add(lra1);
                    assertEquals("LRA should still be active. The identifier of the LRA was " + lra1, LRAStatus.Active,
                            lraClient.getStatus(lra1));

                    // end the LRA
                    invokeInTransaction(lra1, LRAParticipant.END_EXISTING_LRA);


                    return lraClient.getCurrent();

        };

        List<Callable<URI>> callableTasks = new ArrayList<>();
        for(int i = 0; i< NUMBER_OF_TASKS; i++)
            callableTasks.add(callableTask);

        try {
            List<Future<URI>> futures = executorService.invokeAll(callableTasks);
            for( Future<URI> f : futures)
                assertNull(SHOULD_NOT_BE_ASSOCIATED, f.get());

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            fail("Error in testChainOfInvocations method");
        }
    }

    /**
     * test behavior when multiple LRAs are active
     */
    @Test
    public void testNoCurrent() {
            URI lra2 = invokeInTransaction(null, LRAParticipant.START_NEW_LRA);

            Callable<URI> callableTask = () -> {
                URI lra = invokeInTransaction(null, LRAParticipant.START_NEW_LRA);
                lrasToAfterFinish.add(lra);

                assertNull(SHOULD_NOT_BE_ASSOCIATED, lraClient.getCurrent());
                invokeInTransaction(lra, LRAParticipant.END_EXISTING_LRA);
                return lra;

            };


            List<Callable<URI>> callableTasks = new ArrayList<>();
            for(int i = 0; i< NUMBER_OF_TASKS; i++)
                callableTasks.add(callableTask);

            try {
                List<Future<URI>> futures = executorService.invokeAll(callableTasks);
                for( Future<URI> f : futures) {
                    f.get();
                    assertNull(SHOULD_NOT_BE_ASSOCIATED, lraClient.getCurrent());
                }

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                fail("Error in testNoCurrent method");
            }
    }

    private URI invokeInTransaction(URI lra, String resourcePath) {

            Response response = null;

            try {
                Invocation.Builder builder = client.target(UriBuilder.fromUri(baseURL.toExternalForm())
                        .path(LRAParticipant.RESOURCE_PATH).path(resourcePath).build()).request();

                if (lra != null) {
                    builder.header(LRA_HTTP_CONTEXT_HEADER, lra.toASCIIString());
                }

                response = builder.get();

                assertTrue("This test expects that the invoked resource returns the identifier of the LRA "
                        + "that was active during the invocation or an error message.", response.hasEntity());

                String responseMessage = response.readEntity(String.class);

                assertEquals(responseMessage, 200, response.getStatus());

                return URI.create(responseMessage);
            } finally {
                if (response != null) {
                    response.close();
                }
            }
    }
}