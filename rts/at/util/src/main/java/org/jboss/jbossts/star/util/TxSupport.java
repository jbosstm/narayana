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

import java.io.*;
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
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.jboss.jbossts.star.provider.HttpResponseException;
import org.jboss.jbossts.star.util.media.txstatusext.CoordinatorElement;
import org.jboss.jbossts.star.util.media.txstatusext.TransactionManagerElement;
import org.jboss.jbossts.star.util.media.txstatusext.TransactionStatisticsElement;
import org.jboss.logging.Logger;

import javax.ws.rs.core.Link;
import java.security.KeyStore;
import javax.net.ssl.*;

/**
 * Various utilities for sending HTTP messages
 * @deprecated
 */
public class TxSupport
{
    protected static final Logger log = Logger.getLogger(TxSupport.class);

    /**
     * context root
     */
    public static final String TX_CONTEXT = System.getProperty("rest.tx.context.path", "/rest-tx");
    /**
     * Transaction Coordinator resource path
     */
    public static final String TX_PATH = "/tx/";
    /**
     * transaction-manager URI
     */
    public static final String TX_SEGMENT = "transaction-manager/";

    public static final int DEFAULT_READ_TIMEOUT = 20000;

    private static int PORT = 8080;
    private static String BIND_ADDRESS = System.getProperty("jboss.bind.address", "localhost");
    private static String BASE_URL = "http://" + BIND_ADDRESS + ':';
    private static final String DEF_TX_URL = BASE_URL + PORT + TX_CONTEXT + TX_PATH + TX_SEGMENT;

    public static String TXN_MGR_URL = DEF_TX_URL;
    public static final String URI_SEPARATOR = ",";

    private static Pattern NVP_PATTERN = Pattern.compile("\\b\\w+\\s*=\\s*.*"); // matches name=value pairs

    private Map<String, String> links = new HashMap<String, String>();
    private String participantLinkHeader = null;
    private int status = -1;
    private String body = null;
    private String contentType = null;
    private String txnMgr;
    private int readTimeout = DEFAULT_READ_TIMEOUT;
    private static HttpConnectionCreator creator = new HttpConnectionCreator() {
        @Override
        public HttpURLConnection open(URL url) throws IOException {
            return (HttpURLConnection) url.openConnection();
        }
    };

    public static void setTxnMgrUrl(String txnMgrUrl) {
        TXN_MGR_URL = txnMgrUrl;
    }
    public TxSupport(String txnMgr, int readTimeout) {
        this.txnMgr = txnMgr;
        this.readTimeout = readTimeout;
    }

    public TxSupport(String txnMgr) {
        this(txnMgr, DEFAULT_READ_TIMEOUT);
    }

    public TxSupport() {
        this(TXN_MGR_URL);
    }

    public TxSupport(int readTimeout) {
        this(TXN_MGR_URL, readTimeout);
    }

    public static void setHttpConnectionCreator(HttpConnectionCreator creator) {
        TxSupport.creator = creator;
    }

    public static void addLinkHeader(Response.ResponseBuilder response, UriInfo info, String title, String name,
                                     String ... pathComponents)
    {
        String basePath = info.getMatchedURIs().get(0);
        UriBuilder builder = info.getBaseUriBuilder();
        builder.path(basePath);

        for (String component : pathComponents)
            builder.path(component);

        String uri = builder.build().toString();

        setLinkHeader(response, title, name, uri, TxMediaType.PLAIN_MEDIA_TYPE);
    }

    public static void setLinkHeader(Response.ResponseBuilder builder, String title, String rel, String href,
                                     String type)
    {
        Link link = Link.fromUri(href).title(title).rel(rel).type(type).build();

        setLinkHeader(builder, link);
    }

    public static void setLinkHeader(Response.ResponseBuilder builder, Link link)
    {
        builder.header("Link", link);
    }

    public Collection<String> getTransactions() throws HttpResponseException {
        return getTransactions(TxMediaType.TX_LIST_MEDIA_TYPE);
    }

    public Collection<String> getTransactions(String mediaType) throws HttpResponseException {
        String content = httpRequest(new int[] {HttpURLConnection.HTTP_OK}, txnMgr, "GET", mediaType, null, links);
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
        String content = httpRequest(new int[] {HttpURLConnection.HTTP_OK}, txnMgr, "GET",
                TxMediaType.TX_LIST_MEDIA_TYPE, null, null);

        return content.length() == 0 ? 0 : content.split(URI_SEPARATOR).length;
    }

