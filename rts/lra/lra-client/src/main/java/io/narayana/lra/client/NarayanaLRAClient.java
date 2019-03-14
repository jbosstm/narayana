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

import static io.narayana.lra.LRAConstants.CLIENT_ID_PARAM_NAME;
import static io.narayana.lra.LRAConstants.COMPENSATE;
import static io.narayana.lra.LRAConstants.COMPLETE;
import static io.narayana.lra.LRAConstants.COORDINATOR_PATH_NAME;
import static io.narayana.lra.LRAConstants.FORGET;
import static io.narayana.lra.LRAConstants.LEAVE;
import static io.narayana.lra.LRAConstants.PARENT_LRA_PARAM_NAME;
import static io.narayana.lra.LRAConstants.RECOVERY_COORDINATOR_PATH_NAME;
import static io.narayana.lra.LRAConstants.STATUS;
import static io.narayana.lra.LRAConstants.TIMELIMIT_PARAM_NAME;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
import static javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE;

import java.io.Closeable;
import java.io.StringReader;
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
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.narayana.lra.Current;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.ParticipantStatus;

import io.narayana.lra.logging.LRALogger;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.Forget;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.eclipse.microprofile.lra.annotation.ws.rs.Leave;
import org.eclipse.microprofile.lra.annotation.Status;
import org.eclipse.microprofile.lra.client.GenericLRAException;
import org.eclipse.microprofile.lra.client.IllegalLRAStateException;
import org.eclipse.microprofile.lra.client.InvalidLRAIdException;
import org.eclipse.microprofile.lra.client.LRAClient;

/**
 * A utility class for controlling the lifecycle of Long Running Actions (LRAs) but the prefered mechanism is to use
 * the annotation in the {@link org.eclipse.microprofile.lra.annotation} package
 */
@RequestScoped
public class NarayanaLRAClient implements LRAClient, Closeable {

    public static final long DEFAULT_TIMEOUT_MILLIS = 0L;

    private static final String startLRAUrl = "/start";///?ClientId=abc&timeout=300000";
    private static final String recoveryQueryUrl = "/recovery";
    private static final String getAllLRAsUrl = "/";
    private static final String getRecoveringLRAsUrl = "?status=Cancelling";
    private static final String getActiveLRAsUrl = "?status=";

    private static final String confirmFormat = "/%s/close";
    private static final String compensateFormat = "/%s/cancel";
    private static final String leaveFormat = "/%s/remove";
    private static final String renewFormat = "/%s/renew";

    private static final String LINK_TEXT = "Link";

    private URI base;
    private URI rcBase; // base uri of the recovery coordinator
    private ClientBuilder clientBuilder;
    private Client client;
    private boolean isUseable;
    private boolean connectionInUse;
    private Map<URI, String> responseDataMap;

    private static URI defaultCoordinatorURI;

    public static void setDefaultCoordinatorEndpoint(URI lraCoordinatorEndpoint) {
        defaultCoordinatorURI = lraCoordinatorEndpoint;
    }

    public static void setDefaultRecoveryEndpoint(URI recoveryEndpoint) {
        LRALogger.logger.debugf(
                "LRAClient assuming the LRA coordinator and recovery coordinator are on the same endpoint");
    }

    public static boolean isInitialised() {
        return defaultCoordinatorURI != null;
    }

    /**
     * Creating LRA client where expecting LRA coordinator being at
     * <code>http://localhost:8080</code>
     */
    public NarayanaLRAClient() throws URISyntaxException {
        if (defaultCoordinatorURI != null) {
            try {
                init(defaultCoordinatorURI);
            } catch (MalformedURLException e) {
                throw new URISyntaxException(defaultCoordinatorURI.toString(), e.getMessage());
            }
        } else {
            init("http",
                    System.getProperty(LRA_COORDINATOR_HOST_KEY, "localhost"),
                    Integer.getInteger(LRA_COORDINATOR_PORT_KEY, 8080));
        }
    }

    /**
     * Creating LRA client where expecting LRA coordinator being available through <code>http</code>
     * protocol at <i>host</i>:<i>port</i>.
     *
     * @param host  hostname where the LRA coordinator will be contacted
     * @param port  port where the LRA coordinator will be contacted
     */
    public NarayanaLRAClient(String host, int port) throws URISyntaxException {
        this("http", host, port);
    }

