/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.jboss.jbossts.star.test;

import org.jboss.jbossts.star.annotation.Commit;
import org.jboss.jbossts.star.annotation.Prepare;
import org.jboss.jbossts.star.annotation.Rollback;
import org.jboss.jbossts.star.annotation.SRA;
import org.jboss.jbossts.star.annotation.Status;
import org.jboss.jbossts.star.client.SRAStatus;
import org.jboss.jbossts.star.client.ServerSRAFilter;
import org.jboss.jbossts.star.service.Coordinator;
import org.jboss.jbossts.star.util.TxMediaType;
import org.jboss.jbossts.star.util.TxSupport;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.jboss.jbossts.star.client.SRAClient.COORDINATOR_URL_PROP;
import static org.jboss.jbossts.star.client.SRAClient.RTS_HTTP_CONTEXT_HEADER;

public class SRATest extends BaseTest {

    private static TxSupport txn = txn = new TxSupport(TXN_MGR_URL, 6000);
    static final AtomicInteger completeCount = new AtomicInteger(0);
    static final AtomicInteger prepareCount = new AtomicInteger(0);


    @BeforeClass
    public static void beforeClass() throws Exception {
        System.setProperty(COORDINATOR_URL_PROP, TXN_MGR_URL);
        startContainer2(TXN_MGR_URL, TransactionalResource.class, Coordinator.class, ServerSRAFilter.class,
                SRAParticipant.class, SRASecondParticipant.class);
    }

    // afterClass finishes SRA in order to clean in-memory transaction map
    @AfterClass
    public static void after() {
        Collection<String> txs = txn.getTransactions();
        for( String id : txs) {
            String path = String.format("%s%s/%s", SURL, SRAParticipant.SRA_TEST_PATH,
                    SRAParticipant.END_REQUIRED_SRA_PATH);
            Map<String, String> reqHeaders = new HashMap<>();

            reqHeaders.put(RTS_HTTP_CONTEXT_HEADER, id);
            txn.httpRequest(new int[] { HttpURLConnection.HTTP_OK }, path, "GET",
                    TxMediaType.PLAIN_MEDIA_TYPE, null, null, reqHeaders);
        }

        Assert.assertEquals(0, txn.getTransactions().size());
    }

    @Before
    public void before() {
        prepareCount.set(0);
        completeCount.set(0);
    }

    @Path(SRAParticipant.SRA_TEST_PATH)
    public static class SRAParticipant {
        protected static final String SRA_TEST_PATH = "sra-participant";
        protected static final String END_REQUIRED_SRA_PATH = "required-end-true";
        protected static final String START_REQUIRED_SRA_PATH = "required-end-false";
        protected static final String END_MANDATORY_SRA_PATH = "mandatory-end-true";
        protected static final String START_MANDATORY_SRA_PATH = "mandatory-end-false";

        @GET
        @Path(START_REQUIRED_SRA_PATH)
        @SRA(value = SRA.Type.REQUIRED, end = false)
        @Consumes(TxMediaType.PLAIN_MEDIA_TYPE)
        public Response startRequiredSRA(@HeaderParam(RTS_HTTP_CONTEXT_HEADER) URI contextId,
                @DefaultValue("0") @QueryParam("accept") Integer acceptCount,
                @DefaultValue("false") @QueryParam("cancel") Boolean cancel) {

            return Response.ok(contextId).build();
        }

        @GET
        @Path(END_REQUIRED_SRA_PATH)
        @SRA(value = SRA.Type.REQUIRED, end = true)
        @Consumes(TxMediaType.PLAIN_MEDIA_TYPE)
        public Response endRequiredSRA(@HeaderParam(RTS_HTTP_CONTEXT_HEADER) URI contextId,
                @DefaultValue("0") @QueryParam("accept") Integer acceptCount,
                @DefaultValue("false") @QueryParam("cancel") Boolean cancel) {

            return Response.ok(contextId).build();
        }

        @GET
        @Path(START_MANDATORY_SRA_PATH)
        @SRA(value = SRA.Type.MANDATORY, end = false)
        @Consumes(TxMediaType.PLAIN_MEDIA_TYPE)
        public Response startMandatorySRA(@HeaderParam(RTS_HTTP_CONTEXT_HEADER) URI contextId,
                @DefaultValue("0") @QueryParam("accept") Integer acceptCount,
                @DefaultValue("false") @QueryParam("cancel") Boolean cancel) {

            return Response.ok(contextId).build();
        }

