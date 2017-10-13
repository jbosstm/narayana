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
package io.narayana.lra.client;

import io.narayana.lra.annotation.Compensate;
import io.narayana.lra.annotation.CompensatorStatus;
import io.narayana.lra.annotation.Complete;
import io.narayana.lra.annotation.Forget;
import io.narayana.lra.annotation.Leave;
import io.narayana.lra.annotation.Status;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import javax.enterprise.context.RequestScoped;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.narayana.lra.annotation.TimeLimit;

import java.io.Closeable;
import java.io.IOException;
import java.io.StringReader;

import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * A utility class for controlling the lifecycle of Long Running Actions (LRAs) but the prefered mechanism is to use
 * the annotation in the {@link io.narayana.lra.annotation} package
 */
@RequestScoped
public class LRAClient implements LRAClientAPI, Closeable {
    public static final String LRA_HTTP_HEADER = "Long-Running-Action";
    public static final String LRA_HTTP_RECOVERY_HEADER = "Long-Running-Action-Recovery";

    public static final String COORDINATOR_PATH_NAME = "lra-coordinator";
    public static final String RECOVERY_COORDINATOR_PATH_NAME = "lra-recovery-coordinator";

    public static final String CORRDINATOR_HOST_PROP = "lra.http.host";
    public static final String CORRDINATOR_PORT_PROP = "lra.http.port";

    public static final String COMPLETE = "complete";
    public static final String COMPENSATE = "compensate";
    public static final String STATUS = "status";
    public static final String LEAVE = "leave";
    public static final String FORGET = "forget";

    public static final String TIMELIMIT_PARAM_NAME = "TimeLimit";
    public static final String CLIENT_ID_PARAM_NAME = "ClientID";
    public static final String PARENT_LRA_PARAM_NAME = "ParentLRA";
    public static final String STATUS_PARAM_NAME = "Status";

    public static final long DEFAULT_TIMEOUT_MILLIS = 0L;

    private static final String startLRAUrl = "/start";///?ClientId=abc&timeout=300000";
    private static final String getAllLRAsUrl = "/";
    private static final String getRecoveringLRAsUrl = "?status=Compensating";
    private static final String getActiveLRAsUrl = "?status=";

    private static final String confirmFormat = "/%s/close";
    private static final String compensateFormat = "/%s/cancel";
    private static final String leaveFormat = "/%s/remove";
    private static final String renewFormat = "/%s/renew";


    private static final String MISSING_ANNOTATION_FORMAT =
            "Cannot enlist resource class %s: annotated with LRA but is missing one or more of {@Complete. @Compensate, @Status}";

    private static Boolean isTrace = Boolean.getBoolean("trace");

    private WebTarget target;
    private URI base;
    private Client client;
    private boolean isUseable;
    private boolean connectionInUse;
    private Map<URL, List<String>> responseDataMap;

    public LRAClient() throws URISyntaxException {
        this("http",
                System.getProperty(CORRDINATOR_HOST_PROP, "localhost"),
                Integer.getInteger(CORRDINATOR_PORT_PROP, 8080));
    }

    public LRAClient(String host, int port) throws URISyntaxException {
        this("http", host, port);
    }

    public LRAClient(String scheme, String host, int port) throws URISyntaxException {
        init(scheme, host, port);
    }

    public LRAClient(URL coordinatorUrl) throws MalformedURLException, URISyntaxException {
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
        return lraToURL(lraId, "Invalid LRA id");
    }

    public static URL lraToURL(String lraId, String message) {
        try {
            return new URL(lraId);
        } catch (MalformedURLException e) {
            throw new GenericLRAException(null, Response.Status.BAD_REQUEST.getStatusCode(),
                    String.format("%s: %s", message, lraId), e);
        }
    }

