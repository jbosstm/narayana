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

import io.narayana.lra.Current;
import io.narayana.lra.LRAConstants;
import io.narayana.lra.LRAData;
import io.narayana.lra.logging.LRALogger;
import org.eclipse.microprofile.lra.annotation.AfterLRA;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.Forget;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.eclipse.microprofile.lra.annotation.Status;
import org.eclipse.microprofile.lra.annotation.ws.rs.Leave;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.Closeable;
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
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.narayana.lra.LRAConstants.AFTER;
import static io.narayana.lra.LRAConstants.CLIENT_ID_PARAM_NAME;
import static io.narayana.lra.LRAConstants.COMPENSATE;
import static io.narayana.lra.LRAConstants.COMPLETE;
import static io.narayana.lra.LRAConstants.COORDINATOR_PATH_NAME;
import static io.narayana.lra.LRAConstants.FORGET;
import static io.narayana.lra.LRAConstants.LEAVE;
import static io.narayana.lra.LRAConstants.PARENT_LRA_PARAM_NAME;
import static io.narayana.lra.LRAConstants.STATUS;
import static io.narayana.lra.LRAConstants.TIMELIMIT_PARAM_NAME;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE;
import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;
import static javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
import static javax.ws.rs.core.Response.Status.GONE;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_RECOVERY_HEADER;

/**
 * A utility class for controlling the lifecycle of Long Running Actions (LRAs) but the prefered mechanism is to use
 * the annotation in the {@link org.eclipse.microprofile.lra.annotation} package
 */
@RequestScoped
public class NarayanaLRAClient implements Closeable {
    /**
     * Key for looking up the config property that specifies which host a
     * coordinator is running on
     */
    public static final String LRA_COORDINATOR_HOST_KEY = "lra.http.host";

    /**
     * Key for looking up the config property that specifies which port a
     * coordinator is running on
     */
    public static String LRA_COORDINATOR_PORT_KEY = "lra.http.port";

    /**
     * Key for looking up the config property that specifies which JAX-RS path a
     * coordinator is running on
     */
    public static String LRA_COORDINATOR_PATH_KEY = "lra.coordinator.path";

    public static final long DEFAULT_TIMEOUT_MILLIS = 0L;

    // LRA Coordinator API
    private static final String START_PATH = "/start";
    private static final String LEAVE_PATH = "/%s/remove";
    private static final String STATUS_PATH = "/%s/status";
    private static final String CLOSE_PATH = "/%s/close";
    private static final String CANCEL_PATH = "/%s/cancel";

    private static final String LINK_TEXT = "Link";

    private static URI defaultCoordinatorURI;
    private URI base;