        @GET
        @Path(END_MANDATORY_SRA_PATH)
        @SRA(value = SRA.Type.MANDATORY, end = true)
        @Consumes(TxMediaType.PLAIN_MEDIA_TYPE)
        public Response endMandatorySRA(@HeaderParam(RTS_HTTP_CONTEXT_HEADER) URI contextId,
                @DefaultValue("0") @QueryParam("accept") Integer acceptCount,
                @DefaultValue("false") @QueryParam("cancel") Boolean cancel) {

            return Response.ok(contextId).build();
        }

        @POST
        @Produces(TxMediaType.PLAIN_MEDIA_TYPE)
        public String enlist(@Context UriInfo info, @QueryParam("pId") @DefaultValue("") String pId,
                @QueryParam("fault") @DefaultValue("") String fault,
                @QueryParam("twoPhaseAware") @DefaultValue("true") String twoPhaseAware,
                @QueryParam("isVolatile") @DefaultValue("false") String isVolatile,
                @QueryParam("isUnawareTwoPhaseParticipantOnePhase") @DefaultValue("true") String isUnawareOnePhase,
                String enlistUrl) throws IOException {
            return "";
        }

        @PUT
        @Path("/commit/{txid}")
        @Produces(MediaType.APPLICATION_JSON)
        @Commit
        public Response commitWork(@HeaderParam(RTS_HTTP_CONTEXT_HEADER) String atId, @PathParam("txid") String sraId)
                throws NotFoundException {
            completeCount.incrementAndGet();
            return updateState(SRAStatus.TransactionCommitted, sraId);// getCurrentActivityId());
        }

        @PUT
        @Path("/prepare/{txid}")
        @Produces(MediaType.APPLICATION_JSON)
        @Prepare
        public Response prepareWork(@PathParam("txid") String sraId) throws NotFoundException {
            prepareCount.incrementAndGet();
            return updateState(SRAStatus.TransactionPrepared, sraId);// getCurrentActivityId());
        }

        @PUT
        @Path("/rollback/{txid}")
        @Produces(MediaType.APPLICATION_JSON)
        @Rollback
        public Response rollbackWork(@PathParam("txid") String sraId) throws NotFoundException {
            return updateState(SRAStatus.TransactionRolledBack, sraId);// getCurrentActivityId());
        }

        @GET
        @Path("/status/{txid}")
        @Produces(MediaType.APPLICATION_JSON)
        @Status
        @Transactional(Transactional.TxType.NOT_SUPPORTED)
        public Response status(@PathParam("txid") String sraId) throws NotFoundException {
            return Response.status(Response.Status.OK).entity(Entity.text(SRAStatus.TransactionActive)).build(); // TODO
        }

        private Response updateState(SRAStatus status, String activityId) {
            return Response.ok().build();
        }
    }

    @Path(SRASecondParticipant.SRA_TEST_PATH)
    public static class SRASecondParticipant extends SRAParticipant{
        protected static final String SRA_TEST_PATH = "sra-second-participant";

    }
    @Test
    public void testRequiredSRAWithoutEnd() throws IOException {

        int activeTxCount = txn.getTransactions().size();

        String path = String.format("%s%s/%s", SURL, SRAParticipant.SRA_TEST_PATH,
                SRAParticipant.START_REQUIRED_SRA_PATH);

        String sraId = txn.httpRequest(new int[] { HttpURLConnection.HTTP_OK }, path, "GET",
                TxMediaType.PLAIN_MEDIA_TYPE, null, null);

        Assert.assertEquals(true, txn.getTransactions().contains(sraId));

        Assert.assertEquals(activeTxCount + 1, txn.getTransactions().size());

    }