    // Transaction control methods
    public TxSupport startTx() throws HttpResponseException {
        httpRequest(new int[]{HttpURLConnection.HTTP_CREATED}, txnMgr, "POST", TxMediaType.POST_MEDIA_TYPE, "", links);
        links.put(TxLinkNames.TRANSACTION, links.get(TxLinkNames.LOCATION));
        return this;
    }
    public TxSupport startTx(long milliseconds) throws HttpResponseException {
        httpRequest(new int[] {HttpURLConnection.HTTP_CREATED}, txnMgr, "POST", TxMediaType.POST_MEDIA_TYPE,
                TxMediaType.TIMEOUT_PROPERTY + "=" + milliseconds, links);
        links.put(TxLinkNames.TRANSACTION, links.get(TxLinkNames.LOCATION));
        return this;
    }
    public String commitTx() throws HttpResponseException {
        return httpRequest(new int[] {HttpURLConnection.HTTP_OK}, links.get(TxLinkNames.TERMINATOR), "PUT",
                TxMediaType.TX_STATUS_MEDIA_TYPE, TxStatusMediaType.TX_COMMITTED, null);
    }
    public String rollbackTx() throws HttpResponseException {
        return httpRequest(new int[] {HttpURLConnection.HTTP_OK}, links.get(TxLinkNames.TERMINATOR), "PUT",
                TxMediaType.TX_STATUS_MEDIA_TYPE, TxStatusMediaType.TX_ROLLEDBACK, null);
    }
    public String markTxRollbackOnly() throws HttpResponseException {
        return httpRequest(new int[] {HttpURLConnection.HTTP_OK}, links.get(TxLinkNames.TERMINATOR), "PUT",
                TxMediaType.TX_STATUS_MEDIA_TYPE, TxStatusMediaType.TX_ROLLBACK_ONLY, null);
    }

    /**
     * Get the status of the current transaction
     * @return the transaction status expressed in the default media type (@see TxMediaType#TX_STATUS_MEDIA_TYPE)
     * @throws HttpResponseException
     */
    public String txStatus() throws HttpResponseException {
        return txStatus(TxMediaType.TX_STATUS_MEDIA_TYPE, null);
    }
    public String txStatus(String mediaType) throws HttpResponseException {
        return txStatus(mediaType, links);
    }
    private String txStatus(String mediaType, Map<String, String> linkHeaders) throws HttpResponseException {
        return httpRequest(new int[] {HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_UNSUPPORTED_TYPE},
                links.get(TxLinkNames.TRANSACTION), "GET", mediaType, null, linkHeaders);
    }
    public String getTxnUri() {
        return links.get(TxLinkNames.TRANSACTION);
    }
    public String getTerminatorURI() {
        return links.get(TxLinkNames.TERMINATOR);
    }
    public String getDurableParticipantEnlistmentURI() {
        return links.get(TxLinkNames.PARTICIPANT);
    }
    public String getVolatileParticipantEnlistmentURI() {
        return links.get(TxLinkNames.VOLATILE_PARTICIPANT);
    }

    public String getBody() {
        return body;
    }
    public int getStatus() {
        return status;
    }
    public String getContentType() {
        return contentType;
    }

    public void refreshTransactionHeaders(Map<String, String> linkHeaders) throws HttpResponseException {
        httpRequest(new int[] {HttpURLConnection.HTTP_OK}, links.get(TxLinkNames.TRANSACTION), "HEAD",
                TxMediaType.TX_STATUS_MEDIA_TYPE, null, linkHeaders);
    }

    public String enlistTestResource(String pUrl, boolean vParticipant) throws HttpResponseException {
        String content = links.get(TxLinkNames.PARTICIPANT);

        if (vParticipant)
            content += "," + links.get(TxLinkNames.VOLATILE_PARTICIPANT);

        return httpRequest(new int[] {HttpURLConnection.HTTP_OK}, pUrl, "POST", TxMediaType.POST_MEDIA_TYPE,
                content, null);
    }

    // return a map of link name to link uri
    public static Map<String, String> decodeLinkHeader(String linkHeader) {
        int i;
        Map<String, String> decodedLinks = new HashMap<String, String>();

        if (linkHeader == null || (i = linkHeader.indexOf('<')) == -1)
            return decodedLinks;

        return extractLinkHeaders(linkHeader.substring(i), decodedLinks);
    }