    /**
     * Creating LRA client where expecting LRA coordinator being available through
     * protocol <i>scheme</i> at <i>host</i>:<i>port</i>.
     *
     * @param scheme  protocol used to contact the LRA coordinator
     * @param host  hostname where the LRA coordinator will be contacted
     * @param port  port where the LRA coordinator will be contacted
     */
    public NarayanaLRAClient(String scheme, String host, int port) throws URISyntaxException {
        init(scheme, host, port);
    }

    /**
     * Creating LRA client where expecting LRA coordinator being available
     * at the provided uri.
     *
     * @param coordinatorUri  uri of the lra coordinator
     */
    public NarayanaLRAClient(URI coordinatorUri) throws MalformedURLException, URISyntaxException {
        init(coordinatorUri);
    }

    private void init(URI coordinatorUri) throws URISyntaxException, MalformedURLException {
        init(coordinatorUri.toURL().getProtocol(), coordinatorUri.getHost(), coordinatorUri.getPort());
    }

    public void connectTimeout(long connect, TimeUnit unit) {
        clientBuilder.connectTimeout(connect, unit);

        if (client != null) {
            client.close();
            client = null;
        }
    }

    public void readTimeout(long read, TimeUnit unit) {
        clientBuilder.readTimeout(read, unit);

        if (client != null) {
            client.close();
            client = null;
        }
    }

    public void setCoordinatorURI(URI uri) {
        base = uri;

        isUseable = true;

        if (responseDataMap == null) {
            postConstruct();
        } else {
            responseDataMap.clear();
        }
    }

    public void setRecoveryCoordinatorURI(URI uri) {
        setCoordinatorURI(uri); // same as the LRA coordinator
    }

    private void init(String scheme, String host, int port) throws URISyntaxException {
        clientBuilder = ClientBuilder.newBuilder();
        setCoordinatorURI(new URI(scheme, null, host, port, "/" + COORDINATOR_PATH_NAME, null, null));
        rcBase = new URI(scheme, null, host, port, "/" + RECOVERY_COORDINATOR_PATH_NAME, null, null);
    }

    /**
     * Defines if the LRA client is an active instance and was not destroyed.
     *
     * @return  true if it's active, false if it was destroyed
     */
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

    /**
     * Extract the uid part from an LRA URL.
     *
     * @param lraId  LRA id to extract from
     * @return  uid of lra extracted from LRA id URL
     */
    public static String getLRAId(String lraId) {
        return lraId == null ? null : lraId.replaceFirst(".*/([^/?]+).*", "$1");
    }

    /**
     * Converting LRA id string format to URL.
     *
     * @param lraId  string LRA id
     * @return URL format of the lraId
     * @throws InvalidLRAIdException  if the string lra id can't be transformed to URL
     */
    public URL toURL(String lraId) throws InvalidLRAIdException {
        try {
            return new URL(lraId);
        } catch (MalformedURLException e) {
            LRALogger.i18NLogger.error_invalidStringFormatOfUrl(lraId, e);
            throw new InvalidLRAIdException(lraId, "Invalid syntax", e);
        }
    }

    private WebTarget getTarget() {
        // return target; // TODO can't share the target if a service makes multiple JAX-RS requests
        if (client != null) {
            client.close(); // hacking
        }

        client = ClientBuilder.newClient();

        return client.target(base);
    }

    public void setCurrentLRA(URI coordinatorUri) {
        URL url = null;

        try {
            url = coordinatorUri.toURL();
            init(coordinatorUri);
        } catch (URISyntaxException | MalformedURLException e) {
            LRALogger.i18NLogger.error_invalidCoordinatorUrl(url, e);
            throw new GenericLRAException(coordinatorUri, Response.Status.BAD_REQUEST.getStatusCode(), e.getMessage(), e);
        }
    }

    /**
     * Starting LRA. You provide client id determining the LRA being started.
     *
     * @param clientID  client id determining the LRA
     * @return  LRA id as URL
     * @throws GenericLRAException  thrown when start of the LRA failed
     */
    public URI startLRA(String clientID) throws GenericLRAException {
        return startLRA(clientID, 0L);
    }

    /**
     * Starting LRA. You provide client id that joins the LRA context
     * and is passed when working with the LRA.
     *
     * @param clientID  client id determining the LRA
     * @param timeout  timeout value in seconds, when timeouted the LRA will be compensated
     * @return  LRA id as URL
     * @throws GenericLRAException  thrown when start of the LRA failed
     */
    public URI startLRA(String clientID, Long timeout) throws GenericLRAException {
        return startLRA(clientID, timeout, ChronoUnit.SECONDS);
    }

