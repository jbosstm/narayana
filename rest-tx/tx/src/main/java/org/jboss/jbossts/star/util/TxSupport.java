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
package org.jboss.jbossts.star.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.jboss.jbossts.star.provider.HttpResponseException;
import org.jboss.logging.Logger;
import org.jboss.resteasy.spi.Link;

/**
 * Various utilities for sending HTTP messages
 */
public class TxSupport
{
    protected static final Logger log = Logger.getLogger(TxSupport.class);

    private static Pattern NVP_PATTERN = Pattern.compile("\\b\\w+\\s*=\\s*.*"); // matches name=value pairs
    private static String LINK_REGEX = "<(.*?)>.*rel=\"(.*?)\"";
    private static Pattern LINK_PATTERN = Pattern.compile(LINK_REGEX);

    public static int PORT = 8080;
    public static String BIND_ADDRESS = System.getProperty("jboss.bind.address", "127.0.0.1");
    public static String BASE_URL = "http://" + BIND_ADDRESS + ':';
    public static final String TX_CONTEXT = System.getProperty("rest.tx.context.path", "/rest-tx");
    /**
     * The REST path prefix for the transaction and recovery coordinator URLS
     */
    public static final String TX_PATH = "/tx/";
    /**
     * The REST path for the transaction coordinator. Thus the full path for the coordinator
     * is <web server context> + TX_PATH + TX_SEGMENT
     */
    public static final String TX_SEGMENT = "transaction-manager";

    public static final String RC_SEGMENT = "recovery-coordinator";

    public static final String DEF_TX_URL = BASE_URL + PORT + TX_CONTEXT + TX_PATH + TX_SEGMENT;
    public static String TXN_MGR_URL = DEF_TX_URL;


    public static final String URI_SEPARATOR = ";";

    public static final String ABORT_ONLY = "TransactionRollbackOnly";
    public static final String ABORTING = "TransactionRollingBack";
    public static final String ABORTED = "TransactionRolledBack";
    public static final String COMMITTING = "TransactionCommitting";
    public static final String COMMITTED = "TransactionCommitted";
    public static final String COMMITTED_ONE_PHASE = "TransactionCommittedOnePhase";
    public static final String H_ROLLBACK = "TransactionHeuristicRollback";
    public static final String H_COMMIT = "TransactionHeuristicCommit";
    public static final String H_HAZARD = "TransactionHeuristicHazard";
    public static final String H_MIXED = "TransactionHeuristicMixed";
    public static final String PREPARING = "TransactionPreparing";
    public static final String PREPARED = "TransactionPrepared";
    public static final String RUNNING = "TransactionActive";

    public static final String READONLY = "TransactionReadOnly";

    public static final String TX_ACTIVE = toStatusContent(RUNNING);
    public static final String TX_PREPARED = toStatusContent(PREPARED);
    public static final String TX_COMMITTED = toStatusContent(COMMITTED);
    public static final String TX_ABORTED = toStatusContent(ABORTED);
    public static final String TX_H_MIXED = toStatusContent(H_MIXED);
    public static final String TX_H_ROLLBACK = toStatusContent(H_ROLLBACK);

    public static final String DO_COMMIT = toStatusContent(COMMITTED);
    public static final String DO_ABORT = toStatusContent(ABORTED);

    public static final String STATUS_MEDIA_TYPE = "application/txstatus";
    public static final String POST_MEDIA_TYPE = "application/x-www-form-urlencoded";
    public static final String PLAIN_MEDIA_TYPE = "text/plain";

    public static final String LOCATION_LINK = "location";
    public static final String TERMINATOR_LINK = "terminator";
    public static final String PARTICIPANT_LINK = "durableparticipant";

    public static final String TIMEOUT_PROPERTY = "timeout";
    public static final String STATUS_PROPERTY = "txStatus";

    private Map<String, String> links = new HashMap<String, String>();
    private int status = -1;
    private String body = null;
    private String txnMgr;

    public static void setTxnMgrUrl(String txnMgrUrl) {
        TXN_MGR_URL = txnMgrUrl;
    }

    public TxSupport(String txnMgr) {
        this.txnMgr = txnMgr;
    }

    public TxSupport() {
        this(TXN_MGR_URL);
    }

    public static void addLinkHeader(Response.ResponseBuilder response, UriInfo info, String title, String name, String ... pathComponents)
    {
        String basePath = info.getMatchedURIs().get(0);
        UriBuilder builder = info.getBaseUriBuilder();
        builder.path(basePath);

        for (String component : pathComponents)
            builder.path(component);

        String uri = builder.build().toString();

        setLinkHeader(response, title, name, uri, null);
    }

    public static void setLinkHeader(Response.ResponseBuilder builder, String title, String rel, String href, String type)
    {
        Link link = new Link(title, rel, href, type, null);
        setLinkHeader(builder, link);
    }

    public static void setLinkHeader(Response.ResponseBuilder builder, Link link)
    {
        builder.header("Link", link);
    }

