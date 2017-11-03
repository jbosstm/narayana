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
package io.narayana.lra.filter;

import io.narayana.lra.annotation.LRA;
import io.narayana.lra.annotation.Compensate;
import io.narayana.lra.annotation.Complete;
import io.narayana.lra.annotation.Leave;
import io.narayana.lra.annotation.NestedLRA;
import io.narayana.lra.annotation.Status;
import io.narayana.lra.annotation.Forget;
import io.narayana.lra.annotation.TimeLimit;
import io.narayana.lra.client.Current;
import io.narayana.lra.client.GenericLRAException;
import io.narayana.lra.client.IllegalLRAStateException;
import io.narayana.lra.client.NarayanaLRAClient;
import io.narayana.lra.logging.LRALogger;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.narayana.lra.client.NarayanaLRAClient.COMPENSATE;
import static io.narayana.lra.client.NarayanaLRAClient.COMPLETE;
import static io.narayana.lra.client.NarayanaLRAClient.LEAVE;
import static io.narayana.lra.client.NarayanaLRAClient.LRA_HTTP_HEADER;
import static io.narayana.lra.client.NarayanaLRAClient.LRA_HTTP_RECOVERY_HEADER;
import static io.narayana.lra.client.NarayanaLRAClient.STATUS;
import static io.narayana.lra.client.NarayanaLRAClient.FORGET;

@Provider
public class ServerLRAFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String CANCEL_ON_FAMILY_PROP = "CancelOnFamily";
    private static final String CANCEL_ON_PROP = "CancelOn";
    private static final String TERMINAL_LRA_PROP = "terminateLRA";

    @Context
    protected ResourceInfo resourceInfo;

    @Inject
    private NarayanaLRAClient lraClient;

    private void checkForTx(LRA.Type type, URL lraId, boolean shouldNotBeNull) {
        if (lraId == null && shouldNotBeNull) {
            throw new GenericLRAException(Response.Status.PRECONDITION_FAILED.getStatusCode(),
                    type.name() + " but no tx");
        } else if (lraId != null && !shouldNotBeNull) {
            throw new GenericLRAException(lraId, Response.Status.PRECONDITION_FAILED.getStatusCode(),
                    type.name() + " but found tx");
        }
    }