    @Override
    public URI startLRA(String clientID, Long timeout, ChronoUnit unit) throws GenericLRAException {
        return startLRA(getCurrent(), clientID, timeout, unit);
    }

    @Override
    public URI startLRA(URI parentLRA, String clientID, Long timeout, ChronoUnit unit) throws GenericLRAException {
        Response response = null;
        URI lra;

        if (clientID == null) {
            clientID = "";
        }

        if (timeout == null) {
            timeout = 0L;
        } else if (timeout < 0) {
            throw new GenericLRAException(parentLRA, Response.Status.BAD_REQUEST.getStatusCode(),
                    "Invalid timeout value: " + timeout, null);
        }

        lraTracef("startLRA for client %s with parent %s", clientID, parentLRA);

        try {
            String encodedParentLRA = parentLRA == null ? "" : URLEncoder.encode(parentLRA.toString(), "UTF-8");

            aquireConnection();

            response = getTarget().path(startLRAUrl)
                    .queryParam(TIMELIMIT_PARAM_NAME, Duration.of(timeout, unit).toMillis())
                    .queryParam(CLIENT_ID_PARAM_NAME, clientID)
                    .queryParam(PARENT_LRA_PARAM_NAME, encodedParentLRA)
                    .request()
                    .post(Entity.text(""));

            // validate the HTTP status code says an LRAInfo resource was created
            if (!isExpectedResponseStatus(response, Response.Status.CREATED)) {
                LRALogger.i18NLogger.error_lraCreationUnexpectedStatus(response.getStatus(), response);
                throw new GenericLRAException(null, INTERNAL_SERVER_ERROR.getStatusCode(),
                        "LRA start returned an unexpected status code: " + response.getStatus(), null);
            }

            // validate that there is an LRAInfo response header holding the LRAInfo id
            Object lraObject = Current.getLast(response.getHeaders().get(LRA_HTTP_HEADER));

            if (lraObject == null) {
                LRALogger.i18NLogger.error_nullLraOnCreation(response);
                throw new GenericLRAException(null, INTERNAL_SERVER_ERROR.getStatusCode(), "LRA creation is null", null);
            }

            lra = new URI(URLDecoder.decode(lraObject.toString(), "UTF-8"));

            lraTrace(lra, "startLRA returned");

            Current.push(lra);

        } catch (UnsupportedEncodingException e) {
            LRALogger.i18NLogger.error_cannotCreateUrlFromLCoordinatorResponse(response, e);
            throw new GenericLRAException(null, INTERNAL_SERVER_ERROR.getStatusCode(), e.getMessage(), e);
        } catch (Exception e) {
            LRALogger.i18NLogger.error_cannotContactLRACoordinator(base, e);

            if (e.getCause() != null && ConnectException.class.equals(e.getCause().getClass())) {
                throw new GenericLRAException(null, SERVICE_UNAVAILABLE.getStatusCode(),
                        "Cannot connect to the LRA coordinator: " + base + " (" + e.getCause().getMessage() + ")", e);
            }

            throw new GenericLRAException(null, Response.Status.SERVICE_UNAVAILABLE.getStatusCode(), e.getMessage(), e);
        } finally {
            releaseConnection(response);
        }

        // check that the lra is active
        // isActiveLRA(lra);

        return lra;
    }

    public NarayanaLRAInfo getLRAInfo(URI lraId) throws GenericLRAException {
        Response response = null;

        lraTracef(lraId, "getLRAInfo for LRA %s", lraId.toASCIIString());

        try {
            aquireConnection();

            response = getTarget().path(lraId.toString())
                    .request()
                    .get();

            if (!response.hasEntity()) {
                throw new GenericLRAException(null, response.getStatus(),
                        "missing entity body for getLRAInfo response", null);
            }

            return response.readEntity(NarayanaLRAInfo.class);
        } finally {
            releaseConnection(response);
        }
    }