    public static String encodeURL(URL lraId, String message) {
        try {
            return URLEncoder.encode(lraId.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new GenericLRAException(lraId, Response.Status.BAD_REQUEST.getStatusCode(), String.format("%s: %s", message, lraId), e);
        }
    }

    // extract the uid part from an LRA URL
    public static String getLRAId(String lraId) {
        return lraId == null ? null : lraId.replaceFirst(".*/([^/?]+).*", "$1");
    }

    public URL toURL(String lraId) throws InvalidLRAId {
        try {
            return new URL(lraId);
        } catch (MalformedURLException e) {
            throw new InvalidLRAId(lraId, "Invalid syntax", e);
        }
    }

    private WebTarget getTarget() {
//        return target; // TODO can't share the target if a service makes multiple JAX-RS requests
        client.close(); // hacking
        client = ClientBuilder.newClient();
        return client.target(base);
    }

    /**
     * Update the clients notion of the current coordinator. Warning all further operations will be performed
     * on the LRA manager that created the passed in coordinator.
     *
     * @param coordinatorUrl the full url of an LRA
     */
    public void setCurrentLRA(URL coordinatorUrl) {
        try {
            init(coordinatorUrl);
        } catch (URISyntaxException e) {
            throw new GenericLRAException(coordinatorUrl, Response.Status.BAD_REQUEST.getStatusCode(), e.getMessage(), e);
        }
    }

    public URL startLRA(String clientID) throws GenericLRAException {
        return startLRA(clientID, 0L);
    }

    public URL startLRA(String clientID, Long timeout) throws GenericLRAException {
        return startLRA(null, clientID, timeout, TimeUnit.SECONDS);
    }

    @Override
    public URL startLRA(URL parentLRA, String clientID, Long timeout, TimeUnit unit) throws GenericLRAException {
        Response response = null;
        URL lra;

        if (clientID == null)
            clientID = "";

        if (timeout == null)
            timeout = 0L;
        else if (timeout < 0)
            throw new GenericLRAException(parentLRA, Response.Status.BAD_REQUEST.getStatusCode(),
                    "Invalid timeout value: " + timeout, null);

        lraTrace(String.format("startLRA for client %s with parent %s", clientID, parentLRA), null);

        try {
            String encodedParentLRA = parentLRA == null ? "" : URLEncoder.encode(parentLRA.toString(), "UTF-8");

            aquireConnection();

            response = getTarget().path(startLRAUrl)
                    .queryParam(TIMELIMIT_PARAM_NAME, unit.toMillis(timeout))
                    .queryParam(CLIENT_ID_PARAM_NAME, clientID)
                    .queryParam(PARENT_LRA_PARAM_NAME, encodedParentLRA)
                    .request()
                    .post(Entity.text(""));

            // validate the HTTP status code says an LRAStatus resource was created
            checkStatus(null, response, "LRA start returned an unexpected status code: %d",
                    Response.Status.CREATED);

            // validate that there is an LRAStatus response header holding the LRAStatus id
            Object lraObject = response.getHeaders().getFirst(LRA_HTTP_HEADER);

            assertNotNull(lraObject, "LRA is null");

            lra = new URL(URLDecoder.decode(lraObject.toString(), "UTF-8"));

            lraTrace("startLRA returned", lra);

            Current.push(lra);

        } catch (UnsupportedEncodingException | MalformedURLException e) {
            throw new GenericLRAException(null, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getMessage(), e);
        } catch (Exception e) {
            if (e.getCause() != null && ConnectException.class.equals(e.getCause().getClass()))
                throw new GenericLRAException(null, Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
                        "Cannont connect to an LRA coordinator: " + e.getCause().getMessage(), e);

            throw new GenericLRAException(null, Response.Status.SERVICE_UNAVAILABLE.getStatusCode(), e.getMessage(), e);
        } finally {
            releaseConnection(response);
        }

        // check that the lra is active
//        isActiveLRA(lra);

        return lra;
    }

    @Override
    public void renewTimeLimit(URL lraId, long limit, TimeUnit unit) {
        Response response = null;

        lraTrace(String.format("renew time limit to %s s of LRA", unit.toSeconds(limit)), lraId);

        try {
            aquireConnection();

            response = getTarget().path(String.format(renewFormat, getLRAId(lraId.toString())))
                    .queryParam(TIMELIMIT_PARAM_NAME, unit.toMillis(limit))
                    .request()
                    .header(LRA_HTTP_HEADER, lraId)
                    .put(Entity.text(""));

            if (Response.Status.OK.getStatusCode() != response.getStatus())
                throw new GenericLRAException(lraId, response.getStatus(), "", null);
        } finally {
            releaseConnection(response);
        }
    }

    @Override
    public String cancelLRA(URL lraId) throws GenericLRAException {
        return endLRA(lraId, false);
    }

    @Override
    public String closeLRA(URL lraId) throws GenericLRAException {
        return endLRA(lraId, true);
    }

    /**
     *
     * @param lraUrl the URL of the LRA to join
     * @param timelimit how long the participant is prepared to wait for LRA completion
     * @param linkHeader participant protocol URLs in link header format (RFC 5988)
     *
     * @param compensatorData
     * @return a recovery URL for this enlistment
     *
     * @throws GenericLRAException if the LRA coordinator failed to enlist the participant
     */
    public String joinLRAWithLinkHeader(URL lraUrl, Long timelimit, String linkHeader,
                                        String compensatorData) throws GenericLRAException {
        lraTrace(String.format("joining LRA with participant link: %s", linkHeader), lraUrl);
        return enlistCompensator(lraUrl, timelimit, linkHeader, compensatorData);
    }

    @Override
    public String joinLRA(URL lraId, Long timelimit, String compensatorUrl,
                          String compensatorData) throws GenericLRAException {
        lraTrace(String.format("joining LRA with participant %s", compensatorUrl), lraId);

        return enlistCompensator(lraId, timelimit, "",
                String.format("%s/compensate", compensatorUrl),
                String.format("%s/complete", compensatorUrl),
                String.format("%s/forget",compensatorUrl),
                String.format("%s/leave", compensatorUrl),
                String.format("%s/status", compensatorUrl),
                compensatorData);
    }

    private String toExternalForm(URL url) {
        return url == null ? null : url.toExternalForm();
    }

    @Override
    public String joinLRA(URL lraId, Long timelimit,
                          URL compensateUrl, URL completeUrl, URL forgetUrl, URL leaveUrl, URL statusUrl,
                          String compensatorData) throws GenericLRAException {
        return enlistCompensator(lraId, timelimit, "",
                toExternalForm(compensateUrl), toExternalForm(completeUrl),
                toExternalForm(forgetUrl), toExternalForm(leaveUrl), toExternalForm(statusUrl),
                compensatorData);
    }

    public String joinLRA(URL lraId, Class<?> resourceClass, URI baseUri,
                          String compensatorData) throws GenericLRAException {
        Map<String, String> terminateURIs = getTerminationUris(resourceClass, baseUri);
        String timeLimitStr = terminateURIs.get(LRAClient.TIMELIMIT_PARAM_NAME);
        long timeLimit = timeLimitStr == null ? LRAClient.DEFAULT_TIMEOUT_MILLIS : Long.valueOf(timeLimitStr);

        if (terminateURIs.containsKey("Link"))
            return joinLRAWithLinkHeader(lraId, timeLimit, terminateURIs.get("Link"), compensatorData);

        return null;
    }

    @Override
    public URL updateCompensator(URL recoveryUrl, URL compensateUrl, URL completeUrl,
                                 URL forgetUrl, URL statusUrl,
                                 String compensatorData) throws GenericLRAException {
        return null;// TODO
    }

    @Override
    public void leaveLRA(URL lraId, String compensatorUrl) throws GenericLRAException {
        Response response = null;

        lraTrace("leaving LRA", lraId);

        try {
            aquireConnection();

            response = getTarget().path(String.format(leaveFormat, getLRAId(lraId.toString())))
                    .request()
                    .header(LRA_HTTP_HEADER, lraId)
                    .put(Entity.entity(compensatorUrl, MediaType.TEXT_PLAIN));

            if (Response.Status.OK.getStatusCode() != response.getStatus())
                throw new GenericLRAException(lraId, response.getStatus(), "", null);
        } finally {
            releaseConnection(response);
        }
    }

    @Override
    public List<LRAStatus> getAllLRAs() throws GenericLRAException {
        return getLRAs(null, null);
    }

    @Override
    public List<LRAStatus> getActiveLRAs() throws GenericLRAException {
        return getLRAs("status", "");
    }

    @Override
    public List<LRAStatus> getRecoveringLRAs() throws GenericLRAException {
        return getLRAs("status", CompensatorStatus.Compensating.name());
    }

    private List<LRAStatus> getLRAs(String queryName, String queryValue) {
        Response response = null;

        try {
            aquireConnection();

            if (queryName == null)
                response = getTarget().request().get();
            else
                response = getTarget().queryParam(queryName, queryValue).request().get();

            if (!response.hasEntity())
                throw new GenericLRAException(null, response.getStatus(), "missing entity body", null);

            List<LRAStatus> actions = new ArrayList<>();

            String lras = response.readEntity(String.class);

            JsonReader reader = Json.createReader(new StringReader(lras));
            JsonArray ja = reader.readArray();

            ja.forEach(jsonValue ->
                    actions.add(toLRAStatus(((JsonObject) jsonValue))));

            return actions;
        } finally {
            releaseConnection(response);
        }
    }

    private LRAStatus toLRAStatus(JsonObject jo) {
        try {
            return new LRAStatus(
                    jo.getString("lraId"),
                    jo.getString("clientId"),
                    jo.getBoolean("completed"),
                    jo.getBoolean("compensated"),
                    jo.getBoolean("recovering"),
                    jo.getBoolean("active"),
                    jo.getBoolean("topLevel"));
        } catch (Exception e) {
            System.out.printf("Error parsing json LRAStatus");

            return new LRAStatus(e);
        }
    }

    @Override
    public Boolean isActiveLRA(URL lraId) throws GenericLRAException {
        try {
            return !getStatus(lraId).isPresent(); // empty means the LRA has not entered the termination phase
        } catch (GenericLRAException e) {
            if (e.getStatusCode() == Response.Status.NOT_FOUND.getStatusCode())
                return false;

            throw e;
        }
    }

    @Override
    public Boolean isCompensatedLRA(URL lraId) throws GenericLRAException {
        return isStatus(lraId, CompensatorStatus.Compensated);
    }

    @Override
    public Boolean isCompletedLRA(URL lraId) throws GenericLRAException {
        return isStatus(lraId, CompensatorStatus.Completed);
    }

    public static Map<String, String> getTerminationUris(Class<?> compensatorClass, URI baseUri) {
        Map<String, String> paths = new HashMap<>();
        final boolean[] asyncTermination = {false};
        Annotation resourcePathAnnotation = compensatorClass.getAnnotation(Path.class);
        String resourcePath = resourcePathAnnotation == null
                ? ""
                : ((Path) resourcePathAnnotation).value().replaceAll("^/+", "");

        final String uriPrefix = String.format("%s:%s%s",
                baseUri.getScheme(), baseUri.getSchemeSpecificPart(), resourcePath)
                .replaceAll("/$", "");

        Arrays.stream(compensatorClass.getMethods()).forEach(method -> {
            Annotation pathAnnotation = method.getAnnotation(Path.class);

            if (pathAnnotation != null) {

                if (checkMethod(paths, COMPENSATE, (Path) pathAnnotation,
                        method.getAnnotation(Compensate.class), uriPrefix) != 0) {
                    TimeLimit timeLimit = method.getAnnotation(TimeLimit.class);

                    if (timeLimit != null)
                        paths.put(TIMELIMIT_PARAM_NAME, Long.toString(timeLimit.unit().toMillis(timeLimit.limit())));

                    if (isAsyncCompletion(method))
                        asyncTermination[0] = true;
                }

                if (checkMethod(paths, COMPLETE, (Path) pathAnnotation,
                        method.getAnnotation(Complete.class), uriPrefix) != 0) {
                    if (isAsyncCompletion(method))
                        asyncTermination[0] = true;
                }
                checkMethod(paths, STATUS, (Path) pathAnnotation,
                        method.getAnnotation(Status.class), uriPrefix);
                checkMethod(paths, FORGET, (Path) pathAnnotation,
                        method.getAnnotation(Forget.class), uriPrefix);

                checkMethod(paths, LEAVE, (Path) pathAnnotation, method.getAnnotation(Leave.class), uriPrefix);
            }
        });

        if (asyncTermination[0] && !paths.containsKey(STATUS) && !paths.containsKey(FORGET))
            throw new GenericLRAException(null, Response.Status.BAD_REQUEST.getStatusCode(),
                    "LRA participant class with asynchronous temination but no @Status or @Forget annotations", null);

        StringBuilder linkHeaderValue = new StringBuilder();

        if (paths.size() != 0) {
            paths.forEach((k, v) -> makeLink(linkHeaderValue, null, k, v));
            paths.put("Link", linkHeaderValue.toString());
        }

        return paths;
    }

    public static boolean isAsyncCompletion(Method method) {
        if (method.isAnnotationPresent(Complete.class) || method.isAnnotationPresent(Compensate.class)) {
            for (Annotation[] ann : method.getParameterAnnotations())
                for (Annotation an : ann)
                    if (Suspended.class.getName().equals(an.annotationType().getName())) {
                        System.out.printf("WARNING: JAX-RS @Suspended annotation is untested");
                        return true;
                    }
        }

        return false;
    }

    private static int checkMethod(Map<String, String> paths,
                                   String rel,
                                   Path pathAnnotation,
                                   Annotation annotationClass,
                                   String uriPrefix) {
            /*
             * If the annotationClass is null the requested participant annotation is not present,
             * but we also need to check for conformance with the interoperability spec,
             * ie look for paths of the form:
             * `<participant URL>/compensate`
             * `<participant URL>/complete`
             * etc
             */
        if (annotationClass == null) {
            // TODO support standard compenators with: && !pathAnnotation.value().endsWith(rel)) {
            // ie ones that do not use the @Compensate annotation
            return 0;
        }

        paths.put(rel, uriPrefix + pathAnnotation.value());

        return 1;
    }

    private boolean isStatus(URL lraId, CompensatorStatus testStatus) {
        Optional<CompensatorStatus> status = getStatus(lraId);

        // return status.map(s -> s == testStatus).orElseGet(() -> testStatus == null);

        if (!status.isPresent())
            return testStatus == null;

        return status.get() == testStatus;
    }

    public Optional<CompensatorStatus> getStatus(URL lraId) throws GenericLRAException {
        Response response = null;

        try {
            aquireConnection();

            response = getTarget().path(getLRAId(lraId.toString()))
                    .request()
                    .accept(MediaType.TEXT_PLAIN_TYPE)
                    .get();
        } catch (Exception e) {
            releaseConnection(null);

            throw new GenericLRAException(lraId,
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    "Could not access the LRA coordinator: " + e.getMessage(),
                    e);
        }

        try {
            if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode())
                return Optional.empty();

            if (response.getStatus() != Response.Status.OK.getStatusCode())
                throw new GenericLRAException(lraId,
                        response.getStatus(),
                        "LRA coordinator returned an invalid status code",
                        null);

            if (!response.hasEntity())
                throw new GenericLRAException(lraId,
                        Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                        "LRA coordinator#getStatus returned 200 OK but no content",
                        null);

            // convert the returned String into a status
            Optional<CompensatorStatus> status;

            try {
                return fromString(response.readEntity(String.class));
            } catch (IllegalArgumentException e) {
                throw new GenericLRAException(lraId,
                        Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                        "LRA coordinator returned an invalid status",
                        e);
            }
        } finally {
            releaseConnection(response);
        }
    }