    public static void setDefaultCoordinatorEndpoint(URI lraCoordinatorEndpoint) {
        defaultCoordinatorURI = lraCoordinatorEndpoint;
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
                init(defaultCoordinatorURI);
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

    private void init(URI coordinatorUri) {
        setCoordinatorURI(coordinatorUri);
    }

    private void setCoordinatorURI(URI uri) {
        base = uri;
    }

    private void init(String scheme, String host, int port) throws URISyntaxException {
        setCoordinatorURI(new URI(scheme, null, host, port, "/" + COORDINATOR_PATH_NAME, null, null));
    }

    public void setCurrentLRA(URI coordinatorUri) {
        try {
            init(LRAConstants.getLRACoordinatorUri(coordinatorUri));
        } catch (IllegalStateException e) {
            LRALogger.i18NLogger.error_invalidCoordinatorId(coordinatorUri.toASCIIString(), e);
            throwGenericLRAException(coordinatorUri, BAD_REQUEST.getStatusCode(), e.getMessage());
        }
    }

    public List<LRAData> getAllLRAs() {
        Client client = null;
        try {
            client = getClient();
            Response response = client.target(base)
                .request()
                .get();

            if (response.getStatus() != OK.getStatusCode()) {
                LRALogger.logger.debugf("Error getting all LRAs from the coordinator, response status: %d", response.getStatus());
                throw new WebApplicationException(response);
            }

            return response.readEntity(new GenericType<List<LRAData>>() {});
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    /**
     * Starting LRA. You provide client id determining the LRA being started.
     *
     * @param clientID  client id determining the LRA
     * @return  LRA id as URL
     * @throws WebApplicationException  thrown when start of the LRA failed
     */
    public URI startLRA(String clientID) throws WebApplicationException {
        return startLRA(clientID, 0L);
    }

    /**
     * Starting LRA. You provide client id that joins the LRA context
     * and is passed when working with the LRA.
     *
     * @param clientID  client id determining the LRA
     * @param timeout  timeout value in seconds, when timeouted the LRA will be compensated
     * @return  LRA id as URL
     * @throws WebApplicationException  thrown when start of the LRA failed
     */
    private URI startLRA(String clientID, Long timeout) throws WebApplicationException {
        return startLRA(clientID, timeout, ChronoUnit.SECONDS);
    }

    private URI startLRA(String clientID, Long timeout, ChronoUnit unit) throws WebApplicationException {
        return startLRA(getCurrent(), clientID, timeout, unit);
    }

    public URI startLRA(URI parentLRA, String clientID, Long timeout, ChronoUnit unit) throws WebApplicationException {
        Client client = null;
        Response response = null;
        URI lra;

        if (clientID == null) {
            clientID = "";
        }

        if (timeout == null) {
            timeout = 0L;
        } else if (timeout < 0) {
            throwGenericLRAException(parentLRA, BAD_REQUEST.getStatusCode(),
                    "Invalid timeout value: " + timeout);
            return null;
        }

        lraTracef("startLRA for client %s with parent %s", clientID, parentLRA);

        try {
            String encodedParentLRA = parentLRA == null ? "" : URLEncoder.encode(parentLRA.toString(), "UTF-8");

            client = getClient();

            response = client.target(base)
                .path(START_PATH)
                .queryParam(CLIENT_ID_PARAM_NAME, client)
                .queryParam(TIMELIMIT_PARAM_NAME, Duration.of(timeout, unit).toMillis())
                .queryParam(PARENT_LRA_PARAM_NAME, encodedParentLRA)
                .request()
                .post(null);

            // validate the HTTP status code says an LRA resource was created
            if (isUnexpectedResponseStatus(response, Response.Status.CREATED)) {
                LRALogger.i18NLogger.error_lraCreationUnexpectedStatus(response.getStatus(), response.toString());
                throwGenericLRAException(null, INTERNAL_SERVER_ERROR.getStatusCode(),
                        "LRA start returned an unexpected status code: " + response.getStatus());
                return null;
            }

            lra = URI.create(response.getHeaderString(HttpHeaders.LOCATION));

            if (lra == null) {
                LRALogger.i18NLogger.error_nullLraOnCreation(response.toString());
                throwGenericLRAException(null, INTERNAL_SERVER_ERROR.getStatusCode(), "LRA creation is null");
                return null;
            }

            lraTrace(lra, "startLRA returned");

            Current.push(lra);

        } catch (UnsupportedEncodingException e) {
            LRALogger.i18NLogger.error_cannotCreateUrlFromLCoordinatorResponse(response.toString(), e);
            throwGenericLRAException(null, INTERNAL_SERVER_ERROR.getStatusCode(), e.getMessage());
            return null;
        } catch (Exception e) {
            LRALogger.i18NLogger.error_cannotContactLRACoordinator(base, e);

            if (e.getCause() != null && ConnectException.class.equals(e.getCause().getClass())) {
                throwGenericLRAException(null, SERVICE_UNAVAILABLE.getStatusCode(),
                        "Cannot connect to the LRA coordinator: " + base + " (" + e.getCause().getMessage() + ")");
            } else {
                throwGenericLRAException(null, Response.Status.SERVICE_UNAVAILABLE.getStatusCode(), e.getMessage());
            }

            return null;
        } finally {
            if (client != null) {
                client.close();
            }
        }

        // check that the lra is active
        // isActiveLRA(lra);
        return lra;
    }

    public void cancelLRA(URI lraId) throws WebApplicationException {
        endLRA(lraId, false);
    }

    public void closeLRA(URI lraId) throws WebApplicationException {
        endLRA(lraId, true);
    }

    /**
     * @param lraId the URI of the LRA to join
     * @param timelimit how long the participant is prepared to wait for LRA completion
     * @param compensateUri URI for compensation notifications
     * @param completeUri URI for completion notifications
     * @param forgetUri URI for forget callback
     * @param leaveUri URI for leave requests
     * @param statusUri URI for reporting the status of the participant
     * @param compensatorData data provided during compensation
     * @return a recovery URL for this enlistment
     * @throws WebApplicationException if the LRA coordinator failed to enlist the participant
     */
    public URI joinLRA(URI lraId, Long timelimit,
                       URI compensateUri, URI completeUri, URI forgetUri, URI leaveUri, URI afterUri, URI statusUri,
                       String compensatorData) throws WebApplicationException {
        return enlistCompensator(lraId, timelimit, "",
                compensateUri, completeUri,
                forgetUri, leaveUri, afterUri, statusUri,
                compensatorData);
    }

    public void leaveLRA(URI lraId, String body) throws WebApplicationException {
        Client client = null;
        Response response = null;

        try {
            client = getClient();

            response = client.target(base)
                .path(String.format(LEAVE_PATH, LRAConstants.getLRAId(lraId)))
                .request()
                .put(Entity.text(body));

            if (OK.getStatusCode() != response.getStatus()) {
                LRALogger.i18NLogger.error_lraLeaveUnexpectedStatus(response.getStatus(), response.toString());
                throwGenericLRAException(null, response.getStatus(), "");
            }
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    /**
     * For particular compensator class it returns termination uris based on the provided base uri.
     * You get map of string and URI.
     *
     * @param compensatorClass  compensator class to examine
     * @param uriInfo  the uri that triggered this join request.
     * @return map of URI
     */
    public static Map<String, String> getTerminationUris(Class<?> compensatorClass, UriInfo uriInfo, Long timeout) {
        Map<String, String> paths = new HashMap<>();
        final boolean[] asyncTermination = {false};
        URI baseUri = uriInfo.getBaseUri();

        /*
         * Calculate which path to prepend to the LRA participant methods. If there is more than one matching URI
         * then the second matched URI comes from either the class level Path annotation or from a sub-resource locator.
         * In both cases the second matched URI can be used as a prefix for the LRA participant URIs:
         */
        List<String> matchedURIs = uriInfo.getMatchedURIs();
        int matchedURI = (matchedURIs.size() > 1 ? 1 : 0);
        final String uriPrefix = baseUri + matchedURIs.get(matchedURI);

        String timeoutValue = timeout != null ? Long.toString(timeout) : "0";

        Arrays.stream(compensatorClass.getMethods()).forEach(method -> {
            Path pathAnnotation = method.getAnnotation(Path.class);

            if (pathAnnotation != null) {

                if (checkMethod(paths, method, COMPENSATE, pathAnnotation,
                        method.getAnnotation(Compensate.class), uriPrefix) != 0) {
                    paths.put(TIMELIMIT_PARAM_NAME, timeoutValue);

                    if (isAsyncCompletion(method)) {
                        asyncTermination[0] = true;
                    }
                }

                if (checkMethod(paths, method, COMPLETE, pathAnnotation,
                        method.getAnnotation(Complete.class), uriPrefix) != 0) {
                    paths.put(TIMELIMIT_PARAM_NAME, timeoutValue);

                    if (isAsyncCompletion(method)) {
                        asyncTermination[0] = true;
                    }
                }
                checkMethod(paths, method, STATUS, pathAnnotation,
                        method.getAnnotation(Status.class), uriPrefix);
                checkMethod(paths, method, FORGET, pathAnnotation,
                        method.getAnnotation(Forget.class), uriPrefix);

                checkMethod(paths, method, LEAVE, pathAnnotation, method.getAnnotation(Leave.class), uriPrefix);
                checkMethod(paths, method, AFTER, pathAnnotation, method.getAnnotation(AfterLRA.class), uriPrefix);
            }
        });

        if (asyncTermination[0] && !paths.containsKey(STATUS) && !paths.containsKey(FORGET)) {
            LRALogger.i18NLogger.error_asyncTerminationBeanMissStatusAndForget(compensatorClass);

            throw new WebApplicationException(
                    Response.status(BAD_REQUEST)
                            .entity("LRA participant class with asynchronous temination but no @Status or @Forget annotations")
                            .build());
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
                                   Method method, String rel,
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

        // search for a matching JAX-RS method
        for (Annotation annotation : method.getDeclaredAnnotations()) {
            String name = annotation.annotationType().getName();

            if (name.equals(GET.class.getName()) ||
                    name.equals(PUT.class.getName()) ||
                    name.equals(POST.class.getName()) ||
                    name.equals(DELETE.class.getName())) {
                String pathValue = pathAnnotation.value();
                pathValue = pathValue.startsWith("/") ? pathValue : "/" + pathValue;
                String url = String.format("%s%s?%s=%s", uriPrefix, pathValue, LRAConstants.HTTP_METHOD_NAME, name);

                paths.put(rel, url);
                break;
            }
        }

        return 1;
    }

    public LRAStatus getStatus(URI uri) throws WebApplicationException {
        Client client = null;
        Response response = null;
        URL lraId;

        try {
            lraId = uri.toURL();
        } catch (MalformedURLException e) {
            throwGenericLRAException(null,
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    "Could not convert LRA to a URL: " + e.getMessage()
            );
            return null;
        }

        try {
            client = getClient();
            response = client.target(base)
                .path(String.format(STATUS_PATH, LRAConstants.getLRAId(uri)))
                .request()
                .get();

            if (response.getStatus() == NOT_FOUND.getStatusCode()) {
                String responseContent = response.readEntity(String.class);
                String errorMsg = "The requested LRA it '" + lraId + "' was not found and the status can't be obtained"
                    + (responseContent != null ? ": " + responseContent : "");
                throw new NotFoundException(errorMsg, Response.status(NOT_FOUND).entity(errorMsg).build());
            }

            if (response.getStatus() == NO_CONTENT.getStatusCode()) {
                return LRAStatus.Active;
            }

            if (response.getStatus() != OK.getStatusCode()) {
                LRALogger.i18NLogger.error_invalidStatusCode(base, response.getStatus(), lraId);
                throwGenericLRAException(uri,
                    response.getStatus(),
                    "LRA coordinator returned an invalid status code"
                );
            }

            if (!response.hasEntity()) {
                LRALogger.i18NLogger.error_noContentOnGetStatus(base, lraId);
                throwGenericLRAException(uri,
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    "LRA coordinator#getStatus returned 200 OK but no content: lra: " + lraId);
            }

            // convert the returned String into a status
            try {
                return fromString(response.readEntity(String.class));
            } catch (IllegalArgumentException e) {
                LRALogger.i18NLogger.error_invalidArgumentOnStatusFromCoordinator(base, lraId, e);
                throwGenericLRAException(uri,
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    "LRA coordinator returned an invalid status"
                );
                return null;
            }
        } finally {
            if (client != null) {
                client.close();
            }
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
                                  URI forgetUri, URI leaveUri, URI afterUri, URI statusUri,
                                  String compensatorData) {
        validateURI(completeUri, true, "Invalid complete URL: %s");
        validateURI(compensateUri, true, "Invalid compensate URL: %s");
        validateURI(leaveUri, true, "Invalid status URL: %s");
        validateURI(afterUri, true, "Invalid after URL: %s");
        validateURI(forgetUri, true, "Invalid forgetUri URL: %s");
        validateURI(statusUri, true, "Invalid status URL: %s");

        Map<String, URI> terminateURIs = new HashMap<>();

        terminateURIs.put(COMPENSATE, compensateUri);
        terminateURIs.put(COMPLETE, completeUri);
        terminateURIs.put(LEAVE, leaveUri);
        terminateURIs.put(AFTER, afterUri);
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
        Client client = null;
        Response response = null;
        String responseEntity = null;
        URL lraId;

        try {
            lraId = uri.toURL();
        } catch (MalformedURLException e) {
            throwGenericLRAException(null,
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    "Could not convert LRA to a URL: " + e.getMessage()
            );
            return null;
        }
        if (timelimit < 0) {
            timelimit = 0L;
        }

        try {
            client = getClient();
            response = client.target(base)
                .path(LRAConstants.getLRAId(uri))
                .queryParam(TIMELIMIT_PARAM_NAME, timelimit)
                .request()
                .header("Link", linkHeader)
                .put(Entity.text(compensatorData == null ? linkHeader : compensatorData));
        } catch (WebApplicationException webApplicationException) {
            throw new WebApplicationException(uri.toASCIIString(), GONE);
        } finally {
            if (client != null) {
                client.close();
            }
        }

        if (response.getStatus() == Response.Status.PRECONDITION_FAILED.getStatusCode()) {
            LRALogger.i18NLogger.error_tooLateToJoin(lraId, response.toString());
            String errorMsg = lraId + ": Too late to join with this LRA";
            throw new WebApplicationException(errorMsg,
                    Response.status(PRECONDITION_FAILED).entity(errorMsg).build());
        } else if (response.getStatus() == NOT_FOUND.getStatusCode()) {
            LRALogger.i18NLogger.info_failedToEnlistingLRANotFound(
                    lraId, base, NOT_FOUND.getStatusCode(), NOT_FOUND.getReasonPhrase(), GONE.getStatusCode(), GONE.getReasonPhrase());
            throw new WebApplicationException(uri.toASCIIString(),
                    Response.status(GONE).entity(uri.toASCIIString()).build());
        } else if (response.getStatus() != OK.getStatusCode()) {
            LRALogger.i18NLogger.error_failedToEnlist(lraId, base, response.getStatus());

            throwGenericLRAException(uri, response.getStatus(),
                    "unable to register participant");
        }

        try {
            String recoveryUrl = response.getHeaderString(LRA_HTTP_RECOVERY_HEADER); //readEntity(String.class).replaceAll("^\"|\"$", "");
            String url = URLDecoder.decode(recoveryUrl, "UTF-8");
            return new URI(url);
        } catch (URISyntaxException | UnsupportedEncodingException e) {
            LRALogger.logger.infof("join %s returned an invalid recovery URI: %s", lraId, responseEntity);
            throwGenericLRAException(null, Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
                    "join " + lraId + " returned an invalid recovery URI: " + responseEntity);
            return null;
        }
    }

    private void endLRA(URI lra, boolean confirm) throws WebApplicationException {
        Client client = null;
        Response response = null;

        lraTracef(lra, "%s LRA", confirm ? "close" : "compensate");

        try {
            client = getClient();
            String lraId = LRAConstants.getLRAId(lra);
            response = client.target(base)
                .path(confirm ? String.format(CLOSE_PATH, lraId) : String.format(CANCEL_PATH, lraId))
                .request()
                .put(null);

            if (isUnexpectedResponseStatus(response, OK, Response.Status.ACCEPTED, NOT_FOUND)) {
                LRALogger.i18NLogger.error_lraTerminationUnexpectedStatus(response.getStatus(), response.toString());
                throwGenericLRAException(lra, INTERNAL_SERVER_ERROR.getStatusCode(),
                        "LRA finished with an unexpected status code: " + response.getStatus());
            }

            if (response.getStatus() == NOT_FOUND.getStatusCode()) {
                String errorMsg = LRALogger.i18NLogger.get_couldNotCompleteCompensateOnReturnedStatus(
                        confirm ? "close" : "compensate", lra, base, NOT_FOUND.getReasonPhrase());
                LRALogger.logger.info(errorMsg);
                throw new NotFoundException(errorMsg,
                        Response.status(NOT_FOUND).entity(lra.toASCIIString()).build());
            }

        } finally {
            Current.pop(lra);

            if (client != null) {
                client.close();
            }
        }
    }

    private void validateURI(URI uri, boolean nullAllowed, String message) {
        if (uri == null) {
            if (!nullAllowed) {
                throwGenericLRAException(null, NOT_ACCEPTABLE.getStatusCode(),
                        String.format(message, "null value"));
            }
        } else {
            try {
                // the passed in URI should be a valid URL - verify that that is the case
                uri.toURL();
            } catch (MalformedURLException e) {
                throwGenericLRAException(null, NOT_ACCEPTABLE.getStatusCode(),
                        String.format(message, e.getMessage()) + " uri=" + uri);
            }
        }
    }

    private boolean isUnexpectedResponseStatus(Response response, Response.Status... expected) {
        for (Response.Status anExpected : expected) {
            if (response.getStatus() == anExpected.getStatusCode()) {
                return false;
            }
        }
        return true;
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
    }

    private void throwGenericLRAException(URI lraId, int statusCode, String message) throws WebApplicationException {
        String errorMsg = String.format("%s: %s", lraId, message);
        throw new WebApplicationException(errorMsg, Response.status(statusCode)
                .entity(errorMsg).build());
    }

    private Client getClient() {
        return ClientBuilder.newClient();
    }
}