    public Collection<String> getTransactions() throws HttpResponseException {
        String content = httpRequest(new int[] {HttpURLConnection.HTTP_OK}, txnMgr, "GET", STATUS_MEDIA_TYPE, null, null);
        Collection<String> txns = new ArrayList<String> ();

        // the returned document contains transaction URLs delimited by the TXN_LIST_SEP character
        // If the string is empty split returns an array of size 1 with the empty string as the element
        if(content.length() == 0) {
            return txns;
        }
        for (String txn : content.split(URI_SEPARATOR))
            txns.add(txn.trim());

        return txns;
    }
    public int txCount() throws HttpResponseException {
        String content = httpRequest(new int[] {HttpURLConnection.HTTP_OK}, txnMgr, "GET", STATUS_MEDIA_TYPE, null, null);

        return content.length() == 0 ? 0 : content.split(URI_SEPARATOR).length;
    }

    // Transaction control methods
    public TxSupport startTx() throws HttpResponseException {
        httpRequest(new int[] {HttpURLConnection.HTTP_CREATED}, txnMgr, "POST", POST_MEDIA_TYPE, "", links);
        return this;
    }
    public TxSupport startTx(long milliseconds) throws HttpResponseException {
        httpRequest(new int[] {HttpURLConnection.HTTP_CREATED}, txnMgr, "POST", POST_MEDIA_TYPE, TIMEOUT_PROPERTY + "=" + milliseconds, links);
        return this;
    }
    public String commitTx() throws HttpResponseException {
        return httpRequest(new int[] {HttpURLConnection.HTTP_OK}, links.get(TERMINATOR_LINK), "PUT", STATUS_MEDIA_TYPE, DO_COMMIT, null);
    }
    public String rollbackTx() throws HttpResponseException {
        return httpRequest(new int[] {HttpURLConnection.HTTP_OK}, links.get(TERMINATOR_LINK), "PUT", STATUS_MEDIA_TYPE, DO_ABORT, null);
    }
    public String txStatus() throws HttpResponseException {
        return httpRequest(new int[] {HttpURLConnection.HTTP_OK}, links.get(LOCATION_LINK), "GET", null, null, null);
    }

    public String txUrl() {
        return links.get(LOCATION_LINK);
    }
    public String txTerminatorUrl() {
        return links.get(TERMINATOR_LINK);
    }
    public String enlistUrl() {
        return links.get(PARTICIPANT_LINK);
    }


    public String getBody() {
        return body;
    }
    public int getStatus() {
        return status;
    }

    public void refreshLinkHeaders(Map<String, String> linkHeaders) throws HttpResponseException {
        httpRequest(new int[] {HttpURLConnection.HTTP_OK}, links.get(LOCATION_LINK), "HEAD", STATUS_MEDIA_TYPE, null, linkHeaders);
    }

    public String enlist(String pUrl) throws HttpResponseException {
        return httpRequest(new int[] {HttpURLConnection.HTTP_OK}, pUrl, "POST", POST_MEDIA_TYPE, enlistUrl(), null);
    }

    public String httpRequest(int[] expect, String url, String method, String mediaType, String content, Map<String, String> linkHeaders) throws HttpResponseException {
        HttpURLConnection connection = null;

        try {
            connection = openConnection(null, url, method, mediaType, content);
            connection.setReadTimeout(5000);
            status = connection.getResponseCode();

            try {
                body = (status != -1 ? getContent(connection) : "");
            } catch (IOException e) {
                body = "";
            }

            if (linkHeaders != null) {
                extractLinkHeaders(connection, linkHeaders);
                addLocationHeader(connection, linkHeaders);
            }

			if (log.isTraceEnabled())
				log.trace("httpRequest:" +
					"\n\turl: " + url +
					"\n\tmethod: " + method +
					"\n\tmediaType: " + mediaType +
					"\n\tcontent: " + content +
					"\n\tresponse code: " + status +
					"\n\tresponse body: " + body
				);

            if (expect != null && expect.length != 0) {
                for (int sc : expect)
                    if (sc == status)
                        return body;

                throw new HttpResponseException(null, body, expect, status);
            } else {
                return body;
            }
        } catch (IOException e) {
            throw new HttpResponseException(e, "", expect, HttpURLConnection.HTTP_UNAVAILABLE);
        } finally {
            if (connection != null)
                connection.disconnect();
        }
    }

    private void extractLinkHeaders(HttpURLConnection connection, Map<String, String> links) {
        Collection<String> linkHeaders = connection.getHeaderFields().get("Link");

        if (linkHeaders == null)
            linkHeaders = connection.getHeaderFields().get("link");

        if (linkHeaders != null) {
            for (String link : linkHeaders) {
                String[] lhs = link.split(","); // links are separated by a comma

                for (String lnk : lhs) {
                    Matcher m = LINK_PATTERN.matcher(lnk);
                    if (m.find() && m.groupCount() > 1)
                        links.put(m.group(2), m.group(1));
                }
            }
        }
    }