    /**
     *
     * @param status the string that is to be converted to a CompensatorStatus
     * @return the corresponding status or empty
     * @throws IllegalArgumentException if the status is not a valid enum constant
     */
    private static Optional<CompensatorStatus> fromString(String status) {
        if (status != null)
            return Optional.of(CompensatorStatus.valueOf(status));

        return Optional.empty();
    }

    private static StringBuilder makeLink(StringBuilder b, String uriPrefix, String key, String value) {

        if (value == null)
            return b;

        String terminationUri = uriPrefix == null ? value : String.format("%s%s", uriPrefix, value);
        Link link =  Link.fromUri(terminationUri).title(key + " URI").rel(key).type(MediaType.TEXT_PLAIN).build();

        if (b.length() != 0)
            b.append(',');

        return b.append(link);
    }

    private String enlistCompensator(URL lraUrl, long timelimit, String uriPrefix,
                                     String compensateUrl, String completeUrl,
                                     String forgetUrl, String leaveUrl, String statusUrl,
                                     String compensatorData) {
        validateURL(completeUrl, true, "Invalid complete URL: %s");
        validateURL(compensateUrl, true, "Invalid compensate URL: %s");
        validateURL(leaveUrl, true, "Invalid status URL: %s");
        validateURL(forgetUrl, true, "Invalid forgetUrl URL: %s");
        validateURL(statusUrl, true, "Invalid status URL: %s");

        Map<String, String> terminateURIs = new HashMap<>();

        terminateURIs.put(LRAClient.COMPENSATE, compensateUrl);
        terminateURIs.put(LRAClient.COMPLETE, completeUrl);
        terminateURIs.put(LRAClient.LEAVE, leaveUrl);
        terminateURIs.put(LRAClient.STATUS, statusUrl);
        terminateURIs.put(LRAClient.FORGET, forgetUrl);

        // register with the coordinator
        // put the lra id in an http header
        StringBuilder linkHeaderValue = new StringBuilder();

        terminateURIs.forEach((k, v) -> makeLink(linkHeaderValue, uriPrefix, k, v)); // or use Collectors.joining(",")

        return enlistCompensator(lraUrl, timelimit, linkHeaderValue.toString(), compensatorData);
    }

