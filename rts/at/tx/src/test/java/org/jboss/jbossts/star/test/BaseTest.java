/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
 *
 * (C) 2010
 * @author JBoss Inc.
 */
package org.jboss.jbossts.star.test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.net.ssl.SSLContext;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.internal.jta.transaction.arjunacore.AtomicAction;
import com.squareup.okhttp.OkHttpClient;
import io.undertow.Undertow;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.jboss.jbossts.star.provider.HttpResponseException;
import org.jboss.jbossts.star.provider.HttpResponseMapper;
import org.jboss.jbossts.star.provider.NotFoundMapper;
import org.jboss.jbossts.star.provider.TMUnavailableMapper;
import org.jboss.jbossts.star.provider.TransactionStatusMapper;
import org.jboss.jbossts.star.service.Coordinator;
import org.jboss.jbossts.star.service.TMApplication;
import org.jboss.jbossts.star.util.HttpConnectionCreator;
import org.jboss.jbossts.star.util.TxLinkNames;
import org.jboss.jbossts.star.util.TxMediaType;
import org.jboss.jbossts.star.util.TxStatus;
import org.jboss.jbossts.star.util.TxStatusMediaType;
import org.jboss.jbossts.star.util.TxSupport;
import org.jboss.logging.Logger;
import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.jboss.resteasy.spi.Registry;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.sun.grizzly.http.SelectorThread;
//import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;

public class BaseTest {
    protected static final Logger log = Logger.getLogger(BaseTest.class);

    protected static final ExecutorService executor = Executors.newFixedThreadPool(4);
    protected static boolean USE_RESTEASY = false;
    protected static boolean USE_UNDERTOW = true;
    private static HttpServer grizzlyServer;
    protected static final String USE_SPDY_PROP = "rts.usespdy";
    protected static final String USE_SSL_PROP = "rts.usessl";
    protected static final boolean USE_SPDY = Boolean.getBoolean(USE_SPDY_PROP);
    protected static final boolean USE_SSL = Boolean.getBoolean(USE_SSL_PROP) || USE_SPDY;
    protected static String SCHEME = USE_SSL ? "https" : "http";

    protected static final int PORT = 58081;
    protected static String SURL = SCHEME + "://localhost:" + PORT + '/';
    protected static final String PSEGMENT = "txresource";
    protected static final String NO_RESPONSE_SEGMENT = "no-response";
    protected static final String PURL = SURL + PSEGMENT;
    protected static final String PURL_NO_RESPONSE = PURL + "/" + NO_RESPONSE_SEGMENT;
    protected static String TXN_MGR_URL = SURL + "tx/transaction-manager";
    private static TJWSEmbeddedJaxrsServer server = null;
    private static SelectorThread threadSelector = null;
    private static UndertowJaxrsServer undertow;

    protected static void setTxnMgrUrl(String txnMgrUrl) {
        TXN_MGR_URL = txnMgrUrl;
    }

    protected static void startUndertow(Class<?> ... classes) throws Exception {
        undertow = new UndertowJaxrsServer();

        undertow.start(Undertow.builder().addHttpListener(PORT, "localhost"));

        undertow.deploy(new TMApplication(classes));//, SURL + "tx/");

        System.out.printf("server is ready:");
    }

    protected static void startRestEasy(Class<?> ... classes) throws Exception {
        server = new TJWSEmbeddedJaxrsServer();
        server.setPort(PORT);
        server.start();
        Registry registry = server.getDeployment().getRegistry();
        ResteasyProviderFactory factory = server.getDeployment().getDispatcher().getProviderFactory();

        if (classes != null)
            for (Class<?> clazz : classes)
                registry.addPerRequestResource(clazz);

        factory.registerProvider(TMUnavailableMapper.class);
        factory.registerProvider(TransactionStatusMapper.class);
        factory.registerProvider(HttpResponseMapper.class);
        factory.registerProvider(NotFoundMapper.class);
    }

