/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
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
 * (C) 2011,
 * @author JBoss, by Red Hat.
 */
package quickstart;

import org.jboss.jbossts.star.provider.HttpResponseException;
import org.jboss.jbossts.star.util.TxSupport;

import java.io.*;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An example of how a REST resource can act as a participant in a REST Atomic transaction.
 * For a complete implementation of a participant please refer to the test suite, in particular
 * the inner class: org.jboss.jbossts.star.test.BaseTest$TransactionalResource which implements
 * all the responsibilities of a participant
 *
 * The example sends a service request which is handled by the method incrementCounters.
 * The request includes the URL for registering durable participants within the transaction.
 * This naive implementation assumes every request with a valid enlistment URL is a request
 * a new unit of transactional work and enlists a new URL into the transaction.
 * Thus if a client makes two http requests to the method incrementCounters then the participant
 * is enlisted twice into the transaction but with different completion URLs. This facilitates
 * the demonstration of 2 phase commit processing.
 */
@Path(TransactionAwareResource.PSEGMENT)
public class TransactionAwareResource {
    public static final String PSEGMENT = "service";
    public static String FAIL_COMMIT;
    private static AtomicInteger workId = new AtomicInteger(1);
    private static volatile AtomicInteger[] counters = {new AtomicInteger(0), new AtomicInteger(0)};

    private static Work getWork(String enlistUrl, int index) {
        for (Work w : Work.work.values())
            if (w.index == index && w.enlistUrl.equals(enlistUrl))
                return w;

        return null;
    }

    @GET
    public Response incrementCounters(@Context UriInfo info,
                                      @QueryParam("enlistURL") @DefaultValue("")String enlistUrl,
                                      @QueryParam("counter") @DefaultValue("0") int index,
                                      @QueryParam("failWid") @DefaultValue("")String failWid) {
        System.out.println("Service:incrementCounters request, enlistUrl=" + enlistUrl);
        Response response = null;

        if (failWid.length() != 0) {
            FAIL_COMMIT = failWid;
            return Response.ok("will fail wid " + failWid).build();
        }

        if (index != 0 && index != 1)
            return Response.status(400).entity("Counter not supported").build();

        Work w = getWork(enlistUrl, index);
        if (w != null) {
	    System.out.println("Returning old counter");
            response = Response.ok("" + ++w.counter).build();
        } else if (enlistUrl.length() == 0) {
            response = Response.ok("non transactional request. Value=" +
				counters[index].incrementAndGet()).build();
        } else {
            int wid = workId.incrementAndGet();
            String serviceURL = info.getBaseUri() + info.getPath();
            String workURL = serviceURL + '/' + workId;

            String terminator = workURL + "/terminate";
            String participant = workURL + "/terminator";

            String pUrls = TxSupport.getParticipantUrls(terminator, participant);
            System.out.println("Service: Enlisting " + pUrls + "\non endpoint: " + enlistUrl);
            System.out.println("proxies: " +  System.getProperty("http.proxyHost") +
				':' + System.getProperty("http.proxyPort"));

            try {
                new TxSupport().httpRequest(new int[]{HttpURLConnection.HTTP_CREATED}, enlistUrl,
                        "POST", TxSupport.POST_MEDIA_TYPE, pUrls, null);
            } catch (HttpResponseException e) {
                System.out.println("Enlist error: " + e);
		e.printStackTrace();
                if (e.getActualResponse() == 404)
                    return Response.status(e.getActualResponse()).entity("No such url").build();
                else
                    return Response.status(e.getActualResponse()).
						entity("Transaction Manager service is unavailable").build();
            }

            w = new Work(enlistUrl, wid, index, counters[index].get() + 1);
	    System.out.println("Added counter: " + w.wid + " " + w.counter); 
            Work.work.put(Integer.toString(wid), w);

            serializeWork();

            response = Response.ok(Integer.toString(wid)).build();
        }

        serializeWork();

        return response;
    }