    @Override
    public void renewTimeLimit(URI lraId, long timeLimit, ChronoUnit timeUnit) {
        Response response = null;
        long millis = Duration.of(timeLimit, timeUnit).toMillis();

        lraTracef(lraId, "renew time limit to %s s of LRA", millis);

        try {
            aquireConnection();

            response = getTarget().path(String.format(renewFormat, getLRAId(lraId.toString())))
                    .queryParam(TIMELIMIT_PARAM_NAME, millis)
                    .request()
                    .header(LRA_HTTP_HEADER, lraId)
                    .put(Entity.text(""));

            if (Response.Status.OK.getStatusCode() != response.getStatus()) {
                LRALogger.i18NLogger.error_lraRenewalUnexpectedStatus(response.getStatus(), response);
                throw new GenericLRAException(lraId, response.getStatus(), "", null);
            }
        } finally {
            releaseConnection(response);
        }
    }

    @Override
    public String cancelLRA(URI lraId) throws GenericLRAException {
        return endLRA(lraId, false);
    }

    @Override
    public String closeLRA(URI lraId) throws GenericLRAException {
        return endLRA(lraId, true);
    }

    /**
     *
     * @param lraUri the URL of the LRA to join
     * @param timelimit how long the participant is prepared to wait for LRA completion
     * @param linkHeader participant protocol URLs in link header format (RFC 5988)
     * @param compensatorData  data provided during compensation
     * @return a recovery URL for this enlistment
     *
     * @throws GenericLRAException if the LRA coordinator failed to enlist the participant
     */
    private URI joinLRAWithLinkHeader(URI lraUri, Long timelimit, String linkHeader,
                                     String compensatorData) throws GenericLRAException {
        lraTracef(lraUri, "joining LRA with participant link: %s", linkHeader);
        return enlistCompensator(lraUri, timelimit, linkHeader, compensatorData);
    }

    public URI joinLRA(URI lraId, Long timelimit,
                       URI compensateUri, URI completeUri, URI forgetUri, URI leaveUri, URI statusUri,
                       String compensatorData) throws GenericLRAException {
        return enlistCompensator(lraId, timelimit, "",
                compensateUri, completeUri,
                forgetUri, leaveUri, statusUri,
                compensatorData);
    }

    @Override
    public URI joinLRA(URI lraId, Class<?> resourceClass, URI baseUri,
                          String compensatorData) throws GenericLRAException {
        Map<String, String> terminateURIs = getTerminationUris(resourceClass, baseUri);
        String timeLimitStr = terminateURIs.get(TIMELIMIT_PARAM_NAME);
        long timeLimit = timeLimitStr == null ? NarayanaLRAClient.DEFAULT_TIMEOUT_MILLIS : Long.valueOf(timeLimitStr);

        if (terminateURIs.containsKey(LINK_TEXT)) {
            return joinLRAWithLinkHeader(lraId, timeLimit, terminateURIs.get(LINK_TEXT), compensatorData);
        }

        return null;
    }

    @Override
    public URI updateCompensator(URI recoveryUri, Class<?> aClass, URI baseUri, String s) throws GenericLRAException {
        return null; // TODO
    }

    public void leaveLRA(URI lraId, String body) throws GenericLRAException {
        Response response = null;

        try {
            aquireConnection();

            response = getTarget().path(String.format(leaveFormat, getLRAId(lraId.toString())))
                    .request()
                    .header(LRA_HTTP_HEADER, lraId)
                    .put(Entity.entity(body, MediaType.TEXT_PLAIN));

            if (Response.Status.OK.getStatusCode() != response.getStatus()) {
                LRALogger.i18NLogger.error_lraLeaveUnexpectedStatus(response.getStatus(), response);
                throw new GenericLRAException(null, response.getStatus(), "", null);
            }
        } finally {
            releaseConnection(response);
        }
    }

    @Override
    public void leaveLRA(URI recoveryUri) throws GenericLRAException {
        Response response = null;
        URI lraId = getCurrent();

        try {
            aquireConnection();

            response = getTarget().path(String.format(leaveFormat, getLRAId(lraId.toASCIIString())))
                    .request()
                    .header(LRA_HTTP_HEADER, lraId)
                    .put(Entity.entity(recoveryUri, MediaType.TEXT_PLAIN));

            if (Response.Status.OK.getStatusCode() != response.getStatus()) {
                LRALogger.i18NLogger.error_lraLeaveUnexpectedStatus(response.getStatus(), response);
                throw new GenericLRAException(recoveryUri, response.getStatus(), "", null);
            }
        } finally {
            releaseConnection(response);
        }
    }