    public static Future<String> submitJob(Callable<String> job) {
        return executor.submit(job);
    }

    public static void submitJob(Runnable job) {
        executor.submit(job);
    }

    protected static void startJersey(String packages) throws Exception {
        final Map<String, String> initParams = new HashMap<String, String>();

        initParams.put("com.sun.jersey.config.property.packages", packages);
        initParams.put(ServerProperties.PROVIDER_PACKAGES, packages);
//        initParams.put(ServerProperties.PROVIDER_PACKAGES, Coordinator.class.getPackage().getName());

        try {
            if (USE_SSL) {
                URI baseUri = UriBuilder.fromUri(SURL).build();
                String trustStoreFile = System.getProperty("javax.net.ssl.trustStore");
                String trustStorePswd = System.getProperty("javax.net.ssl.trustStorePassword");

                if (trustStoreFile == null || trustStorePswd == null)
                    throw new IllegalArgumentException("Please set SSL javax.net.ssl.trustStore and javax.net.ssl.trustStorePassword to use SPDY suppport");
                grizzlyServer = SpdyEnabledHttpServer.create(baseUri, initParams, trustStoreFile, trustStorePswd, 50, USE_SPDY);
            } else {
               URI baseUri = UriBuilder.fromUri(SURL).build();
//                threadSelector = GrizzlyWebContainerFactory.create(baseUri, initParams);

                final ResourceConfig resourceConfig = new ResourceConfig();//Coordinator.class);
                resourceConfig.packages("org.jboss.jbossts.star.service", "org.jboss.jbossts.star.provider", "org.jboss.jbossts.star.test");
                grizzlyServer = GrizzlyHttpServerFactory.createHttpServer(baseUri, resourceConfig);

            }
        } catch (IOException e) {
            log.infof(e, "Error starting Grizzly");
        }
    }

    public static void startContainer(String txnMgrUrl, String packages, Class<?> ... classes) throws Exception {
        TxSupport.setTxnMgrUrl(txnMgrUrl);

        if (USE_SPDY)
            TxSupport.setHttpConnectionCreator(new SpdyConnection());

        if (USE_RESTEASY)
            startRestEasy(classes);
        else if (USE_UNDERTOW)
            startUndertow(classes);
        else
            startJersey(packages);
    }

    private static class SpdyConnection implements HttpConnectionCreator {
        private OkHttpClient spdyClient;

        SpdyConnection() {

            spdyClient = new OkHttpClient();

            try {
                //sslContext = getSSLContext();
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, null, null);
                spdyClient.setSslSocketFactory(sslContext.getSocketFactory());
            } catch (Exception e) {
                throw new AssertionError(); // The system has no TLS. Just give up.
            }
        }