    @Test
    public void testRequiredSRAWithEnd() throws IOException {
        int activeTxCount = txn.getTransactions().size();

        String path = String.format("%s%s/%s", SURL, SRAParticipant.SRA_TEST_PATH,
                SRAParticipant.END_REQUIRED_SRA_PATH);

        String sraId = txn.httpRequest(new int[] { HttpURLConnection.HTTP_OK }, path, "GET",
                TxMediaType.PLAIN_MEDIA_TYPE, null, null);

        Assert.assertEquals(false, txn.getTransactions().contains(sraId));
        Assert.assertEquals(activeTxCount, txn.getTransactions().size());

    }

    // mandatory transaction needs an existing transaction id to be called
    @Test
    public void testMandatorySRAWithoutEnd() throws IOException {
        int activeTxCount = txn.getTransactions().size();

        String path = String.format("%s%s/%s", SURL, SRAParticipant.SRA_TEST_PATH,
                SRAParticipant.START_REQUIRED_SRA_PATH);

        String sraId = txn.httpRequest(new int[] { HttpURLConnection.HTTP_OK }, path, "GET",
                TxMediaType.PLAIN_MEDIA_TYPE, null, null);

        Map<String, String> reqHeaders = new HashMap<>();

        reqHeaders.put(RTS_HTTP_CONTEXT_HEADER, sraId);
        path = String.format("%s%s/%s", SURL, SRAParticipant.SRA_TEST_PATH, SRAParticipant.START_MANDATORY_SRA_PATH);

        txn.httpRequest(new int[] { HttpURLConnection.HTTP_OK }, path, "GET", TxMediaType.PLAIN_MEDIA_TYPE, null, null,
                reqHeaders);

        Assert.assertEquals(true, txn.getTransactions().contains(sraId));
        Assert.assertEquals(activeTxCount + 1, txn.getTransactions().size());
    }

    @Test
    public void testMandatorySRAWithEnd() throws IOException {
        int activeTxCount = txn.getTransactions().size();

        String path = String.format("%s%s/%s", SURL, SRAParticipant.SRA_TEST_PATH,
                SRAParticipant.START_REQUIRED_SRA_PATH);

        String sraId = txn.httpRequest(new int[] { HttpURLConnection.HTTP_OK }, path, "GET",
                TxMediaType.PLAIN_MEDIA_TYPE, null, null);

        Assert.assertEquals(true, txn.getTransactions().contains(sraId));

        Map<String, String> reqHeaders = new HashMap<>();

        reqHeaders.put(RTS_HTTP_CONTEXT_HEADER, sraId);
        path = String.format("%s%s/%s", SURL, SRAParticipant.SRA_TEST_PATH, SRAParticipant.END_MANDATORY_SRA_PATH);

        txn.httpRequest(new int[] { HttpURLConnection.HTTP_OK }, path, "GET", TxMediaType.PLAIN_MEDIA_TYPE, null, null,
                reqHeaders);

        Assert.assertEquals(false, txn.getTransactions().contains(sraId));
        Assert.assertEquals(activeTxCount, txn.getTransactions().size());

    }

    // when SRA is committed the prepare and commit endpoints will be invoked for each partecipant involved in the transaction
    @Test
    public void testTwoPartecipants() throws IOException {
        int completions = completeCount.get();
        int prepared = prepareCount.get();

        String path = String.format("%s%s/%s", SURL, SRAParticipant.SRA_TEST_PATH,
                SRAParticipant.START_REQUIRED_SRA_PATH);
        String sraId = txn.httpRequest(new int[] { HttpURLConnection.HTTP_OK }, path, "GET",
                TxMediaType.PLAIN_MEDIA_TYPE, null, null);


        Assert.assertEquals(completions, completeCount.get());
        Assert.assertEquals(prepared, prepareCount.get());

        Map<String, String> reqHeaders = new HashMap<>();
        reqHeaders.put(RTS_HTTP_CONTEXT_HEADER, sraId);
        path = String.format("%s%s/%s", SURL, SRASecondParticipant.SRA_TEST_PATH,
                 SRASecondParticipant.END_MANDATORY_SRA_PATH);
        sraId = txn.httpRequest(new int[] { HttpURLConnection.HTTP_OK }, path, "GET",
                TxMediaType.PLAIN_MEDIA_TYPE, null, null, reqHeaders);


        Assert.assertEquals(completions + 2, completeCount.get());
        Assert.assertEquals(prepared + 2, prepareCount.get());

    }
}