    private void addLocationHeader(HttpURLConnection connection, Map<String, String> links) {
        try {
            if (connection.getResponseCode() == HttpURLConnection.HTTP_CREATED)
                links.put("location", connection.getHeaderField("location"));
        } catch (IOException e) {
        }
    }

    private HttpURLConnection openConnection(HttpURLConnection connection, String url, String method, String contentType,
                                                   String content) throws IOException {
        if (connection != null)
            connection.disconnect();

        connection = (HttpURLConnection) new URL(url).openConnection();

        connection.setRequestMethod(method);

        if (contentType != null)
            connection.setRequestProperty("Content-Type", contentType);

        if (content != null) {
            connection.setDoOutput(true);

            OutputStream os = null;
            try {
                os = connection.getOutputStream();
                os.write(content.getBytes());
                os.flush();
            } finally {
                if (os != null) {
                    os.close();
                }
            }
        }

        return connection;
    }

    private String getContent(HttpURLConnection connection) throws IOException {
        return getContent(connection, new StringBuilder()).toString();
    }

    private StringBuilder getContent(HttpURLConnection connection, StringBuilder builder) throws IOException {
        char[] buffer = new char[1024];
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            int wasRead;

            do 
            {
                wasRead = reader.read(buffer, 0, 1024);
                if (wasRead > 0)
                    builder.append(buffer, 0, wasRead);
            } 
            while (wasRead > -1);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return builder;
    }

    public static String getParticipantUrls(String terminatorUrl, String participantUrl) {
        return new StringBuilder().append(TERMINATOR_LINK).append('=').append(terminatorUrl).
                append(URI_SEPARATOR).append(PARTICIPANT_LINK).append('=').append(participantUrl).toString();
    }

    public static String getStringValue(String content, String name)
    {
        Map<String, String> matches = new HashMap<String, String>();

        TxSupport.matchNames(matches, content, null);

        return matches.get(name);
    }

    public static int getIntValue(String content, String name, int defValue)
    {
        String v = getStringValue(content, name);

        if (v != null)
            try {
                return Integer.valueOf(v);
            } catch (NumberFormatException e) {
                //
            }

        return defValue;
    }

    /**
     * Parse a string for name=value pairs
     * TODO java.util.Scanner might be more efficient
     * @param pairs the name value pairs contained in content
     * @param content a string containing name=value substrings
     */
    public static void matchNames(Map<String, String> pairs, String content, String splitChars) {
        if (content != null) {
            String[] lines;

            if (splitChars == null) {
                lines = new String[] {content};
            } else {
                lines = content.split(splitChars);
            }

            for (String line : lines) {
                Matcher m = NVP_PATTERN.matcher(line);

                while (m.find()) {
                    String[] tokens = m.group().trim().split("\\s+");
                    for (String tok : tokens) {
                        String[] pair = tok.split("=");

                        if (pair.length > 1)
                            pairs.put(pair[0], pair[1]);
                    }
                }
            }
        }
    }
    
    public static UriBuilder getUriBuilder(UriInfo info, int npaths, String ... paths)
    {
        UriBuilder builder = info.getBaseUriBuilder();

        if (npaths > 0){
            List<PathSegment> segments = info.getPathSegments();

            for (int i = 0; i < npaths; i++)
                builder.path(segments.get(i).getPath());
        } else {
            String basePath = info.getMatchedURIs().get(0);

            builder.path(basePath);
        }

        for (String path : paths)
            builder.path(path);

        return builder;
    }

    public static URI getUri(UriInfo info, int npaths, String ... paths)
    {
        return getUriBuilder(info, npaths, paths).build();
    }

    public static String buildURI(UriBuilder builder, String ... pathComponents)
    {
        for (String component : pathComponents)
            builder.path(component);

        return builder.build().toString();
    }

    public static String getStatus(String statusContent) {
        return getStringValue(statusContent, STATUS_PROPERTY);
    }

    public static String toContent(String property, String status) {
        return new StringBuilder(property).append('=').append(status).toString();
    }

    public static String toStatusContent(String status) {
        return toContent(STATUS_PROPERTY, status);
    }

    public static boolean isPrepare(String status) {
        return PREPARED.equals(status);
    }

    public static boolean isCommit(String status) {
        return COMMITTED.equals(status);
    }

    public static boolean isAbort(String status) {
        return ABORTED.equals(status);
    }

    public static boolean isReadOnly(String status) {
        return READONLY.equals(status);
    }

    public static boolean isHeuristic(String status) {
        return H_COMMIT.equals(status) ||
                H_HAZARD.equals(status) ||
                H_MIXED.equals(status) ||
                H_ROLLBACK.equals(status);
    }

    public static boolean isComplete(String status) {
        return COMMITTED.equals(status) ||
                ABORTED.equals(status);
    }

    public static boolean isActive(String status) {
        return ABORT_ONLY.equals(status) ||
                ABORTING.equals(status) ||
                COMMITTING.equals(status) ||
                PREPARING.equals(status) ||
                PREPARED.equals(status) ||
                RUNNING.equals(status);
    }

}