        @Override
        public HttpURLConnection open(URL url) throws IOException {
            return spdyClient.open(url);
        }
    }

    public static void startContainer(String txnMgrUrl) throws Exception {
        startContainer(txnMgrUrl,
                "org.jboss.jbossts.star.service;org.jboss.jbossts.star.provider;org.jboss.jbossts.star.test",
                TransactionalResource.class, Coordinator.class);
    }

    public static void startContainer2(String txnMgrUrl, Class<?> ... resourceClasses) throws Exception {
        startContainer(txnMgrUrl,
                "org.jboss.jbossts.star.service;org.jboss.jbossts.star.provider;org.jboss.jbossts.star.test",
                resourceClasses);
    }

    private static void clearObjectStore(String type) {
        try {
            RecoveryStore recoveryStore = StoreManager.getRecoveryStore();
            InputObjectState states = new InputObjectState();

            if (recoveryStore.allObjUids(type, states) && states.notempty()) {
                boolean finished = false;

                do {
                    Uid uid = UidHelper.unpackFrom(states);

                    if (uid.notEquals(Uid.nullUid())) {
                        recoveryStore.remove_committed(uid, type);
                    } else {
                        finished = true;
                    }
                } while (!finished);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected boolean writeObjectStoreRecord(OSRecordHolder holder) {
        try {
            return StoreManager.getRecoveryStore().write_committed(holder.uid, holder.type, holder.oos);
        } catch (ObjectStoreException e) {
            return false;
        }
    }

    protected static OSRecordHolder readObjectStoreRecord(String type) {
        try {
            RecoveryStore recoveryStore = StoreManager.getRecoveryStore();
            InputObjectState states = new InputObjectState();

            if (recoveryStore.allObjUids(type, states) && states.notempty()) {

                Uid uid = UidHelper.unpackFrom(states);

                if (uid.notEquals(Uid.nullUid())) {
                    InputObjectState ios = recoveryStore.read_committed(uid, type);

                    return new OSRecordHolder(uid, type, ios);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @AfterClass
    public static void afterClass() throws Exception {
        if (undertow != null) {
            undertow.stop();
            undertow = null;
        }

        if (server != null) {
            server.stop();
            server = null;
        }

        if (threadSelector != null) {
            threadSelector.stopEndpoint();
            threadSelector = null;
        } else if (grizzlyServer != null) {
            grizzlyServer.shutdownNow();
        }
    }

    @Before
    public void before() throws Exception {
        TransactionalResource.faults.clear();
        clearObjectStore(new AtomicAction().type());
    }

    @Test
    public void nullTest() throws Exception {
        // need at least one test
    }

    protected String enlistResource(TxSupport txn, String pUrl) {
        return txn.enlistTestResource(pUrl, false);
    }

    private StringBuilder getResourceUpdateUrl(String pUrl, String pid, String name, String value) {
        StringBuilder sb = new StringBuilder(pUrl);

        if (pid != null)
            sb.append("?pId=").append(pid).append("&name=");
        else
            sb.append("?name=");

        sb.append(name);

        if (value != null)
            sb.append("&value=").append(value);

        return sb;
    }

    /**
     * Modify a transactional participant
     * @param txn the transaction
     * @param pUrl the  transactional participant
     * @param pid an id
     * @param name name of a property to update
     * @param value the new value of the property
     * @return the response body
     */
    protected String modifyResource(TxSupport txn, String pUrl, String pid, String name, String value) {
        // tell the resource to modify some data and pass the transaction enlistment url along with the request
        return txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK},
                getResourceUpdateUrl(pUrl, pid, name, value).toString(), "GET", TxMediaType.PLAIN_MEDIA_TYPE);
    }

    protected String getResourceProperty(TxSupport txn, String pUrl, String pid, String name) {
        return txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_NO_CONTENT},
                getResourceUpdateUrl(pUrl, pid, name, null).toString(), "GET", TxMediaType.PLAIN_MEDIA_TYPE);
    }

    private static class Work {
        String id;
        String tid;
        String uri;
        String pLinks;
        String enlistUrl;
        String recoveryUrl;
        String fault;
        Map<String, String> oldState;
        Map<String, String> newState;
        String status;
        int vStatus = 0;
        int syncCount = 0;
        int commitCnt = 0;
        int prepareCnt = 0;
        int rollbackCnt = 0;
        int commitOnePhaseCnt = 0;

        Work(String id, String tid, String uri, String pLinks, String enlistUrl, String recoveryUrl, String fault) {
            this.id = id;
            this.tid = tid;
            this.uri = uri;
            this.pLinks = pLinks;
            this.enlistUrl = enlistUrl;
            this.recoveryUrl = recoveryUrl;
            this.fault = fault;
            this.oldState = new HashMap<String, String> ();
            this.newState = new HashMap<String, String> ();
        }

        public void start() {
            newState.clear();
            newState.putAll(oldState);
        }

        public void end(boolean commit) {
            if (commit) {
                oldState.clear();
                oldState.putAll(newState);
            }
        }

        public boolean inTxn() {
            return status != null && TxStatus.fromStatus(status).isActive();
            // return TxSupport.isActive(status);
        }
    }

    @Path(PSEGMENT)
    public static class TransactionalResource {
        private static int pid = 0;
        static Map<String, Work> faults = new HashMap<String, Work> ();

        public Work makeWork(TxSupport txn, String baseURI, String id, String txId, String enlistUrl,
                             boolean twoPhaseAware, boolean isVolatile, String recoveryUrl, String fault, boolean isUnawareParticipantOnePhase) {
            String linkHeader = twoPhaseAware ?
                    txn.makeTwoPhaseAwareParticipantLinkHeader(baseURI, isVolatile, id, txId) :
                    txn.makeTwoPhaseUnAwareParticipantLinkHeader(baseURI, isVolatile, id, txId, isUnawareParticipantOnePhase);

            return new Work(id, txId, baseURI + '/' + id, linkHeader, enlistUrl, recoveryUrl, fault);
        }

        private String moveParticipant(Work work, String nid, String register,
                                       boolean twoPhaseAware, boolean isVolatile) {
            TxSupport txn = new TxSupport();

            faults.remove(work.id);
            work = makeWork(txn, PURL, nid, work.tid, work.enlistUrl,
                    twoPhaseAware, isVolatile, work.recoveryUrl, work.fault, true);
            faults.put(nid, work);
            // if register is true then tell the transaction manager about the new location - otherwise the old
            // URIs will be used for transaction termination. This is used to test that the coordinator uses
            // the recovery URI correctly
            if ("true".equals(register)) {
                Map<String, String> reqHeaders = new HashMap<String, String> ();

                reqHeaders.put("Link", work.pLinks);
                txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK}, work.recoveryUrl, "PUT", TxMediaType.POST_MEDIA_TYPE, null,
                        null, reqHeaders);
            }

            return nid;
        }

        @SuppressWarnings({"UnusedDeclaration"})
        @GET
        public String getBasic(@Context UriInfo info,
                               @QueryParam("pId") @DefaultValue("")String pId,
                               @QueryParam("context") @DefaultValue("")String ctx,
                               @QueryParam("name") @DefaultValue("")String name,
                               @QueryParam("value") @DefaultValue("")String value,
                               @QueryParam("query") @DefaultValue("pUrl") String query,
                               @QueryParam("arg") @DefaultValue("") String arg,
                               @QueryParam("twoPhaseAware") @DefaultValue("true") String twoPhaseAware,
                               @QueryParam("isVolatile") @DefaultValue("false") String isVolatileParticipant,
                               @QueryParam("register") @DefaultValue("true") String register) {
            Work work = faults.get(pId);
            String res = null;
            boolean isVolatile = "true".equals(isVolatileParticipant);
            boolean isTwoPhaseAware = "true".equals(twoPhaseAware);

            if (name.length() != 0) {
                if (value.length() != 0) {
                    if (work == null) {
                        work = makeWork(new TxSupport(), TxSupport.extractUri(info), String.valueOf(++pid),
                                null, null, isTwoPhaseAware, isVolatile, null, null, true);
                        work.oldState.put(name, value);
                        faults.put(work.id, work);
                        return work.id;
                    }

                    work.newState.put(name, value);
                }

                if (work != null) {
                    if ("syncCount".equals(name))
                        res = String.valueOf(work.syncCount);
                    else if ("commitCnt".equals(name))
                        res = String.valueOf(work.commitCnt);
                    else if ("prepareCnt".equals(name))
                        res = String.valueOf(work.prepareCnt);
                    else if ("rollbackCnt".equals(name))
                        res = String.valueOf(work.rollbackCnt);
                    else if ("commitOnePhaseCnt".equals(name))
                        res = String.valueOf(work.commitOnePhaseCnt);
                    else if (work.inTxn())
                        res = work.newState.get(name);
                    else
                        res = work.oldState.get(name);
                }
            }


            if (work == null)
                throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);

            if ("move".equals(query))
                res = moveParticipant(work, arg, register, isTwoPhaseAware, isVolatile);
            else if ("recoveryUrl".equals(query))
                res = work.recoveryUrl;
            else if ("status".equals(query))
                res = work.status;
            else if (res == null)
                res = work.pLinks;

            return res; // null will generate a 204 status code (no content)
        }

        @POST
        @Produces(TxMediaType.PLAIN_MEDIA_TYPE)
        public String enlist(@Context UriInfo info, @QueryParam("pId") @DefaultValue("")String pId,
                             @QueryParam("fault") @DefaultValue("")String fault,
                             @QueryParam("twoPhaseAware") @DefaultValue("true")String twoPhaseAware,
                             @QueryParam("isVolatile") @DefaultValue("false")String isVolatile,
                             @QueryParam("isUnawareTwoPhaseParticipantOnePhase") @DefaultValue("true") String isUnawareOnePhase,
                             String enlistUrl) throws IOException {
            Work work = faults.get(pId);
            TxSupport txn = new TxSupport();
            String txId = enlistUrl.substring(enlistUrl.lastIndexOf('/') + 1);
            boolean isTwoPhaseAware = "true".equals(twoPhaseAware);
            boolean isVolatileParticipant = "true".equals(isVolatile);
            boolean isUnawareParticipantOnePhase = "true".equals(isUnawareOnePhase);
            String vRegistration = null; // URI for registering with the volatile phase
            String vParticipantLink = null;  // URI for handling pre and post 2PC phases
            String path = TxSupport.extractUri(info);

            if (work == null) {
                int id = ++pid;

                work = makeWork(txn, path, String.valueOf(id), txId, enlistUrl,
                        isTwoPhaseAware, isVolatileParticipant, null, fault, isUnawareParticipantOnePhase);
            } else {
                Work newWork = makeWork(txn, path, work.id, txId, enlistUrl,
                        isTwoPhaseAware, isVolatileParticipant, null, fault, isUnawareParticipantOnePhase);
                newWork.oldState = work.oldState;
                newWork.newState = work.newState;
                work = newWork;
            }

            if (enlistUrl.indexOf(',') != -1) {
                String[] urls = enlistUrl.split(",");

                if (urls.length < 2)
                    throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);

                enlistUrl = urls[0];
                vRegistration = urls[1];

                String vParticipant = new StringBuilder(path).append('/').append(work.id)
                        .append('/').append(txId).append('/').append("vp").toString();
                vParticipantLink = txn.addLink2(
                        new StringBuilder(), TxLinkNames.VOLATILE_PARTICIPANT, vParticipant,true).toString();
            }

            try {
                // enlist TestResource in the transaction as a participant
                work.recoveryUrl = txn.enlistParticipant(enlistUrl, work.pLinks);

                if (vParticipantLink != null)
                    txn.enlistVolatileParticipant(vRegistration, vParticipantLink);
            } catch (HttpResponseException e) {
                throw new WebApplicationException(e.getActualResponse());
            }

            work.status = TxStatus.TransactionActive.name();
            work.start();

            faults.put(work.id, work);

            return work.id;
        }

        @POST
        @Produces(TxMediaType.PLAIN_MEDIA_TYPE)
        @Path(NO_RESPONSE_SEGMENT)
        public String enlistNoResponseParticipant(@Context UriInfo info, @QueryParam("pId") @DefaultValue("")String pId,
                             @QueryParam("fault") @DefaultValue("")String fault,
                             @QueryParam("twoPhaseAware") @DefaultValue("true")String twoPhaseAware,
                             @QueryParam("isVolatile") @DefaultValue("false")String isVolatile,
                             @QueryParam("isUnawareTwoPhaseParticipantOnePhase") @DefaultValue("true") String isUnawareOnePhase,
                             String enlistUrl) throws IOException {

            return enlist(info, pId, fault, twoPhaseAware, isVolatile, isUnawareOnePhase, enlistUrl);
        }

        @SuppressWarnings({"UnusedDeclaration"})
        @PUT
        @Path("{pId}/{tId}/vp")
        public Response directSynchronizations(@PathParam("pId") @DefaultValue("")String pId,
                                               @PathParam("tId") @DefaultValue("")String tId,
                                               String content) {
            return synchronizations(pId, tId, content);
        }

        @SuppressWarnings({"UnusedDeclaration"})
        @PUT
        @Path("{pId}/{tId}/volatile-participant")
        public Response synchronizations(@PathParam("pId") @DefaultValue("")String pId,
                                         @PathParam("tId") @DefaultValue("")String tId,
                                         String content) {
            Work work = faults.get(pId);
            TxStatus txStatus;
            int vStatus;

            if (work == null)
                return Response.ok().build();

            txStatus = content != null ? TxStatus.fromStatus(content) : TxStatus.TransactionStatusUnknown;

            vStatus = txStatus.equals(TxStatus.TransactionStatusUnknown) ? 1 : 2;

            if (vStatus == 2 && work.vStatus == 0) {
                // afterCompletion but coordinator never called beforeCompletion
                return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build();
            }

            work.vStatus = vStatus;
            work.syncCount += 1;

            if (vStatus == 1 && "V_PREPARE".equals(work.fault))
                return Response.status(HttpURLConnection.HTTP_CONFLICT).build();
            else if (vStatus == 2 && "V_COMMIT".equals(work.fault))
                return Response.status(HttpURLConnection.HTTP_CONFLICT).build();

            return Response.ok().build();
        }

        @SuppressWarnings({"UnusedDeclaration"})
        @PUT
        @Path("{pId}/{tId}/terminator")
        public Response terminate(@PathParam("pId") @DefaultValue("")String pId,
                                  @PathParam("tId") @DefaultValue("")String tId,
                                  String content) {
            TxStatus status = TxSupport.toTxStatus(content);

            //String status = TxSupport.getStatus(content);
            Work work = faults.get(pId);

            if (work == null)
                return Response.status(HttpURLConnection.HTTP_NOT_FOUND).build();

            String fault = work.fault;

            if (status.isPrepare()) {
                if ("READONLY".equals(fault)) {
//                    faults.remove(pId);
                    work.status = TxStatus.TransactionReadOnly.name();
                } else if ("PREPARE_FAIL".equals(fault)) {
//                    faults.remove(pId);
                    return Response.status(HttpURLConnection.HTTP_CONFLICT).build();
                    //throw new WebApplicationException(HttpURLConnection.HTTP_CONFLICT);
                } else {
                    if ("PDELAY".equals(fault)) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                        }
                    }
                    work.status = TxStatus.TransactionPrepared.name();
                }
            } else if (status.isCommit() || status.isCommitOnePhase()) {
                if ("HALT".equals(fault))
                    Runtime.getRuntime().halt(1);
                else if ("H_HAZARD".equals(fault))
                    work.status = TxStatus.TransactionHeuristicHazard.name();
                else if ("H_ROLLBACK".equals(fault))
                    work.status = TxStatus.TransactionHeuristicRollback.name();
                else if ("H_MIXED".equals(fault))
                    work.status = TxStatus.TransactionHeuristicMixed.name();
                else if ("CRUNTIME".equals(fault)) {
                    work.status = TxStatus.TransactionStatusUnknown.name();
                    throw new RuntimeException("Test runtime exception");
                } else {
                    if ("CDELAY".equals(fault)) {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            // ok
                        }
                    }
                    work.status = status.isCommitOnePhase() ? TxStatus.TransactionCommittedOnePhase.name()
                            : TxStatus.TransactionCommitted.name();

                    work.end(true);
                }
            } else if (status.isAbort()) {
                if ("H_HAZARD".equals(fault))
                    work.status = TxStatus.TransactionHeuristicHazard.name();
                else if ("H_COMMIT".equals(fault))
                    work.status = TxStatus.TransactionHeuristicCommit.name();
                else if ("H_MIXED".equals(fault))
                    work.status = TxStatus.TransactionHeuristicMixed.name();
                else {
                    if ("ADELAY".equals(fault)) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            // ok
                        }
                    }
                    work.status = TxStatus.TransactionRolledBack.name();
                    work.end(false);