    @GET
    @Path("query")
    public Response readCounters(@QueryParam("wId") @DefaultValue("0") String wid) {
        Work w = Work.work.get(wid);

	System.out.println("Work is null?: " + w == null);

        if (w == null)
            return Response.ok("counter[0]=" + counters[0].get() + "<br/>counter[1]=" +
				counters[1].get()).build();
        else
            return Response.ok("counter[" + w.index + "]=" + w.counter).build();
    }

    /*
     * this method handles PUT requests to the url that the participant gave to the
     * REST Atomic Transactions implementation (in the someServiceRequest method).
     * This is the endpoint that the transaction manager interacts with when it needs
     * participants to prepare/commit/rollback their transactional work.
     */
    @PUT
    @Path("{wId}/terminate")
    public Response terminate(@PathParam("wId") @DefaultValue("")String wId, String content) {
        System.out.println("Service: PUT request to terminate url: wId=" + wId + ", status:=" + content);
        String status = TxSupport.getStatus(content);
        Work w = Work.work.get(wId);
	    System.out.println("Read counter: " + w.wid + " " + w.counter); 

	if (w == null) {
		System.err.println("No work available");
	}

        if (TxSupport.isPrepare(status)) {
            return Response.ok(TxSupport.toStatusContent(TxSupport.PREPARED)).build();
        } else if (TxSupport.isCommit(status)) {
            if (!serializeWork()) {
		System.err.println("Aborting transaction, could not serialize work");
                Work.work.remove(wId);
                return Response.ok(TxSupport.toStatusContent(TxSupport.ABORTED)).build();
            }

            if (wId.equals(FAIL_COMMIT)) {
                System.out.println("Service: Halting VM during commit of work unit wId=" + wId);
                Runtime.getRuntime().halt(1);
            }

	    System.out.println("Updating counter: " + w.index + " " + w.counter);
            counters[w.index].set(w.counter);
            Work.work.remove(wId);
            return Response.ok(TxSupport.toStatusContent(TxSupport.COMMITTED)).build();
        } else if (TxSupport.isAbort(status)) {
	    System.err.println("Participant told to abort");
            Work.work.remove(wId);
            serializeWork();
            return Response.ok(TxSupport.toStatusContent(TxSupport.ABORTED)).build();
        } else {
	    System.err.println("Participant received bad request");
            return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build();
        }
    }

    static boolean serializeWork() {
        try {
            FileOutputStream fos = new FileOutputStream("work.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(Work.work);
            oos.writeObject(workId);
            oos.writeObject(counters);
            oos.close();
            return true;
        } catch (IOException e) {
	    e.printStackTrace();
            return false;
        }

    }

    static boolean deserializeWork() {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream("work.ser");
            ObjectInputStream ois = null;
            ois = new ObjectInputStream(fis);

            Work.work = (Map<String, Work>) ois.readObject();
            workId = (AtomicInteger) ois.readObject();
            counters = (AtomicInteger[]) ois.readObject();
            ois.close();
            return true;
        } catch (Exception e) {
	    e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int PORT = 8081;
        String HOST = "localhost";

        System.out.println("ARGS " + args.length);
        for (int i = 0; i + 1 < args.length; i++)
            if ("-h".equals(args[i])) {
                HOST = args[++i];
            } else if ("-p".equals(args[i])) {
                PORT = Integer.valueOf(args[++i]);
            } else if ("-a".equals(args[i])) {
                String[] addr = args[++i].split(":");
                HOST = addr[0];
                PORT = Integer.valueOf(addr[1]);
            }

        deserializeWork();

        // start an embedded JAX-RS server
        JaxrsServer.startServer(HOST, PORT);

        System.out.println("JAX-RS container waiting for requests on " + HOST + ":" +
			PORT + " (for 1000 seconds) ...");
        Thread.sleep(1000000);

        // shutdown the embedded JAX-RS server
        JaxrsServer.stopServer();
    }
}