    public List<NarayanaLRAInfo> getAllLRAs() throws GenericLRAException {
        return getLRAs(null, null);
    }

    public List<NarayanaLRAInfo> getActiveLRAs() throws GenericLRAException {
        return getLRAs(STATUS, "");
    }

    public List<NarayanaLRAInfo> getRecoveringLRAs() throws GenericLRAException {
        Client rcClient = null;

        try {
            rcClient = ClientBuilder.newClient();
            Response response = rcClient.target(rcBase)
                    .path(recoveryQueryUrl)
                    .request()
                    .get();

            if (!response.hasEntity()) {
                throw new GenericLRAException(null, response.getStatus(), "missing entity body", null);
            }

            List<NarayanaLRAInfo> actions = new ArrayList<>();

            String lras = response.readEntity(String.class);

            JsonReader reader = Json.createReader(new StringReader(lras));
            JsonArray ja = reader.readArray();

            ja.forEach(jsonValue ->
                    actions.add(toLRAInfo(((JsonObject) jsonValue))));

            actions.addAll(getLRAs(STATUS, ParticipantStatus.Compensating.name()));

            return actions;
        } finally {
            if (rcClient != null) {
                rcClient.close();
            }
        }
    }

    private List<NarayanaLRAInfo> getLRAs(String queryName, String queryValue) {
        Response response = null;

        try {
            aquireConnection();

            if (queryName == null) {
                response = getTarget().request().get();
            } else {
                response = getTarget().queryParam(queryName, queryValue).request().get();
            }

            if (!response.hasEntity()) {
                throw new GenericLRAException(null, response.getStatus(), "missing entity body", null);
            }

            List<NarayanaLRAInfo> actions = new ArrayList<>();

            String lras = response.readEntity(String.class);

            JsonReader reader = Json.createReader(new StringReader(lras));
            JsonArray ja = reader.readArray();

            ja.forEach(jsonValue ->
                    actions.add(toLRAInfo(((JsonObject) jsonValue))));

            return actions;
        } finally {
            releaseConnection(response);
        }
    }

    private NarayanaLRAInfo toLRAInfo(JsonObject jo) {
        try {
            long startTime = jo.getInt("startTime");
            long fini = jo.getInt("finishTime");

            return new NarayanaLRAInfo(
                    jo.getString("lraId"),
                    jo.getString("clientId"),
                    jo.getString(STATUS),
                    jo.getBoolean("closed"),
                    jo.getBoolean("cancelled"),
                    jo.getBoolean("recovering"),
                    jo.getBoolean("active"),
                    jo.getBoolean("topLevel"),
                    startTime,
                    fini);
        } catch (Exception e) {
            LRALogger.i18NLogger.warn_failedParsingStatusFromJson(jo, e);
            return new NarayanaLRAInfo("JSON Parse Error: " + e.getMessage(),
                    e.getMessage(),
                    "Unknown",
                    false, false, false, false, false,
                    LocalDateTime.now().getSecond(), LocalDateTime.now().getSecond());
        }
    }

    public Boolean isActiveLRA(URI lraId) throws GenericLRAException {
        try {
            return getStatus(lraId) == LRAStatus.Active;
        } catch (GenericLRAException e) {
            if (e.getStatusCode() == Response.Status.NOT_FOUND.getStatusCode()) {
                return false;
            }

            throw e;
        }
    }

    public Boolean isCompensatedLRA(URI lraId) throws GenericLRAException {
        return isStatus(lraId, LRAStatus.Cancelled);
    }

    public Boolean isCompletedLRA(URI lraId) throws GenericLRAException {
        return isStatus(lraId, LRAStatus.Closed);
    }