    public StringBuilder addLink(StringBuilder linkHeader, String linkName, StringBuilder hrefPrefix, boolean first) {
        if (!first)
            linkHeader.append(',');

        linkHeader.append("<").append(hrefPrefix).append(linkName).append(">; rel=\"").append(linkName).append("\"");

        return linkHeader;
    }

    public StringBuilder addLink2(StringBuilder linkHeader, String linkName, String href, boolean first) {
        if (!first)
            linkHeader.append(',');

        linkHeader.append("<").append(href).append(">; rel=\"").append(linkName).append("\"");

        return linkHeader;
    }

    /**
     * Constructs the participant-resource and participant-terminator URIs for participants in the format:
     * "baseURI/{uid1}/{uid2}/participant" and "baseURI/{uid1}/{uid2}/terminator" and optionally
     * "baseURI/{uid1}/{uid2}/volatile"
     *
     * If uid1 is null then the "{uid1}/" is not included and similarly if uid2 is null.
     *
     * @param baseURI the (full) uri prefix
     * @param vParticipant if true also construct a link header for participation in the volatile protocol
     * @param uid1 a string which together with baseURI and possibly uid2 produce a unique id
     * @param uid2 a string which together with baseURI and possibly uid1 produce a unique id
     * @return link header value
     */
    public String makeTwoPhaseAwareParticipantLinkHeader(
            String baseURI, boolean vParticipant, String uid1, String uid2) {
        StringBuilder resourcePrefix = new StringBuilder(baseURI);
        StringBuilder linkHeader = new StringBuilder(); // "Link:"

        if (uid1 != null)
            resourcePrefix.append('/').append(uid1);
        if (uid2 != null)
            resourcePrefix.append('/').append(uid2);

        resourcePrefix.append('/');

        addLink(linkHeader, TxLinkNames.PARTICIPANT_RESOURCE, resourcePrefix, true);
        addLink(linkHeader, TxLinkNames.PARTICIPANT_TERMINATOR, resourcePrefix, false);

        if (vParticipant)
            addLink(linkHeader, TxLinkNames.VOLATILE_PARTICIPANT, resourcePrefix, false);

        participantLinkHeader = linkHeader.toString();

        return participantLinkHeader;
    }

    public String makeTwoPhaseAwareParticipantLinkHeader(String baseURI, String uid1, String uid2) {
        return makeTwoPhaseAwareParticipantLinkHeader(baseURI, false, uid1, uid2);
    }

    public String makeTwoPhaseAwareParticipantLinkHeader(String participantHref, String terminatorHref) {
        StringBuilder linkHeader = new StringBuilder();
        linkHeader.append("<").append(participantHref).append(">; rel=\"")
                .append(TxLinkNames.PARTICIPANT_RESOURCE).append("\"");
        linkHeader.append(',');
        linkHeader.append("<").append(terminatorHref).append(">; rel=\"")
                .append(TxLinkNames.PARTICIPANT_TERMINATOR).append("\"");

        participantLinkHeader = linkHeader.toString();

        return participantLinkHeader;
    }

    /**
     * Constructs the participant-resource and participant-terminator URIs for participants in the format:
     * "baseURI/{uid1}/{uid2}/participant" and "baseURI/{uid1}/{uid2}/terminate"
     * If uid1 is null then the "{uid1}/" is not included and similarly if uid2 is null.
     * @param baseURI the (full) uri prefix
     * @param vParticipant if true also construct a link header for participation in the volatile protocol
     * @param uid1 a string which together with baseURI and possibly uid2 produce a unique id
     * @param uid2 a string which together with baseURI and possibly uid1 produce a unique id
     * @param commitOnePhase if true generate a commit-one-phase link header
     * @return link header value
     */
    public String makeTwoPhaseUnAwareParticipantLinkHeader(
            String baseURI, boolean vParticipant, String uid1, String uid2, boolean commitOnePhase) {
        StringBuilder resourcePrefix = new StringBuilder(baseURI);
        StringBuilder linkHeader = new StringBuilder();  // "Link:"

        if (uid1 != null)
            resourcePrefix.append('/').append(uid1);
        if (uid2 != null)
            resourcePrefix.append('/').append(uid2);

        resourcePrefix.append('/');

        addLink(linkHeader, TxLinkNames.PARTICIPANT_RESOURCE, resourcePrefix, true);
        addLink(linkHeader, TxLinkNames.PARTICIPANT_PREPARE, resourcePrefix, false);
        addLink(linkHeader, TxLinkNames.PARTICIPANT_COMMIT, resourcePrefix, false);
        addLink(linkHeader, TxLinkNames.PARTICIPANT_ROLLBACK, resourcePrefix, false);

        if (commitOnePhase)
            addLink(linkHeader, TxLinkNames.PARTICIPANT_COMMIT_ONE_PHASE, resourcePrefix, false);

        if (vParticipant)
            addLink(linkHeader, TxLinkNames.VOLATILE_PARTICIPANT, resourcePrefix, false);

        participantLinkHeader = linkHeader.toString();

        return participantLinkHeader;
    }

