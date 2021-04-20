/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
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
package io.narayana.sra.client;

import io.narayana.sra.annotation.Commit;
import io.narayana.sra.annotation.OnePhaseCommit;
import io.narayana.sra.annotation.Prepare;
import io.narayana.sra.annotation.Rollback;
import io.narayana.sra.annotation.Status;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Closeable;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A utility class for controlling the lifecycle of Long Running Actions (SRAs) but the prefered mechanism is to use
 * the annotation in the {@link io.narayana.sra.annotation} package
 */
@RequestScoped
public class SRAClient implements SRAClientAPI, Closeable {
    public static final String SRA_HTTP_HEADER = "Short-Running-Action";
    public static final String RTS_HTTP_RECOVERY_HEADER = "Short-Running-Action-Recovery";

    public static final String COORDINATOR_PATH_NAME = "rest-at-coordinator/tx/transaction-manager";
    public static final String RECOVERY_COORDINATOR_PATH_NAME = "tx/transaction-manager";

    public static final String COORDINATOR_PATH_PROP = "at.http.path";
    public static final String CORRDINATOR_HOST_PROP = "at.http.host";
    public static final String CORRDINATOR_PORT_PROP = "at.http.port";

    public static final String PARTICIPANT = "participant";
    public static final String COMMIT = "commit";
    public static final String PREPARE = "prepare";
    public static final String ROLLBACK = "rollback";
    public static final String STATUS = "participant";
    public static final String ONEPHASECOMMIT = "onephasecommit";

    private static final String TERMINATOR = "terminator";


    public static final String TIMELIMIT_PARAM_NAME = "TimeLimit";
    public static final String CLIENT_ID_PARAM_NAME = "ClientID";
    public static final String PARENT_SRA_PARAM_NAME = "ParentSRA";
    public static final long DEFAULT_TIMEOUT_MILLIS = 0L;


    private static final String startSRAUrl = "";
    private static final String getAllSRAsUrl = "/";
    private static final String getRecoveringSRAsUrl = "/recovery";
    private static final String getActiveSRAsUrl = "/active";

    private static final String isActiveUrlFormat = "/%s";
    private static final String isCompletedUrlFormat = "/completed/%s";
    private static final String isCompensatedUrlFormat = "/compensated/%s";

    private static final String confirmFormat = "/%s/close";
    private static final String compensateFormat = "/%s/cancel";
    private static final String leaveFormat = "/%s/remove";
    private static final String renewFormat = "/%s/renew";

    private static final String TX_STATUS_MEDIA_TYPE = "application/txstatus";
    private static final String POST_MEDIA_TYPE = "application/x-www-form-urlencoded";
    private static final String PLAIN_MEDIA_TYPE = "text/plain";
    private static final String TX_LIST_MEDIA_TYPE = "application/txlist";
    private static final String TX_STATUS_EXT_MEDIA_TYPE = "application/txstatusext+xml";

    public static final String STATUS_PROPERTY = "txstatus";

    public static final String TX_COMMITTED = "txstatus=TransactionCommitted";
    public static final String TX_ROLLEDBACK = "txstatus=TransactionRolledBack";
    public static final String TX_ROLLBACK_ONLY = "txstatus=TransactionRollbackOnly";

    private static final String MISSING_ANNOTATION_FORMAT =
            "Cannot enlist resource class %s: annotated with Transactional but is missing one or more of {@Commit. @Prepare, @Rollback, @Participant}";

    private static Boolean isTrace = Boolean.getBoolean("trace");

    private WebTarget target;
    private URI base;
    private Client client;
    private boolean isUseable;
    private boolean connectionInUse;
    private Map<URL, List<String>> responseDataMap;

    public SRAClient() throws URISyntaxException {
        this("http",
                System.getProperty(CORRDINATOR_HOST_PROP, "localhost"),
                Integer.getInteger(CORRDINATOR_PORT_PROP, 8080));
    }

    public SRAClient(String host, int port) throws URISyntaxException {
        this("http", host, port);
    }

    public SRAClient(String scheme, String host, int port) throws URISyntaxException {
        init(scheme, host, port);
    }

    public SRAClient(URL coordinatorUrl) throws MalformedURLException, URISyntaxException {
        init(coordinatorUrl);
    }

    private void init(URL coordinatorUrl) throws URISyntaxException {
        init(coordinatorUrl.getProtocol(), coordinatorUrl.getHost(), coordinatorUrl.getPort());
    }