//    // TODO figure out how to disable the filters for the coordinator since they are required
//    private boolean isCoordinator() {
//        return resourceInfo.getResourceClass().getName().equals("io.narayana.lra.coordinator.api.Coordinator")
//    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        Method method = resourceInfo.getResourceMethod();
        MultivaluedMap<String, String> headers = containerRequestContext.getHeaders();
        LRA.Type type = null;
        Annotation transactional = method.getDeclaredAnnotation(LRA.class);
        URL lraId;
        URL newLRA = null;

        URL suspendedLRA = null;
        URL incommingLRA = null;
        String recoveryUrl = null;
        boolean nested;
        boolean isLongRunning = false;
        boolean enlist;

        if (transactional == null)
            transactional = method.getDeclaringClass().getDeclaredAnnotation(LRA.class);

        if (transactional != null) {
            type = ((LRA) transactional).value();
            isLongRunning = ((LRA) transactional).delayClose();
            Response.Status.Family[] cancel0nFamily = ((LRA) transactional).cancelOnFamily();
            Response.Status[] cancel0n = ((LRA) transactional).cancelOn();

            if (((LRA) transactional).terminal())
                containerRequestContext.setProperty(TERMINAL_LRA_PROP, Boolean.TRUE);

            if (cancel0nFamily.length != 0)
                containerRequestContext.setProperty(CANCEL_ON_FAMILY_PROP, cancel0nFamily);

            if (cancel0n.length != 0)
                containerRequestContext.setProperty(CANCEL_ON_PROP, cancel0n);
        }

        boolean endAnnotation = method.isAnnotationPresent(Complete.class)
                || method.isAnnotationPresent(Compensate.class)
                || method.isAnnotationPresent(Leave.class)
                || method.isAnnotationPresent(Status.class)
                || method.isAnnotationPresent(Forget.class);

        if (type == null) {
            if(!endAnnotation)
                Current.clearContext(headers);

            return; // not transactional
        }

        if (headers.containsKey(LRA_HTTP_HEADER))
            incommingLRA = new URL(headers.getFirst(LRA_HTTP_HEADER)); // TODO filters for asynchronous JAX-RS motheods should not throw exceptions

        if (endAnnotation && incommingLRA == null)
            return;

        enlist = ((LRA) transactional).join();
        nested = resourceInfo.getResourceMethod().isAnnotationPresent(NestedLRA.class);

        switch (type) {
            case MANDATORY: // a txn must be present
                checkForTx(type, incommingLRA, true);

                if (nested) {
                    // a new LRA is nested under the incomming LRA
                    suspendedLRA = incommingLRA;
                    lraTrace(containerRequestContext, suspendedLRA, "ServerLRAFilter before: MANDATORY start new LRA");
                    newLRA = lraId = startLRA(incommingLRA, method, getTimeOut(method));
                } else {
                    lraId = incommingLRA;
                    resumeTransaction(incommingLRA); // txId is not null
                }
                break;
            case NEVER: // a txn must not be present
                checkForTx(type, incommingLRA, false);

                if (nested) {
                    // nested does not make sense
                    throw new GenericLRAException(Response.Status.PRECONDITION_FAILED.getStatusCode(),
                            type.name() + " but found Nested annnotation");
                }

                enlist = false;
                lraId = null;

                break;
            case NOT_SUPPORTED:
                if (nested) {
                    // nested does not make sense
                    throw new GenericLRAException(Response.Status.PRECONDITION_FAILED.getStatusCode(),
                            type.name() + " but found Nested annnotation");
                }

                enlist = false;
                suspendedLRA = incommingLRA;
                lraId = null;

                break;
            case REQUIRED:
                if (incommingLRA != null) {
                    if (nested) {
                        // if there is an LRA present nest a new LRA under it
                        suspendedLRA = incommingLRA;
                        lraTrace(containerRequestContext, suspendedLRA, "ServerLRAFilter before: REQUIRED start new LRA");
                        newLRA = lraId = startLRA(incommingLRA, method, getTimeOut(method));
                    } else {
                        lraId = incommingLRA;
                        resumeTransaction(incommingLRA);
                    }

                } else {
                    lraTrace(containerRequestContext, null, "ServerLRAFilter before: REQUIRED start new LRA");
                    newLRA = lraId = startLRA(null, method, getTimeOut(method));
                }

                break;
            case REQUIRES_NEW:
//                    previous = AtomicAction.suspend();
                suspendedLRA = incommingLRA;
                lraTrace(containerRequestContext, suspendedLRA, "ServerLRAFilter before: REQUIRES_NEW start new LRA");
                newLRA = lraId = startLRA(incommingLRA, method, getTimeOut(method));

                break;
            case SUPPORTS:
                lraId = incommingLRA;

                if (nested) {
                    // if there is an LRA present a new LRA is nested under it otherwise a new top level LRA is begun
                    if (incommingLRA != null)
                        suspendedLRA = incommingLRA;

                    lraTrace(containerRequestContext, incommingLRA, "ServerLRAFilter before: SUPPORTS start new LRA");
                    newLRA = lraId = startLRA(incommingLRA, method, getTimeOut(method));
                } else if (incommingLRA != null) {
                    resumeTransaction(incommingLRA);
                }

                break;
            default:
                lraId = incommingLRA;
        }

        if (lraId == null) {
            lraTrace(containerRequestContext, null, "ServerLRAFilter before: removing header");
            // the method call needs to run without a transaction
            Current.clearContext(headers);
            return; // non transactional
        } else {
            lraTrace(containerRequestContext, lraId, "ServerLRAFilter before: adding header");

            if (lraId.toExternalForm().contains("recovery-coordi"))
                lraWarn(containerRequestContext, lraId, "wrong lra id");
        }

        if (isLongRunning)
            newLRA = null;

        // store state with the current thread. TODO for the async version use containerRequestContext.setProperty("lra", Current.peek());
        Current.updateLRAContext(lraId, headers); // make the current LRA available to the called method

        if (newLRA != null) {
            if (suspendedLRA != null)
                Current.putState("suspendedLRA", suspendedLRA);

            Current.putState("newLRA", newLRA);
        }

        lraTrace(containerRequestContext, lraId, "ServerLRAFilter before: making LRA available to injected LRAClient");
        lraClient.setCurrentLRA(lraId); // make the current LRA available to the called method

        // TODO make sure it is possible to do compensations inside a new LRA
        if (!endAnnotation && enlist) { // don't enlist for methods marked with Compensate, Complete or Leave
            URI baseUri = containerRequestContext.getUriInfo().getBaseUri();

            Map<String, String> terminateURIs = NarayanaLRAClient.getTerminationUris(resourceInfo.getResourceClass(), baseUri);
            String timeLimitStr = terminateURIs.get(NarayanaLRAClient.TIMELIMIT_PARAM_NAME);
            long timeLimit = timeLimitStr == null ? NarayanaLRAClient.DEFAULT_TIMEOUT_MILLIS : Long.valueOf(timeLimitStr);

            if (terminateURIs.containsKey("Link")) {
                try {
                    recoveryUrl = lraClient.joinLRA(lraId, timeLimit,
                            toURL(terminateURIs.get(COMPENSATE)),
                            toURL(terminateURIs.get(COMPLETE)),
                            toURL(terminateURIs.get(FORGET)),
                            toURL(terminateURIs.get(LEAVE)),
                            toURL(terminateURIs.get(STATUS)),
                            null);
                } catch (IllegalLRAStateException e) {
                    lraTrace(containerRequestContext, lraId, "ServerLRAFilter before: aborting with " + e.getMessage());
                    throw e;
                } catch (WebApplicationException e) {
                    lraTrace(containerRequestContext, lraId, "ServerLRAFilter before: aborting with " + e.getMessage());
                    throw new GenericLRAException(lraId, e.getResponse().getStatus(), e.getMessage(), e);
                }

                headers.putSingle(LRA_HTTP_RECOVERY_HEADER, recoveryUrl);
            } else {
                lraTrace(containerRequestContext, lraId,
                        "ServerLRAFilter: skipping resource " + method.getDeclaringClass().getName() + " - no participant annotations");
            }
        }

        if (method.isAnnotationPresent(Leave.class)) {
            // leave the LRA
            String compensatorId = getCompensatorId(lraId, containerRequestContext.getUriInfo().getBaseUri());

            lraTrace(containerRequestContext, lraId, "leaving LRA");
            lraClient.leaveLRA(lraId, compensatorId);

            // let the participant know which lra he left by leaving the header intact
        }

        lraTrace(containerRequestContext, lraId, "ServerLRAFilter before: making LRA available as a thread local");
    }

    private URL toURL(String url) throws MalformedURLException {
        return url == null ? null : new URL(url);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        // a request is leaving the container so clear any context on the thread and fix up the LRA response header

        Object suspendedLRA = Current.getState("suspendedLRA");
        Object newLRA = Current.getState("newLRA");
        URL current = Current.peek();

        try {
            if (current != null) {
                int status = responseContext.getStatus();
                Response.Status.Family[] cancel0nFamily = (Response.Status.Family[]) requestContext.getProperty(CANCEL_ON_FAMILY_PROP);
                Response.Status[] cancel0n = (Response.Status[]) requestContext.getProperty(CANCEL_ON_PROP);
                Boolean closeCurrent = (Boolean) requestContext.getProperty(TERMINAL_LRA_PROP);

                if (closeCurrent == null)
                    closeCurrent = false;

                if (cancel0nFamily != null)
                    if (Arrays.stream(cancel0nFamily).anyMatch(f -> Response.Status.Family.familyOf(status) == f))
                        closeCurrent = true;

                if (cancel0n != null && !closeCurrent)
                    if (Arrays.stream(cancel0n).anyMatch(f -> status == f.getStatusCode()))
                        closeCurrent = true;

                if (closeCurrent) {
                    lraTrace(requestContext, (URL) newLRA, "ServerLRAFilter after: closing LRA becasue http status is " + status);
                    lraClient.cancelLRA(current);

                    if (current.equals(newLRA))
                        newLRA = null; // don't try to cancle newKRA twice
                }
            }

            if (newLRA != null) {
                lraTrace(requestContext, (URL) newLRA, "ServerLRAFilter after: closing LRA");
                lraClient.closeLRA((URL) newLRA);
            }

            if (responseContext.getStatus() == Response.Status.OK.getStatusCode() &&
                    NarayanaLRAClient.isAsyncCompletion(resourceInfo.getResourceMethod())) {
                LRALogger.i18NLogger.warn_lraParticipantqForAsync(
                        resourceInfo.getResourceMethod().getDeclaringClass().getName(),
                        resourceInfo.getResourceMethod().getName(),
                        Response.Status.ACCEPTED.getStatusCode(),
                        Response.Status.OK.getStatusCode());
            }
        } finally {
            if (suspendedLRA != null)
                Current.push((URL) suspendedLRA);

            Current.updateLRAContext(responseContext.getHeaders());

            Current.popAll();
        }
    }

    private long getTimeOut(Method method) {
        Annotation timeLimit = method.getDeclaredAnnotation(TimeLimit.class);

        if (timeLimit == null)
            timeLimit = method.getDeclaringClass().getDeclaredAnnotation(TimeLimit.class);

        if (timeLimit == null)
            return NarayanaLRAClient.DEFAULT_TIMEOUT_MILLIS;

        TimeLimit tl = (TimeLimit) timeLimit;

        return tl.unit().toMillis(tl.limit());
    }

    private URL startLRA(URL parentLRA, Method method, long timeout) {
        // timeout should already have been converted to milliseconds
        String clientId = method.getDeclaringClass().getName() +"#" + method.getName();

        return lraClient.startLRA(parentLRA, clientId, timeout, TimeUnit.MILLISECONDS);
    }

    private void resumeTransaction(URL lraId) {
        // nothing to do
    }

    private String getCompensatorId(URL lraId, URI baseUri) {
        Map<String, String> terminateURIs = NarayanaLRAClient.getTerminationUris(resourceInfo.getResourceClass(), baseUri);

        if (!terminateURIs.containsKey("Link"))
            throw new GenericLRAException(lraId, Response.Status.BAD_REQUEST.getStatusCode(),
                    "Missing complete or compensate annotations", null);

        return terminateURIs.get("Link");
    }

    private void lraTrace(ContainerRequestContext context, URL lraId, String reason) {
        if (LRALogger.logger.isTraceEnabled()) {
            Method method = resourceInfo.getResourceMethod();
            LRALogger.logger.tracef("%s: container request for method %s: lra: %s%n",
                    reason, method.getDeclaringClass().getName() + "#" + method.getName(),
                    lraId == null ? "context" : lraId);
        }
    }

    private void lraWarn(ContainerRequestContext context, URL lraId, String reason) {
        Method method = resourceInfo.getResourceMethod();
        LRALogger.i18NLogger.warn_lraFilterContainerRequest(reason,
            method.getDeclaringClass().getName() + "#" + method.getName(), lraId == null ? "context" : lraId.toString());
    }
}