    public String makeTwoPhaseUnAwareParticipantLinkHeader(
            String participantHref, String prepareHref, String commitHref, String rollbackHref,
            String vParticipantHref) {
        StringBuilder linkHeader = new StringBuilder();
        linkHeader.append("<").append(participantHref).append(">; rel=\"").append(TxLinkNames.PARTICIPANT_RESOURCE).append("\"");
        linkHeader.append(',');
        linkHeader.append("<").append(prepareHref).append(">; rel=\"").append(TxLinkNames.PARTICIPANT_PREPARE).append("\"");
        linkHeader.append(',');
        linkHeader.append("<").append(commitHref).append(">; rel=\"").append(TxLinkNames.PARTICIPANT_COMMIT).append("\"");
        linkHeader.append(',');
        linkHeader.append("<").append(rollbackHref).append(">; rel=\"").append(TxLinkNames.PARTICIPANT_ROLLBACK).append("\"");

        if (vParticipantHref != null) {
            linkHeader.append(',');
            linkHeader.append("<").append(vParticipantHref).append(">; rel=\"").append(TxLinkNames.VOLATILE_PARTICIPANT).append("\"");
        }

        participantLinkHeader = linkHeader.toString();

        return participantLinkHeader;
    }

    public String makeTwoPhaseParticipantLinkHeader(HashMap<String, String> links) {
        if (!links.containsKey(TxLinkNames.PARTICIPANT_RESOURCE))
            return null;

        StringBuilder hdr = new StringBuilder();

        addLink2(hdr, TxLinkNames.PARTICIPANT_RESOURCE, links.get(TxLinkNames.PARTICIPANT_RESOURCE), true);

        if (links.containsKey(TxLinkNames.PARTICIPANT_TERMINATOR))
            addLink2(hdr, TxLinkNames.PARTICIPANT_TERMINATOR, links.get(TxLinkNames.PARTICIPANT_TERMINATOR), false);

        if (links.containsKey(TxLinkNames.PARTICIPANT_COMMIT))
            addLink2(hdr, TxLinkNames.PARTICIPANT_COMMIT, links.get(TxLinkNames.PARTICIPANT_COMMIT), false);

        if (links.containsKey(TxLinkNames.PARTICIPANT_PREPARE))
            addLink2(hdr, TxLinkNames.PARTICIPANT_PREPARE, links.get(TxLinkNames.PARTICIPANT_PREPARE), false);

        if (links.containsKey(TxLinkNames.PARTICIPANT_ROLLBACK))
            addLink2(hdr, TxLinkNames.PARTICIPANT_ROLLBACK, links.get(TxLinkNames.PARTICIPANT_ROLLBACK), false);

        if (links.containsKey(TxLinkNames.PARTICIPANT_COMMIT_ONE_PHASE))
            addLink2(hdr, TxLinkNames.PARTICIPANT_COMMIT_ONE_PHASE,
                    links.get(TxLinkNames.PARTICIPANT_COMMIT_ONE_PHASE), false);

        participantLinkHeader = hdr.toString();

        return participantLinkHeader;
    }

    public String enlistParticipant(String participantLinkHeader) {
        return enlistParticipant(links.get(TxLinkNames.PARTICIPANT), participantLinkHeader);
    }

    /**
     * @param enlistUri the URI for enlisting participants with a transaction manager
     * @param participantLinkHeader link header for the participant to identify itself to the coordinator
     * @return participant recovery URI
     */
    public String enlistParticipant(String enlistUri, String participantLinkHeader) {
        Map<String, String> reqHeaders = new HashMap<String, String>();
        reqHeaders.put("Link", participantLinkHeader);
        httpRequest(new int[]{HttpURLConnection.HTTP_CREATED}, enlistUri, "POST", TxMediaType.POST_MEDIA_TYPE, null,
                links, reqHeaders);

        links.put(TxLinkNames.PARTICIPANT_RECOVERY, links.get(TxLinkNames.LOCATION));

        return links.get(TxLinkNames.PARTICIPANT_RECOVERY);
    }