    private void init(String scheme, String host, int port) throws URISyntaxException {
        if (client == null)
            client = ClientBuilder.newClient();

        base = new URI(scheme, null, host, port, "/" + COORDINATOR_PATH_NAME, null, null);
        target = client.target(base);

        isUseable = true;

        if (responseDataMap == null)
            postConstruct();
        else
            responseDataMap.clear();
    }


    public boolean isUseable() {
        return isUseable;
    }

    @PostConstruct
    public void postConstruct() {
        // an opportunity to consult any config - NB this will only get called if we are in a CDI enabled container
        responseDataMap = new HashMap<>();
    }

    @PreDestroy
    public void preDestroy() {
        isUseable = false;
    }

    public static URL lraToURL(String lraId) {
        return lraToURL(lraId, "Invalid SRA id");
    }

    public static URL lraToURL(String lraId, String message) {
        try {
            return new URL(lraId);
        } catch (MalformedURLException e) {
            throw new GenericSRAException(null, Response.Status.BAD_REQUEST.getStatusCode(),String.format("%s: %s", message, lraId), e);
        }
    }

    public static String encodeURL(URL lraId, String message) {
        try {
            return URLEncoder.encode(lraId.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new GenericSRAException(lraId, Response.Status.BAD_REQUEST.getStatusCode(), String.format("%s: %s", message, lraId), e);
        }
    }

    // extract the uid part from an SRA URL
    public static String getSRAId(String lraId) {
        return lraId == null ? null : lraId.replaceFirst(".*/([^/?]+).*", "$1");
    }

    public URL toURL(String lraId) throws InvalidSRAId {
        try {
            return new URL(lraId);
        } catch (MalformedURLException e) {
            throw new InvalidSRAId(lraId, "Invalid syntax", e);
        }
    }

    private WebTarget getTarget() {
        return target; // TODO can't share the target if a io.narayana.sra.demo.service makes multiple JAX-RS requests
    }

    /**
     * Update the clients notion of the current coordinator. Warning all further operations will be performed
     * on the SRA manager that created the passed in coordinator.
     *
     * @param coordinatorUrl the full url of an SRA
     */
    public void setCurrentSRA(URL coordinatorUrl) {
        try {
            init(coordinatorUrl);
        } catch (URISyntaxException e) {
            throw new GenericSRAException(coordinatorUrl, Response.Status.BAD_REQUEST.getStatusCode(), e.getMessage(), e);
        }
    }

    public URL startSRA(String clientID, Long timeout) throws GenericSRAException {
        return startSRA(null, clientID, timeout, TimeUnit.MILLISECONDS);
    }

    public URL startSRA(URL parentSRA, String clientID, Long timeout, TimeUnit unit) throws GenericSRAException {
        Response response = null;
        URL lra;

        if (clientID == null)
            clientID = "";

        if (timeout == null)
            timeout = 0L;
        else if (timeout < 0)
            throw new GenericSRAException(parentSRA, Response.Status.BAD_REQUEST.getStatusCode(), "Invalid timeout value: " + timeout, null);

        sraTrace(String.format("startSRA for client %s with parent %s", clientID, parentSRA), null);

        try {
            String encodedParentSRA = parentSRA == null ? "" : URLEncoder.encode(parentSRA.toString(), "UTF-8");

            aquireConnection();

            response = getTarget().path(startSRAUrl)
                    .queryParam(TIMELIMIT_PARAM_NAME, unit.toMillis(timeout))
                    .queryParam(CLIENT_ID_PARAM_NAME, clientID)
                    .queryParam(PARENT_SRA_PARAM_NAME, encodedParentSRA)
                    .request()
                    .post(Entity.entity(String.valueOf(unit.toMillis(timeout)), MediaType.APPLICATION_FORM_URLENCODED_TYPE));

            // validate the HTTP status code says an SRAInfo resource was created
            assertEquals(response, response.getStatus(), Response.Status.CREATED.getStatusCode(),
                    "SRA start returned an unexpected status code: %d versus %d");

            // validate that there is an SRAInfo response header holding the SRAInfo id
            Object sraObject = response.getHeaders().getFirst(SRA_HTTP_HEADER);

            if (sraObject == null) {
                sraObject = response.getHeaders().getFirst("Location");
            }

            assertNotNull(sraObject, "SRA is null");

            lra = new URL(URLDecoder.decode(sraObject.toString(), "UTF-8"));

            sraTrace("startSRA returned", lra);

            Current.push(lra);

        } catch (UnsupportedEncodingException | MalformedURLException e) {
            throw new GenericSRAException(null, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getMessage(), e);
        } catch (Exception e) {
            if (e.getCause() != null && ConnectException.class.equals(e.getCause().getClass()))
                throw new GenericSRAException(null, Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
                        "Cannont connect to an SRA coordinator: " + e.getCause().getMessage(), e);

            throw new GenericSRAException(null, Response.Status.SERVICE_UNAVAILABLE.getStatusCode(), e.getMessage(), e);
        } finally {
            releaseConnection(response);
        }

        // check that the lra is active
//        isActiveSRA(lra);

        return lra;
    }

    public String cancelSRA(URL lraId) throws GenericSRAException {
        return endSRA(lraId, false);
    }

    public String commitSRA(URL lraId) throws GenericSRAException {
        return endSRA(lraId, true);
    }

    /**
     *
     * @param lraUrl the URL of the SRA to join
     * @param timelimit how long the io.narayana.sra is prepared to wait for SRA completion
     * @param linkHeader io.narayana.sra protocol URLs in link header format (RFC 5988)
     *
     * @return a recovery URL for this enlistment
     *
     * @throws GenericSRAException if the SRA coordinator failed to enlist the io.narayana.sra
     */
    public String joinSRAWithLinkHeader(URL lraUrl, Long timelimit, String linkHeader) throws GenericSRAException {
        sraTrace(String.format("joining SRA with compensator link: %s", linkHeader), lraUrl);
        return enlistCompensator(lraUrl, timelimit, linkHeader);
    }

    public void joinSRA(URL lraId, Long timelimit, String compensatorUrl) throws GenericSRAException {
        sraTrace(String.format("joining SRA with compensator %s", compensatorUrl), lraId);

        enlistCompensator(lraId, timelimit, "",
                String.format("%s/compensate", compensatorUrl),
                String.format("%s/complete", compensatorUrl),
                String.format("%s/leave", compensatorUrl),
                String.format("%s/status", compensatorUrl));
    }

    public String joinSRA(URL lraId, Long timelimit,
                          String compensateUrl, String completeUrl, String leaveUrl, String statusUrl) throws GenericSRAException {
        return enlistCompensator(lraId, timelimit, "", compensateUrl, completeUrl, leaveUrl, statusUrl);
    }

    @Override
    public List<SRAInfo> getAllSRAs() throws GenericSRAException {
        return getSRAs(getAllSRAsUrl);
    }

    @Override
    public List<SRAInfo> getActiveSRAs() throws GenericSRAException {
        return getSRAs(getActiveSRAsUrl);
    }

    @Override
    public List<SRAInfo> getRecoveringSRAs() throws GenericSRAException {
        return getSRAs(getRecoveringSRAsUrl);
    }

    private List<SRAInfo> getSRAs(String getUrl) {
        Response response = null;

        try {
            aquireConnection();

            response = getTarget().path(getUrl).request().get();

            if (!response.hasEntity())
                throw new GenericSRAException(null, response.getStatus(), "missing entity body", null);

            List<SRAInfo> actions = new ArrayList<>();

            String lras = response.readEntity(String.class);

            JsonReader reader = Json.createReader(new StringReader(lras));
            JsonArray ja = reader.readArray();

            ja.forEach(jsonValue ->
                    actions.add(toSRAStatus(((JsonObject) jsonValue))));

            return actions;
        } finally {
            releaseConnection(response);
        }
    }

    private SRAInfo toSRAStatus(JsonObject jo) {
        try {
            return new SRAInfo(
                    jo.getString("lraId"),
                    jo.getString("clientId"),
                    jo.getBoolean("complete"),
                    jo.getBoolean("compensated"),
                    jo.getBoolean("recovering"),
                    jo.getBoolean("active"),
                    jo.getBoolean("topLevel"));
        } catch (Exception e) {
            System.out.printf("Error parsing json SRAInfo");

            return new SRAInfo(jo.getString("lraId"), jo.getString("lraId"), jo.getBoolean("complete"), jo.getBoolean("compensated"), jo.getBoolean("recovering"), jo.getBoolean("active"), jo.getBoolean("topLevel"));
        }
    }

    @Override
    public Boolean isActiveSRA(URL lraId) throws GenericSRAException {
        return getStatus(lraId, isActiveUrlFormat);
    }

    @Override
    public Boolean isCompensatedSRA(URL lraId) throws GenericSRAException {
        return getStatus(lraId, isCompensatedUrlFormat);
    }

    @Override
    public Boolean isCompletedSRA(URL lraId) throws GenericSRAException {
        return getStatus(lraId, isCompletedUrlFormat);
    }

    public Map<String, String> getTerminationUris(URL aaId, Class<?> compensatorClass, URI baseUri, boolean validate) {
        Map<String, String> paths = new HashMap<>();

        Annotation resourcePathAnnotation = compensatorClass.getAnnotation(Path.class);
        String resourcePath = resourcePathAnnotation == null ? "/" : ((Path) resourcePathAnnotation).value();
        final String uid = aaId.toString().replaceFirst(".*/([^/?]+).*", "$1");
        final String uriPrefix = String.format("%s:%s%s",
                baseUri.getScheme(), baseUri.getSchemeSpecificPart(), resourcePath.substring(1));

        final int[] validCnt = {0};

        Arrays.stream(compensatorClass.getMethods()).forEach(method -> {
            Path pathAnnotation = method.getAnnotation(Path.class);

            if (pathAnnotation != null) {
                if (checkMethod(paths, STATUS, pathAnnotation, method.getAnnotation(Status.class), uriPrefix, uid)) {
                    validCnt[0] += 1;
                }

                if (checkMethod(paths, PREPARE, pathAnnotation, method.getAnnotation(Prepare.class), uriPrefix, uid)) {
                    validCnt[0] += 1;
                }

                if (checkMethod(paths, COMMIT, pathAnnotation, method.getAnnotation(Commit.class), uriPrefix, uid)) {
                    validCnt[0] += 1;
                }

                if (checkMethod(paths, ROLLBACK, pathAnnotation, method.getAnnotation(Rollback.class), uriPrefix, uid)) {
                    validCnt[0] += 1;
                }

                checkMethod(paths, ONEPHASECOMMIT, pathAnnotation, method.getAnnotation(OnePhaseCommit.class), uriPrefix, uid);
            }
        });

        if (validate && validCnt[0] < 4) {
            if (!paths.containsKey(COMMIT) && !paths.containsKey(ONEPHASECOMMIT))
                throw new GenericSRAException(null, Response.Status.BAD_REQUEST.getStatusCode(),
                        String.format(MISSING_ANNOTATION_FORMAT, compensatorClass.getName()), null);
        }

        StringBuilder linkHeaderValue = new StringBuilder();

        paths.forEach((k, v) -> makeLink(linkHeaderValue, null, k, v));

        paths.put("Link", linkHeaderValue.toString());

        return paths;
    }

    private boolean checkMethod(Map<String, String> paths,
                                String rel,
                                Path pathAnnotation,
                                Annotation annotationClass,
                                String uriPrefix,
                                String aaId) {
        if (annotationClass == null)
            return false;

        // TODO remove hardcoded JAX-RS template param
        paths.put(rel, String.format("%s%s", uriPrefix, pathAnnotation.value().replaceAll("\\{txid}", aaId)));

        return true;
    }

    private Boolean getStatus(URL lraId, String statusFormat) {
        Response response = null;

        try {
            aquireConnection();

            response = getTarget().path("status").path(String.format(statusFormat, getSRAId(lraId.toString()))).request().get();

            return Boolean.valueOf(response.readEntity(String.class));
        } finally {
            releaseConnection(response);
        }
    }

    private StringBuilder makeLink(StringBuilder b, String uriPrefix, String key, String value) {

        String terminationUri = uriPrefix == null ? value : String.format("%s%s", uriPrefix, value);
        Link link =  Link.fromUri(terminationUri).title(key + " URI").rel(key).type(MediaType.TEXT_PLAIN).build();

        if (b.length() != 0)
            b.append(',');

        return b.append(link);
    }

    private String enlistCompensator(URL lraUrl, long timelimit, String uriPrefix,
                                     String compensateUrl, String completeUrl, String leaveUrl, String statusUrl ) {
        validateURL(completeUrl, false, "Invalid complete URL: %s");
        validateURL(compensateUrl, false, "Invalid compensate URL: %s");
        validateURL(leaveUrl, true, "Invalid status URL: %s");
        validateURL(statusUrl, false, "Invalid status URL: %s");

        Map<String, String> terminateURIs = new HashMap<>();

        terminateURIs.put(SRAClient.PREPARE, compensateUrl);
        terminateURIs.put(SRAClient.COMMIT, completeUrl);
        terminateURIs.put(SRAClient.ROLLBACK, leaveUrl);
        terminateURIs.put(SRAClient.STATUS, statusUrl);

        // register with the coordinator
        // put the lra id in an http header
        StringBuilder linkHeaderValue = new StringBuilder();

        terminateURIs.forEach((k, v) -> makeLink(linkHeaderValue, uriPrefix, k, v)); // or use Collectors.joining(",")

        return enlistCompensator(lraUrl, timelimit, linkHeaderValue.toString());
    }

    private String enlistCompensator(URL lraUrl, long timelimit, String linkHeader) {
        // register with the coordinator
        // put the lra id in an http header
        Response response = null;

        if (timelimit < 0)
            timelimit = 0L;

        try {
            response = getTarget().path(getSRAId(lraUrl.toString()))
                    .queryParam(TIMELIMIT_PARAM_NAME, timelimit)
                    .request()
                    .header("Link", linkHeader)
                    .header(SRA_HTTP_HEADER, lraUrl)
                    .post(Entity.entity(linkHeader, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

            if (response.getStatus() == Response.Status.PRECONDITION_FAILED.getStatusCode()) {
                throw new IllegalSRAStateException(lraUrl.toString(), "Too late to join with this SRA", null);
            } else if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
                String reason = response.readEntity(String.class);

                sraTrace(String.format("enlist in SRA failed (%d): %s",
                        response.getStatus(), response.readEntity(String.class)), lraUrl);

                throw new GenericSRAException(lraUrl, response.getStatus(),
                        "unable to register particiapnt: " + reason, null);
            } else if (!response.getHeaders().containsKey("Location")) {
                String reason = response.readEntity(String.class);

                throw new GenericSRAException(lraUrl, response.getStatus(),
                        "participant registration did not return a recovery URL: " + reason, null);
            }

            return response.getHeaders().getFirst("Location").toString();
        } finally {
            releaseConnection(response);
        }
    }

    private String endSRA(URL lra, boolean commit) throws GenericSRAException {
        String targetState = commit ? TX_COMMITTED : TX_ROLLEDBACK;

        Response response = null;

        sraTrace(String.format("%s SRA", commit ? "commit" : "rollback"), lra);

        try {
            response = getTarget().path(getSRAId(lra.toString())).path(TERMINATOR).request().put(Entity.entity(targetState, TX_STATUS_MEDIA_TYPE));

            assertEquals(response, Response.Status.OK.getStatusCode(),
                    response.getStatus(), "SRA finished with an unexpected status code: " + response.getStatus());

            return response.readEntity(String.class);
        } finally {

            releaseConnection(response);

            Current.pop(lra);

            URL nextSRA = Current.peek();

            if (nextSRA != null) {
                try {
                    init(nextSRA);
                } catch (URISyntaxException ignore) {
                    // the validity of the url was checked when we added it to Current
                }
            }
        }
    }

    private void validateURL(String url, boolean nullAllowed, String message) {
        if (url == null) {
            if (!nullAllowed)
                throw new GenericSRAException(null, Response.Status.NOT_ACCEPTABLE.getStatusCode(), String.format(message, "null value"), null);
        } else {
            try {
                new URL(url);
            } catch (MalformedURLException e) {
                throw new GenericSRAException(null, Response.Status.NOT_ACCEPTABLE.getStatusCode(), String.format(message, e.getMessage()), e);
            }
        }
    }

    private void assertNotNull(Object lra, String message) {
        if (lra == null)
            throw new GenericSRAException(null, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), message, null);
    }

    private void assertEquals(Response response, Object expected, Object actual, String messageFormat) {
        if (!actual.equals(expected))
            throw new GenericSRAException(null, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), String.format(messageFormat, expected, actual), null);
    }