//                    faults.remove(pId);
                }
            } else {
                return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build();
                //throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
            }

            //return TxSupport.toStatusContent(work.status);
            return Response.ok(TxSupport.toStatusContent(work.status)).build();
        }

        @PUT
        @Path(NO_RESPONSE_SEGMENT + "/{pId}/{tId}/terminator")
        public Response terminateWithoutResponse(@PathParam("pId") @DefaultValue("")String pId,
                                  @PathParam("tId") @DefaultValue("")String tId,
                                  String content) {

            Response response = terminate(pId, tId, content);

            return Response.status(response.getStatus()).build();
        }

        @PUT
        @Path("{pId}/{tId}/prepare")
        public Response prepare(@PathParam("pId") @DefaultValue("")String pId, @PathParam("tId") @DefaultValue("")String tId, String content) {
            Work work = faults.get(pId);
            if (work != null)
                work.prepareCnt += 1;
            return terminate(pId, tId, TxStatusMediaType.TX_PREPARED);
        }

        @PUT
        @Path("{pId}/{tId}/commit")
        public Response commit(@PathParam("pId") @DefaultValue("")String pId, @PathParam("tId") @DefaultValue("")String tId, String content) {
            Work work = faults.get(pId);
            if (work != null)
                work.commitCnt += 1;
            return terminate(pId, tId, TxStatusMediaType.TX_COMMITTED);
        }

        @PUT
        @Path("{pId}/{tId}/rollback")
        public Response rollback(@PathParam("pId") @DefaultValue("")String pId, @PathParam("tId") @DefaultValue("")String tId, String content) {
            Work work = faults.get(pId);
            if (work != null)
                work.rollbackCnt += 1;
            return terminate(pId, tId, TxStatusMediaType.TX_ROLLEDBACK);
        }

        @PUT
        @Path("{pId}/{tId}/commit-one-phase")
        public Response commmitOnePhase(@PathParam("pId") @DefaultValue("")String pId, @PathParam("tId") @DefaultValue("")String tId, String content) {
            Work work = faults.get(pId);
            if (work != null)
                work.commitOnePhaseCnt += 1;
            return terminate(pId, tId, TxStatusMediaType.TX_COMMITTED_ONE_PHASE);
        }

        @SuppressWarnings({"UnusedDeclaration"})
        @HEAD
        @Path("{pId}/{tId}/participant")
        public Response getTerminator(@Context UriInfo info, @PathParam("pId") @DefaultValue("")String pId, @PathParam("tId") @DefaultValue("")String tId) {
            Work work = faults.get(pId);

            if (work == null)
                return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build();

            Response.ResponseBuilder builder = Response.ok();

            builder.header("Link", work.pLinks);

            return builder.build();
        }

        @SuppressWarnings({"UnusedDeclaration"})
        @GET
        @Path("{pId}/{tId}/participant")
        public String getStatus(@PathParam("pId") @DefaultValue("")String pId, @PathParam("tId") @DefaultValue("")String tId) {
            Work work = faults.get(pId);

            if (work == null)
                throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);

            return TxSupport.toStatusContent(work.status);

        }

        @SuppressWarnings({"UnusedDeclaration"})
        @DELETE
        @Path("{pId}/{tId}/participant")
        public void forgetWork(@PathParam("pId") String pId, @PathParam("tId") String tId) {
            Work work = faults.get(pId);

            if (work == null)
                throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);

            faults.remove(pId);

        }
    }
}