    public void enlistVolatileParticipant(String enlistUri, String participantLinkHeader) {
        Map<String, String> reqHeaders = new HashMap<String, String>();
        reqHeaders.put("Link", participantLinkHeader);
        httpRequest(new int[]{HttpURLConnection.HTTP_OK}, enlistUri, "PUT", null, null,
                links, reqHeaders);
    }

    public String httpRequest(int[] expect, String url, String method, String mediaType) throws HttpResponseException {
        return httpRequest(expect, url, method, mediaType, null, null, null);
    }

    public String httpRequest(int[] expect, String url, String method, String mediaType, String content)
            throws HttpResponseException {
        return httpRequest(expect, url, method, mediaType, content, null, null);
    }

    public String httpRequest(int[] expect, String url, String method, String mediaType, String content,
                              Map<String, String> linkHeaders) throws HttpResponseException {
        return httpRequest(expect, url, method, mediaType, content, linkHeaders, null);
    }

    public String httpRequest(int[] expect, String url, String method, String mediaType, String content,
                              Map<String, String> linkHeaders, Map<String, String> reqHeaders)
            throws HttpResponseException {
        HttpURLConnection connection = null;

        try {
            connection = openConnection(null, url, method, mediaType, content, reqHeaders);
            connection.setReadTimeout(readTimeout);
            status = connection.getResponseCode();
            contentType = connection.getContentType();

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
            if (log.isTraceEnabled())
                log.tracef("httpRequest: io error: %s%n", e.getMessage());
            throw new HttpResponseException(e, "", expect, HttpURLConnection.HTTP_UNAVAILABLE);
        } finally {
            if (connection != null)
                connection.disconnect();
        }
    }

    private static Map<String, String> extractLinkHeaders(String header, Map<String, String> links) {
        if (header != null) {
            for (String linkHeader : header.split(",")) {
                Link lnk = Link.valueOf(linkHeader);

                links.put(lnk.getRel(), lnk.getUri().toString());
            }
        }

        return links;
    }

    private void extractLinkHeaders(HttpURLConnection connection, Map<String, String> links) {
        Collection<String> linkHeaders = connection.getHeaderFields().get("Link");

        if (linkHeaders == null)
            linkHeaders = connection.getHeaderFields().get("link");

        if (linkHeaders != null) {
            for (String header : linkHeaders)
                extractLinkHeaders(header, links);
        }
    }

    private void addLocationHeader(HttpURLConnection connection, Map<String, String> links) {
        try {
            if (connection.getResponseCode() == HttpURLConnection.HTTP_CREATED)
                links.put("location", connection.getHeaderField("location"));
        } catch (IOException e) {
        }
    }

    private HttpURLConnection openConnection(HttpURLConnection connection, String url, String method,
                                             String contentType, String content, Map<String, String> reqHeaders)
            throws IOException {
        if (connection != null)
            connection.disconnect();

        connection = creator.open(new URL(url));

        connection.setRequestMethod(method);

        if (contentType != null) {
            if ("GET".equals(method))
                connection.setRequestProperty("Accept", contentType);
            else
                connection.setRequestProperty("Content-Type", contentType);
        }

        if (reqHeaders != null) {
            /*
             * NOTE: HTTP requires all request properties which can legally have multiple instances
             * with the same key to use a comma-separated list syntax which enables multiple
             * properties to be appended into a single property.
             *
             * In particular this applies to the Link header defined in rfc5988 (web linking)
             */
            for (Map.Entry<String, String> entry : reqHeaders.entrySet())
                connection.setRequestProperty(entry.getKey(), entry.getValue());
        }

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
                return Integer.parseInt(v);
            } catch (NumberFormatException e) {
                //
            }

        return defValue;
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

    public static String extractUri(UriInfo info, String ... paths) {
        // careful with extracting the uri from a UriInfo since it casues issues with emma
        // also info.getBaseUri() + info.getPath() fail running the wildfly testsuite
        return getUri(info, info.getPathSegments().size(), paths).toASCIIString();
    }

    public static String buildURI(UriBuilder builder, String ... pathComponents)
    {
        for (String component : pathComponents)
            builder.path(component);

        return builder.build().toString();
    }