    private String enlistCompensator(URL lraUrl, long timelimit, String linkHeader, String compensatorData) {
        // register with the coordinator
        // put the lra id in an http header
        Response response = null;

        if (timelimit < 0)
            timelimit = 0L;

        try {
            response = getTarget().path(getLRAId(lraUrl.toString()))
                    .queryParam(TIMELIMIT_PARAM_NAME, timelimit)
                    .request()
                    .header("Link", linkHeader)
                    .header(LRA_HTTP_HEADER, lraUrl)
                    .put(Entity.entity(compensatorData == null ? linkHeader : compensatorData, MediaType.TEXT_PLAIN));

            if (response.getStatus() == Response.Status.PRECONDITION_FAILED.getStatusCode()) {
                throw new IllegalLRAStateException(lraUrl.toString(),
                        "Too late to join with this LRA", null);
            } else if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                lraTrace(String.format("enlist in LRA failed (%d)", response.getStatus()), lraUrl);

                throw new GenericLRAException(lraUrl, response.getStatus(),
                        "unable to register participant", null);
            }

            return response.readEntity(String.class);
        } finally {
            releaseConnection(response);
        }
    }

    private String endLRA(URL lra, boolean confirm) throws GenericLRAException {
        String confirmUrl = String.format(confirm ? confirmFormat : compensateFormat, getLRAId(lra.toString()));
        Response response = null;

        lraTrace(String.format("%s LRA", confirm ? "close" : "compensate"), lra);

        try {
            response = getTarget().path(confirmUrl).request().put(Entity.text(""));

            checkStatus(lra, response,"LRA finished with an unexpected status code: %d",
                    Response.Status.OK, Response.Status.ACCEPTED);

            String responseData = response.readEntity(String.class);

            setResponseData(lra, responseData);

            return responseData;
        } finally {

            releaseConnection(response);

            Current.pop(lra);

            URL nextLRA = Current.peek();

            if (nextLRA != null) {
                try {
                    init(nextLRA);
                } catch (URISyntaxException ignore) {
                    // the validity of the url was checked when we added it to Current
                }
            }
        }
    }

    private void validateURL(String url, boolean nullAllowed, String message) {
        if (url == null) {
            if (!nullAllowed)
                throw new GenericLRAException(null, Response.Status.NOT_ACCEPTABLE.getStatusCode(),
                        String.format(message, "null value"), null);
        } else {
            try {
                new URL(url);
            } catch (MalformedURLException e) {
                throw new GenericLRAException(null, Response.Status.NOT_ACCEPTABLE.getStatusCode(),
                        String.format(message, e.getMessage()), e);
            }
        }
    }

    private void assertNotNull(Object lra, String message) {
        if (lra == null)
            throw new GenericLRAException(null, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    message, null);
    }

    private void checkStatus(URL lraId, Response response, String messageFormat, Response.Status... expected) {
        for (Response.Status anExpected : expected) {
            if (response.getStatus() == anExpected.getStatusCode())
                return;
        }

        throw new GenericLRAException(lraId, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                String.format(messageFormat, response.getStatus()), null);
    }

    public String getUrl() {
        return base.toString();
    }

    public URL getCurrent() {
        return Current.peek();
    }

    private void lraTrace(String reason, URL lra) {
        if (isTrace)
            System.out.printf("LRAClient: %s: lra: %s%n", reason, lra == null ? "null" : lra);
    }

    public void close() {
        client.close();
        if (responseDataMap != null)
            responseDataMap.clear();
    }

    private void aquireConnection() {
        if (connectionInUse) {
            System.out.printf("LRAClient: trying to aquire an in use connection");

            throw new GenericLRAException(null, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    "LRAClient: trying to aquire an in use connection", null);
        }

        connectionInUse = true;
    }

    private void releaseConnection(Response response) {
        if (response != null)
            response.close();

        connectionInUse = false;
    }

    private void setResponseData(URL lraId, String responseData) {
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
    }

    public List<String> getResponseData(URL lraId) {
        return responseDataMap.containsKey(lraId) ? responseDataMap.get(lraId) : Collections.emptyList();
    }
}