    /**
     * For particular compensator class it returns termination uris based on the provided base uri.
     * You get map of string and URI.
     *
     * @param compensatorClass  compensator class to examine
     * @param baseUri  base URI used on creation of the termination map.
     * @return map of URI
     */
    public static Map<String, String> getTerminationUris(Class<?> compensatorClass, URI baseUri) {
        Map<String, String> paths = new HashMap<>();
        final boolean[] asyncTermination = {false};
        Path resourcePathAnnotation = compensatorClass.getAnnotation(Path.class);
        String resourcePath = resourcePathAnnotation == null
                ? ""
                : resourcePathAnnotation.value().replaceAll("^/+", "");

        final String uriPrefix = String.format("%s:%s%s",
                baseUri.getScheme(), baseUri.getSchemeSpecificPart(), resourcePath)
                .replaceAll("/$", "");

        Arrays.stream(compensatorClass.getMethods()).forEach(method -> {
            Path pathAnnotation = method.getAnnotation(Path.class);

            if (pathAnnotation != null) {

                if (checkMethod(paths, COMPENSATE, pathAnnotation,
                        method.getAnnotation(Compensate.class), uriPrefix) != 0) {
                    long timeLimit = method.getAnnotation(Compensate.class).timeLimit();
                    ChronoUnit timeUnit = method.getAnnotation(Compensate.class).timeUnit();

                    paths.put(TIMELIMIT_PARAM_NAME, Long.toString(Duration.of(timeLimit, timeUnit).toMillis()));

                    if (isAsyncCompletion(method)) {
                        asyncTermination[0] = true;
                    }
                }

                if (checkMethod(paths, COMPLETE, pathAnnotation,
                        method.getAnnotation(Complete.class), uriPrefix) != 0) {
                    if (isAsyncCompletion(method)) {
                        asyncTermination[0] = true;
                    }
                }
                checkMethod(paths, STATUS, pathAnnotation,
                        method.getAnnotation(Status.class), uriPrefix);
                checkMethod(paths, FORGET, pathAnnotation,
                        method.getAnnotation(Forget.class), uriPrefix);

                checkMethod(paths, LEAVE, pathAnnotation, method.getAnnotation(Leave.class), uriPrefix);
            }
        });

        if (asyncTermination[0] && !paths.containsKey(STATUS) && !paths.containsKey(FORGET)) {
            LRALogger.i18NLogger.error_asyncTerminationBeanMissStatusAndForget(compensatorClass);
            throw new GenericLRAException(null, Response.Status.BAD_REQUEST.getStatusCode(),
                    "LRA participant class with asynchronous temination but no @Status or @Forget annotations", null);
        }

        StringBuilder linkHeaderValue = new StringBuilder();

        if (paths.size() != 0) {
            paths.forEach((k, v) -> makeLink(linkHeaderValue, null, k, v));
            paths.put(LINK_TEXT, linkHeaderValue.toString());
        }

        return paths;
    }