    public static TxStatus toTxStatus(String statusContent) {
        String status = getStringValue(statusContent, TxStatusMediaType.STATUS_PROPERTY);
        return TxStatus.fromStatus(status);
    }
    public static String getStatus(String statusContent) {
        return getStringValue(statusContent, TxStatusMediaType.STATUS_PROPERTY);
    }

    public static String toContent(String property, String status) {
        return new StringBuilder(property).append('=').append(status).toString();
    }

    public static String toStatusContent(String status) {
        return toContent(TxStatusMediaType.STATUS_PROPERTY, status);
    }

    public String getLink(String linkName) {
        return links.get(linkName);
    }

    /**
     * Obtain statistical information such as the number of transactions that have committed and aborted.
     * @return transaction statistics
     * @throws JAXBException if JAXB cannot convert an XML representation of the statistics into a JAXB object
     */
    public TransactionStatisticsElement getTransactionStatistics() throws JAXBException {
        // performing a get on the transaction-manager MAY return a link for obtaining transaction statistic
        getTransactions();

        String statisticsHref = getLink(TxLinkNames.STATISTICS);

        if (statisticsHref == null) // NOTE: statistics are optional
            return null;

        // GET the statistics
        String txStats = httpRequest(new int[] {HttpURLConnection.HTTP_OK}, statisticsHref, "GET",
                TxMediaType.TX_STATUS_EXT_MEDIA_TYPE);

        if (log.isTraceEnabled())
            log.tracef("Unmarshalling TransactionStatisticsElement\n%s", txStats);

        JAXBContext jc = JAXBContext.newInstance(TransactionStatisticsElement.class.getPackage().getName() );
        Unmarshaller u = jc.createUnmarshaller();
        Object o = u.unmarshal( new StreamSource( new StringReader(txStats)));

        if (o instanceof TransactionStatisticsElement)
            return (TransactionStatisticsElement)o;

        return (TransactionStatisticsElement)((JAXBElement)o).getValue();
    }

    public CoordinatorElement getTransactionInfo() throws JAXBException {
        if (!links.containsKey(TxLinkNames.TRANSACTION))
            throw new IllegalStateException("Not transaction has been started");

        txStatus(TxMediaType.TX_STATUS_EXT_MEDIA_TYPE);

        if (status == HttpURLConnection.HTTP_UNSUPPORTED_TYPE )
            return null;

        if (log.isTraceEnabled())
            log.tracef("Unmarshalling CoordinatorElement\n%s", getBody());

        JAXBContext jc = JAXBContext.newInstance(CoordinatorElement.class.getPackage().getName() );


        Unmarshaller u = jc.createUnmarshaller();
        Object o = u.unmarshal( new StreamSource( new StringReader(getBody())));

        return (CoordinatorElement)((JAXBElement)o).getValue();
    }

    public CoordinatorElement getTransactionInfo(String uri) throws JAXBException {
        links.put(TxLinkNames.TRANSACTION, uri);

        return getTransactionInfo();
    }


    public TransactionManagerElement getTransactionManagerInfo() throws JAXBException {
        // GET the extended transaction-manager info
        String xml = httpRequest(new int[]{HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_UNSUPPORTED_TYPE},
                txnMgr, "GET", TxMediaType.TX_STATUS_EXT_MEDIA_TYPE);

        if (status == HttpURLConnection.HTTP_UNSUPPORTED_TYPE )
            return null;

        if (log.isTraceEnabled())
            log.tracef("Unmarshalling TransactionManagerElement%n%s", xml);

        JAXBContext jc = JAXBContext.newInstance(TransactionManagerElement.class.getPackage().getName() );


        Unmarshaller u = jc.createUnmarshaller();
        Object o = u.unmarshal( new StreamSource( new StringReader(xml)));

        if (o instanceof TransactionManagerElement)
            return (TransactionManagerElement)o;

        return (TransactionManagerElement)((JAXBElement)o).getValue();
    }

    private static SSLContext getSSLContext() throws Exception {
        String trustStoreFile = System.getProperty("javax.net.ssl.trustStore");
        String trustStorePswd = System.getProperty("javax.net.ssl.trustStorePassword");

        // Load the key store: change store type if needed
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        FileInputStream fis = new FileInputStream(trustStoreFile);

        try {
            ks.load(fis, trustStorePswd.toCharArray());
        } finally {
            if (fis != null) { fis.close(); }
        }

        // Get the default Key Manager
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, trustStorePswd.toCharArray());

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);

        return sslContext;
   }
}
