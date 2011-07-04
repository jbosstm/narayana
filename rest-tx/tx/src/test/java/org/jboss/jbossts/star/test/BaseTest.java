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

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;
import org.jboss.logging.Logger;

import org.jboss.jbossts.star.provider.*;
import org.jboss.jbossts.star.service.Coordinator;
import org.jboss.jbossts.star.util.LinkHolder;
import org.jboss.jbossts.star.util.TxSupport;
import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;
import org.jboss.resteasy.spi.Registry;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.junit.*;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class BaseTest {
    protected final static Logger log = Logger.getLogger(BaseTest.class);

    protected static boolean USE_RESTEASY = false;

    protected static final int PORT = 58081;
    protected static final String SURL = "http://127.0.0.1:" + PORT + '/';
    protected static final String PSEGMENT = "txresource";
    protected static final String PURL = SURL + PSEGMENT;
    protected static String TXN_MGR_URL = SURL + "tx/transaction-manager";
    private static TJWSEmbeddedJaxrsServer server = null;
    private static SelectorThread threadSelector = null;

    protected static void setTxnMgrUrl(String txnMgrUrl) {
        TXN_MGR_URL = txnMgrUrl;
    }
    
    protected static void startRestEasy(Class<?> ... classes) throws Exception
    {
        server = new TJWSEmbeddedJaxrsServer();
        server.setPort(PORT);
        server.start();
        Registry registry = server.getDeployment().getRegistry();
        ResteasyProviderFactory factory = server.getDeployment().getDispatcher().getProviderFactory();

        if (classes != null)
            for (Class<?> clazz : classes)
                registry.addPerRequestResource(clazz);

        factory.addExceptionMapper(TMUnavailableMapper.class);
        factory.addExceptionMapper(TransactionStatusMapper.class);
        factory.addExceptionMapper(HttpResponseMapper.class);
        factory.addExceptionMapper(NotFoundMapper.class);
    }

    protected static void startJersey(String packages) throws Exception {
        final URI baseUri= UriBuilder.fromUri(SURL).build();
        final Map<String, String> initParams = new HashMap<String, String>();

        initParams.put("com.sun.jersey.config.property.packages", packages);

        try {
            threadSelector = GrizzlyWebContainerFactory.create(baseUri, initParams);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static void startContainer(String txnMgrUrl, String packages, Class<?> ... classes) throws Exception {
        TxSupport.setTxnMgrUrl(txnMgrUrl);
        
        if (USE_RESTEASY)
            startRestEasy(classes);
        else
            startJersey(packages);
    }

    public static void startContainer(String txnMgrUrl) throws Exception {
        startContainer(txnMgrUrl,
                "org.jboss.jbossts.star.service;org.jboss.jbossts.star.provider;org.jboss.jbossts.star.test",
                TransactionalResource.class, Coordinator.class);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        if (server != null) {
            server.stop();
            server = null;
        }

        if (threadSelector != null) {
            threadSelector.stopEndpoint();
            threadSelector = null;
        }
    }

    @Before
    public void before() throws Exception {
        TransactionalResource.faults.clear();
    }

    @Test
    public void nullTest() throws Exception
    {
        // need at least one test
    }

    protected String enlistResource(TxSupport txn, String pUrl)
    {
        return txn.enlist(pUrl);
    }

    private StringBuilder getResourceUpdateUrl(String pUrl, String pid, String name, String value)
    {
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
    protected String modifyResource(TxSupport txn, String pUrl, String pid, String name, String value)
    {
        // tell the resource to modify some data and pass the transaction enlistment url along with the request
        return txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK},
                getResourceUpdateUrl(pUrl, pid, name, value).toString(), "GET", TxSupport.POST_MEDIA_TYPE, null, null);
    }

    protected String getResourceProperty(TxSupport txn, String pUrl, String pid, String name)
    {
        return txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_NO_CONTENT},
                getResourceUpdateUrl(pUrl, pid, name, null).toString(), "GET", TxSupport.POST_MEDIA_TYPE, null, null);
    }

    private static class Work
    {
        String id;
        String tid;
        String uri;
        String pUrls;
        String enlistUrl;
        String recoveryUrl;
        String fault;
        Map<String, String> oldState;
        Map<String, String> newState;
        String status;

        Work(String id, String tid, String uri, String pUrls, String enlistUrl, String recoveryUrl, String fault) {
            this.id = id;
            this.tid = tid;
            this.uri = uri;
            this.pUrls = pUrls;
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
            return TxSupport.isActive(status);
        }
    }

    @Path(PSEGMENT)
    public static class TransactionalResource
    {
        private static int pid = 0;
        static Map<String, Work> faults = new HashMap<String, Work> ();

        public Work makeWork(String id, String txId, String enlistUrl, String recoveryUrl, String fault) {
            String pURI = PURL + '/' + id;
            String terminator = new StringBuilder().append(pURI).append('/').append(txId).append("/terminate").toString();
            String participant = new StringBuilder().append(pURI).append('/').append(txId).append("/terminator").toString();
            String pUrls = TxSupport.getParticipantUrls(terminator, participant);

            return new Work(id, txId, pURI, pUrls, enlistUrl, recoveryUrl, fault);
        }

        private String moveParticipant(Work work, String nid, String register)
        {
            faults.remove(work.id);
            work = makeWork(nid, work.tid, work.enlistUrl, work.recoveryUrl, work.fault);
            faults.put(nid, work);
            // now tell the transaction manager about the new location
            if ("true".equals(register))
                new TxSupport().httpRequest(new int[] {HttpURLConnection.HTTP_OK},
                        work.recoveryUrl, "PUT", TxSupport.POST_MEDIA_TYPE, work.pUrls, null);

            return nid;
        }

        @GET
        public String getBasic(@QueryParam("pId") @DefaultValue("")String pId,
                               @QueryParam("context") @DefaultValue("")String ctx,
                               @QueryParam("name") @DefaultValue("")String name,
                               @QueryParam("value") @DefaultValue("")String value,
                               @QueryParam("query") @DefaultValue("pUrl") String query,
                               @QueryParam("arg") @DefaultValue("") String arg,
                               @QueryParam("register") @DefaultValue("true") String register)
        {
            Work work = faults.get(pId);
            String res = null;

            if (name.length() != 0) {
                if (value.length() != 0) {
                    if (work == null){
                        work = makeWork(Integer.toString(++pid), null, null, null, null);
                        work.oldState.put(name, value);
                        faults.put(work.id, work);
                        return work.id;
                    }

                    work.newState.put(name, value);
                }

                if (work != null)
                    if (work.inTxn())
                        res = work.newState.get(name);
                    else
                        res = work.oldState.get(name);
            }


            if (work == null)
                //return Response.status(HttpURLConnection.HTTP_NOT_FOUND).build();
                throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);

            if ("move".equals(query))
                res = moveParticipant(work, arg, register);
            else if ("recoveryUrl".equals(query))
                res = work.recoveryUrl;
            else if ("status".equals(query))
                res = work.status;
            else if (res == null && work != null)
                res = work.pUrls;

            return res; // null will generate a 204 status code (no content)
        }

        @POST
        @Produces(TxSupport.PLAIN_MEDIA_TYPE)
        public String enlist(@QueryParam("pId") @DefaultValue("")String pId, @QueryParam("fault") @DefaultValue("")String fault, String enlistUrl) throws IOException {
            Map<String, String> links = new HashMap<String, String>();
            Work work = faults.get(pId);

            if (work == null)
                work = makeWork(Integer.toString(++pid), enlistUrl.substring(enlistUrl.lastIndexOf('/') + 1), enlistUrl, null, fault);

            // enlist in the transaction as a participant
            try {
                new TxSupport().httpRequest(new int[] {HttpURLConnection.HTTP_CREATED}, enlistUrl, "POST", TxSupport.POST_MEDIA_TYPE, work.pUrls, links);
                work.recoveryUrl = links.get("location");
            } catch (HttpResponseException e) {
                throw new WebApplicationException(e.getActualResponse());
            }

            work.status = TxSupport.RUNNING;
            work.start();

            faults.put(work.id, work);

            return work.id;
        }

        @PUT
        @Path("{pId}/{tId}/terminate")
        public Response terminate(@PathParam("pId") @DefaultValue("")String pId, @PathParam("tId") @DefaultValue("")String tId, String content) {
            String status = TxSupport.getStatus(content);
            Work work = faults.get(pId);

            if (work == null)
                return Response.status(HttpURLConnection.HTTP_NOT_FOUND).build();

            String fault = work.fault;

            if (TxSupport.isPrepare(status)) {
                if ("READONLY".equals(fault)) {
//                    faults.remove(pId);
                    work.status = TxSupport.READONLY;
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
                    work.status = TxSupport.PREPARED;
                }
            } else if (TxSupport.isCommit(status)) {
                if ("H_HAZARD".equals(fault))
                    work.status = TxSupport.H_HAZARD;
                else if ("H_ROLLBACK".equals(fault))
                    work.status = TxSupport.H_ROLLBACK;
                else if ("H_MIXED".equals(fault))
                    work.status = TxSupport.H_MIXED;
                else {
                    if ("CDELAY".equals(fault)) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                        }
                    }
                    work.status = TxSupport.COMMITTED;
                    work.end(true);
                }
            } else if (TxSupport.isAbort(status)) {
                if ("H_HAZARD".equals(fault))
                    work.status = TxSupport.H_HAZARD;
                else if ("H_COMMIT".equals(fault))
                    work.status = TxSupport.H_COMMIT;
                else if ("H_MIXED".equals(fault))
                    work.status = TxSupport.H_MIXED;
                else {
                    if ("ADELAY".equals(fault)) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                        }
                    }
                    work.status = TxSupport.ABORTED;
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

        @HEAD
        @Path("{pId}/{tId}/terminator")
        public Response getTerminator(@Context UriInfo info, @PathParam("pId") @DefaultValue("")String pId, @PathParam("tId") @DefaultValue("")String tId) {
            Work work = faults.get(pId);

            if (work == null)
                return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build();

            Response.ResponseBuilder builder = Response.ok();

            LinkHolder pUrls = new LinkHolder(work.pUrls);
            String pTerminator = pUrls.get(TxSupport.TERMINATOR_LINK);

            TxSupport.setLinkHeader(builder, TxSupport.TERMINATOR_LINK, TxSupport.TERMINATOR_LINK, pTerminator, null);

            return builder.build();
        }

        @GET
        @Path("{pId}/{tId}/terminator")
        public String getStatus(@PathParam("pId") @DefaultValue("")String pId, @PathParam("tId") @DefaultValue("")String tId) {
            Work work = faults.get(pId);

            if (work == null)
                throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);

            return TxSupport.toStatusContent(work.status);

        }

        @DELETE
        @Path("{pId}/{tId}/terminator")
        public void forgetWork(@PathParam("pId") String pId, @PathParam("tId") String tId) {
            Work work = faults.get(pId);

            if (work == null)
                throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);

            faults.remove(pId);

        }
    }
}