    /**
     * Providing information if method is defined to be completed asynchronously.
     * This means that {@link Suspended} annotation is available amongst the method parameters
     * while the method is annotated with {@link Complete} or {@link Compensate}.
     *
     * @param method  method to be checked for async completion
     * @return  true if method is to complete asynchronously, false if synchronously
     */
    public static boolean isAsyncCompletion(Method method) {
        if (method.isAnnotationPresent(Complete.class) || method.isAnnotationPresent(Compensate.class)) {
            for (Annotation[] ann : method.getParameterAnnotations()) {
                for (Annotation an : ann) {
                    if (Suspended.class.getName().equals(an.annotationType().getName())) {
                        LRALogger.logger.warn("JAX-RS @Suspended annotation is untested");
                        return true;
                    }
                }
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

    private boolean isStatus(URI lraId, LRAStatus testStatus) {
        LRAStatus status = getStatus(lraId);
        return status == testStatus;
    }

    @Override
    public LRAStatus getStatus(URI uri) throws GenericLRAException {
        Response response = null;
        URL lraId;

        try {
            lraId = uri.toURL();
        } catch (MalformedURLException e) {
            throw new GenericLRAException(null,
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    "Could not convert LRA to a URL: " + e.getMessage(),
                    e);
        }

        try {
            aquireConnection();

            response = getTarget().path(getLRAId(lraId.toString()))
                    .request()
                    .accept(MediaType.TEXT_PLAIN_TYPE)
                    .get();
        } catch (Exception e) {
            releaseConnection(null);

            LRALogger.i18NLogger.error_cannotAccesCorrdinatorWhenGettingStatus(base, lraId, e);
            throw new GenericLRAException(uri,
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    "Could not access the LRA coordinator: " + e.getMessage(),
                    e);
        }

        try {
            if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                String responseContent = response.readEntity(String.class);
                throw new NotFoundException("Failed to get status of LRA id " + lraId
                        + (responseContent != null ? ": " + responseContent : ""));
            }

            if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
                return LRAStatus.Active;
            }

            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                LRALogger.i18NLogger.error_invalidStatusCode(base, response.getStatus(), lraId);
                throw new GenericLRAException(uri,
                        response.getStatus(),
                        "LRA coordinator returned an invalid status code",
                        null);
            }

            if (!response.hasEntity()) {
                LRALogger.i18NLogger.error_noContentOnGetStatus(base, lraId);
                throw new GenericLRAException(uri,
                        Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                        "LRA coordinator#getStatus returned 200 OK but no content: lra: " + lraId, null);
            }

            // convert the returned String into a status
            try {
                return fromString(response.readEntity(String.class));
            } catch (IllegalArgumentException e) {
                LRALogger.i18NLogger.error_invalidArgumentOnStatusFromCoordinator(base, lraId, e);
                throw new GenericLRAException(uri,
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
    private static LRAStatus fromString(String status) {
        if (status == null) {
            throw new IllegalArgumentException("The status parameter is null");
        }
        return LRAStatus.valueOf(status);
    }

    private static StringBuilder makeLink(StringBuilder b, String uriPrefix, String key, String value) {

        if (value == null) {
            return b;
        }

        String terminationUri = uriPrefix == null ? value : String.format("%s%s", uriPrefix, value);
        Link link =  Link.fromUri(terminationUri).title(key + " URI").rel(key).type(MediaType.TEXT_PLAIN).build();

        if (b.length() != 0) {
            b.append(',');
        }

        return b.append(link);
    }

    private URI enlistCompensator(URI lraUri, long timelimit, String uriPrefix,
                                  URI compensateUri, URI completeUri,
                                  URI forgetUri, URI leaveUri, URI statusUri,
                                  String compensatorData) {
        validateURI(completeUri, true, "Invalid complete URL: %s");
        validateURI(compensateUri, true, "Invalid compensate URL: %s");
        validateURI(leaveUri, true, "Invalid status URL: %s");
        validateURI(forgetUri, true, "Invalid forgetUri URL: %s");
        validateURI(statusUri, true, "Invalid status URL: %s");

        Map<String, URI> terminateURIs = new HashMap<>();

        terminateURIs.put(COMPENSATE, compensateUri);
        terminateURIs.put(COMPLETE, completeUri);
        terminateURIs.put(LEAVE, leaveUri);
        terminateURIs.put(STATUS, statusUri);
        terminateURIs.put(FORGET, forgetUri);

        // register with the coordinator
        // put the lra id in an http header
        StringBuilder linkHeaderValue = new StringBuilder();

        terminateURIs.forEach((k, v) -> makeLink(linkHeaderValue, uriPrefix, k, v == null ? null : v.toASCIIString()));

        return enlistCompensator(lraUri, timelimit, linkHeaderValue.toString(), compensatorData);
    }

    private URI enlistCompensator(URI uri, long timelimit, String linkHeader, String compensatorData) {
        // register with the coordinator
        // put the lra id in an http header
        Response response = null;
        String responseEntity = null;
        URL lraId;

        try {
            lraId = uri.toURL();
        } catch (MalformedURLException e) {
            throw new GenericLRAException(null,
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    "Could not convert LRA to a URL: " + e.getMessage(),
                    e);
        }
        if (timelimit < 0) {
            timelimit = 0L;
        }

        try {
            response = getTarget().path(getLRAId(uri.toASCIIString()))
                    .queryParam(TIMELIMIT_PARAM_NAME, timelimit)
                    .request()
                    .header(LINK_TEXT, linkHeader)
                    .header(LRA_HTTP_HEADER, lraId)
                    .put(Entity.entity(compensatorData == null ? linkHeader : compensatorData, MediaType.TEXT_PLAIN));

            if (response.getStatus() == Response.Status.PRECONDITION_FAILED.getStatusCode()) {
                LRALogger.i18NLogger.error_tooLateToJoin(lraId, response);
                throw new IllegalLRAStateException(lraId.toString(),
                        "Too late to join with this LRA", "enlistCompensator");
            } else if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                LRALogger.logger.infof("Failed enlisting to LRA '%s', coordinator '%s' responded with status '%s'",
                        lraId, base, Response.Status.NOT_FOUND.getStatusCode());
                throw new NotFoundException(uri.toASCIIString());
            } else if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                LRALogger.i18NLogger.error_failedToEnlist(lraId, base, response.getStatus());

                throw new GenericLRAException(uri, response.getStatus(),
                        "unable to register participant", null);
            }

            try {
                responseEntity = response.readEntity(String.class).replaceAll("^\"|\"$", "");
                return new URI(responseEntity);
            } catch (URISyntaxException e) {
                LRALogger.logger.infof("join %s returned an invalid recovery URI: %", lraId, responseEntity);
                throw new GenericLRAException(null, Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
                        "join " + lraId + " returned an invalid recovery URI: " + responseEntity, e);
            }

        } finally {
            releaseConnection(response);
        }
    }

    private String endLRA(URI lra, boolean confirm) throws GenericLRAException {
        String confirmUrl = String.format(confirm ? confirmFormat : compensateFormat, getLRAId(lra.toString()));
        Response response = null;

        lraTracef(lra, "%s LRA", confirm ? "close" : "compensate");

        try {
            response = getTarget().path(confirmUrl).request().put(Entity.text(""));

            if (!isExpectedResponseStatus(response, Response.Status.OK, Response.Status.ACCEPTED, Response.Status.NOT_FOUND)) {
                LRALogger.i18NLogger.error_lraTerminationUnexpectedStatus(response.getStatus(), response);
                throw new GenericLRAException(lra, INTERNAL_SERVER_ERROR.getStatusCode(),
                        "LRA finished with an unexpected status code: " + response.getStatus(), null);
            }

            if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                LRALogger.logger.infof("Could not %s LRA '%s': coordinator '%s' responded with status '%s'",
                        confirm ? "close" : "compensate", lra, base, Response.Status.NOT_FOUND.getReasonPhrase());
                throw new NotFoundException(lra.toASCIIString());
            }

            String responseData = response.readEntity(String.class);

            setResponseData(lra, responseData);

            return responseData;
        } finally {

            releaseConnection(response);

            Current.pop(lra);

            URI nextLRA = Current.peek();

            if (nextLRA != null) {
                try {
                    init(nextLRA);
                } catch (URISyntaxException | MalformedURLException ignore) {
                    // the validity of the URI was checked when we added it to Current
                }
            }
        }
    }

    private void validateURI(URI uri, boolean nullAllowed, String message) {
        if (uri == null) {
            if (!nullAllowed) {
                throw new GenericLRAException(null, NOT_ACCEPTABLE.getStatusCode(),
                        String.format(message, "null value"), null);
            }
        } else {
            try {
                // the passed in URI should be a valid URL - verify that that is the case
                uri.toURL();
            } catch (MalformedURLException e) {
                throw new GenericLRAException(null, NOT_ACCEPTABLE.getStatusCode(),
                        String.format(message, e.getMessage()) + " uri=" + uri, e);
            }
        }
    }

    private boolean isExpectedResponseStatus(Response response, Response.Status... expected) {
        for (Response.Status anExpected : expected) {
            if (response.getStatus() == anExpected.getStatusCode()) {
                return true;
            }
        }
        return false;
    }

    public String getUrl() {
        return base.toString();
    }

    public URI getCurrent() {
        return Current.peek();
    }

    private void lraTracef(String reasonFormat, Object... parameters) {
        if (!LRALogger.logger.isTraceEnabled()) {
            return;
        }

        LRALogger.logger.tracef(reasonFormat, parameters);
    }

    private void lraTrace(URI lra, String reason) {
        lraTracef(lra, reason, (Object[]) null);
    }

    private void lraTracef(URI lra, String reasonFormat, Object... parameters) {
        Object[] newParams;
        if (parameters != null) {
            newParams = Arrays.copyOf(parameters, parameters.length + 1);
        } else {
            newParams = new Object[1];
        }
        newParams[newParams.length - 1] = lra;
        lraTracef(reasonFormat + ", lra id: %s", newParams);
    }

    public void close() {
        client.close();
        if (responseDataMap != null) {
            responseDataMap.clear();
        }
    }

    private void aquireConnection() {
        if (connectionInUse) {
            LRALogger.i18NLogger.error_cannotAquireInUseConnection();

            throw new GenericLRAException(null, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    "LRAClient: trying to aquire an in use connection", null);
        }

        connectionInUse = true;
    }

    private void releaseConnection(Response response) {
        if (response != null) {
            response.close();
        }

        connectionInUse = false;
    }

    private void setResponseData(URI lraId, String responseData) {
        responseDataMap.put(lraId, responseData);
/*
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
*/
    }

    public String getResponseData(URL lraId) {
        return responseDataMap.containsKey(lraId) ? responseDataMap.get(lraId) : null;
    }
}
