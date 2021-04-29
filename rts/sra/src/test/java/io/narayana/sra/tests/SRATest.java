package io.narayana.sra.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.jboss.jbossts.star.service.Coordinator;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.jboss.resteasy.test.TestPortProvider;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import io.narayana.sra.annotation.SRA;
import io.narayana.sra.client.SRAClient;
import io.narayana.sra.client.ServerSRAFilter;
import io.narayana.sra.demo.service.BookingException;
import io.narayana.sra.logging.SRALogger;

public class SRATest {
	private static UndertowJaxrsServer server;
	public static final String COORDINATOR_PATH_NAME = "rest-at-coordinator/tx/transaction-manager";
	private static final String TERMINAL_LRA_PROP = "terminateLRA";
	private static ContainerRequestContext containerRequestContext;
	
	@Inject
	static
	ServerSRAFilter sraFilter;
	@Rule
	public TestName testName = new TestName();
	@Context
	protected static ResourceInfo resourceInfo;
	
	@Inject
	static WebTarget hotelPath;
	private static SRAClient sraClient;
	private static SRA sra;
	private static Client client;

	@Before
	public void before() throws URISyntaxException {
		SRALogger.logger.debugf("Starting test %s", testName);
		server = new UndertowJaxrsServer().start();
		sraClient = new SRAClient();

		Client client = ClientBuilder.newClient();
		String coordinatorPath = TestPortProvider.generateURL('/' + COORDINATOR_PATH_NAME);
	}

	@Path("/test")
	public static class Participant {
		private Response getResult(boolean cancel, SRA lraId) {
			Response.Status status = cancel ? Response.Status.INTERNAL_SERVER_ERROR : Response.Status.OK;

			return Response.status(status).entity(lraId.annotationType()).build();
		}

		@GET
		@Path("start")
		@SRA(value = SRA.Type.REQUIRED, delayCommit = true)
		public Response startSRA(SRA lraId) {
			return getResult(false, lraId);
		}
		@ApplicationPath("/base")
		public static class SRAParticipant extends Application {
			@Override
			public Set<Class<?>> getClasses() {
				HashSet<Class<?>> classes = new HashSet<>();
				classes.add(Participant.class);
				classes.add(Coordinator.class);
				classes.add(ServerSRAFilter.class);
				return classes;
			}
		}

		@Test
		public void test_delayCommit() throws IOException, BookingException {
			Response r1 = client.target(COORDINATOR_PATH_NAME + "/xyz/status").request().get();
		    assertEquals("LRA id xyz should not exist", Response.Status.NOT_FOUND.getStatusCode(), r1.getStatus());
			sra = (SRA) new SRACreatorTest();
			startSRA(sra);
			containerRequestContext.setProperty("delayCommit", sra);
			sraFilter.filter(containerRequestContext);
			assertNotNull(containerRequestContext.getProperty(TERMINAL_LRA_PROP));
			assertTrue(sra.delayCommit());
			
		}
	}
}