    public String getUrl() {
        return base.toString();
    }

    public URL getCurrent() {
        return Current.peek();
    }

    private void sraTrace(String reason, URL lra) {
        if (isTrace)
            System.out.printf("SRAClient: %s: lra: %s%n", reason, lra == null ? "null" : lra);
    }

    public void close() {
        client.close();
        if (responseDataMap != null)
            responseDataMap.clear();
    }

    private void aquireConnection() {
        if (connectionInUse) {
            System.out.printf("SRAClient: trying to aquire an in use connection");

            throw new GenericSRAException(null, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    "SRAClient: trying to aquire an in use connection", null);
        }

        connectionInUse = true;
    }

    private void releaseConnection(Response response) {
        if (response != null)
            response.close();

        connectionInUse = false;
    }


/*    private void setResponseData(URL lraId, String responseData) {
        if (responseData == null || responseData.isEmpty())
            return;

        // responseData will be a json encoded list of strings
        ObjectMapper mapper = new ObjectMapper();

        try {
            List<String> compensatorData = Arrays.asList(mapper.readValue(responseData, String[].class));

            if (responseDataMap.containsKey(lraId))
                responseDataMap.get(lraId).addAll(compensatorData);
            else
                responseDataMap.put(lraId, compensatorData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    public List<String> getResponseData(URL lraId) {
        return responseDataMap.containsKey(lraId) ? responseDataMap.get(lraId) : Collections.emptyList();
    }
}
