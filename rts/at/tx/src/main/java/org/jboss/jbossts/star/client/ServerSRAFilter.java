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
package org.jboss.jbossts.star.client;

import org.jboss.jbossts.star.annotation.Commit;
import org.jboss.jbossts.star.annotation.OnePhaseCommit;
import org.jboss.jbossts.star.annotation.Prepare;
import org.jboss.jbossts.star.annotation.Rollback;
import org.jboss.jbossts.star.annotation.SRA;
import org.jboss.jbossts.star.annotation.Status;
import org.jboss.jbossts.star.annotation.TimeLimit;
import org.jboss.logging.Logger;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.jboss.jbossts.star.client.SRAClient.RTS_HTTP_RECOVERY_HEADER;
import static org.jboss.jbossts.star.client.SRAClient.RTS_HTTP_CONTEXT_HEADER;

@Provider
public class ServerSRAFilter implements ContainerRequestFilter, ContainerResponseFilter {
    protected static final Logger logger = Logger.getLogger(ServerSRAFilter.class);

    private SRAClient sraClient;

    private static final String CANCEL_ON_FAMILY_PROP = "CancelOnFamily";
    private static final String CANCEL_ON_PROP = "CancelOn";
    private static final String SUSPENDED_LRA_PROP = "suspendLRA";
    private static final String TERMINAL_LRA_PROP = "terminateLRA";

    private static final Boolean isTrace = Boolean.getBoolean("trace");

    @Context
    protected ResourceInfo resourceInfo;

//    private AtomicAction previous = null;

    private void checkForTx(SRA.Type type, URL lraId, boolean shouldNotBeNull) {
        if (lraId == null && shouldNotBeNull) {
            throw new GenericSRAException(null, Response.Status.PRECONDITION_FAILED.getStatusCode(),
                    type.name() + " but no tx", null);
        } else if (lraId != null && !shouldNotBeNull) {
            throw new GenericSRAException(lraId, Response.Status.PRECONDITION_FAILED.getStatusCode(),
                    type.name() + " but found tx", null);
        }
    }

//    // TODO figure out how to disable the filters for the coordinator (they remove the
//    private boolean isCoordinator() {
//        return resourceInfo.getResourceClass().getName().equals("org.jboss.narayana.rts.lra.coordinator.io.narayana.sra.demo.api.Coordinator")
//    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        Method method = resourceInfo.getResourceMethod();
        MultivaluedMap<String, String> headers = containerRequestContext.getHeaders();
        SRA.Type type = null;
        SRA transactional = method.getDeclaredAnnotation(SRA.class);
        URL lraId;
        URL newLRA = null;

        URL suspendedLRA = null;
        URL incommingLRA = null;
        String recoveryUrl = null;
        boolean delayCommit;

        if (transactional == null)
            transactional = method.getDeclaringClass().getDeclaredAnnotation(SRA.class);

        if (transactional != null) {
            type = (transactional).value();
            delayCommit = (transactional).delayCommit();
            Response.Status.Family[] cancel0nFamily = {Response.Status.Family.SERVER_ERROR};//TODO((Transactional) transactional).cancelOnFamily();
            Response.Status[] cancel0n = {}; //TODO((Transactional) transactional).cancelOn();

            containerRequestContext.setProperty(CANCEL_ON_FAMILY_PROP, cancel0nFamily);
            containerRequestContext.setProperty(CANCEL_ON_PROP, cancel0n);
        } else {
            delayCommit = false;
        }

        if (type == null) {
            Current.clearContext(headers);

            return; // not transactional
        }

        boolean enlist = true;
        boolean endAnnotation = method.isAnnotationPresent(Commit.class)
                || method.isAnnotationPresent(Prepare.class)
                || method.isAnnotationPresent(OnePhaseCommit.class);

        if (headers.containsKey(RTS_HTTP_CONTEXT_HEADER))
            incommingLRA = new URL(headers.getFirst(RTS_HTTP_CONTEXT_HEADER)); // TODO filters for asynchronous JAX-RS motheods should not throw exceptions

        if (endAnnotation && incommingLRA == null) {
//            Current.clearContext(headers); // or don't provide the header to participants and force them to provide something unique

            return;
        }

        switch (type) {
            case MANDATORY: // a txn must be present
                checkForTx(type, incommingLRA, true);

                lraId = incommingLRA;
                resumeTransaction(incommingLRA); // txId is not null

                break;
            case NEVER: // a txn must not be present
                checkForTx(type, incommingLRA, false);

                enlist = false;
                lraId = null;

                break;
            case NOT_SUPPORTED:
                // suspend any currently active transaction
//                    previous = AtomicAction.suspend();
                enlist = false;
                suspendedLRA = incommingLRA;
                lraId = null;

                break;
            case REQUIRED:
                if (incommingLRA != null) {
                        lraId = incommingLRA;
                        resumeTransaction(incommingLRA);
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

                if (incommingLRA != null) {
                    resumeTransaction(incommingLRA);
                }

                break;
            default:
                lraId = incommingLRA;
        }

        if (lraId == null) {
            lraTrace(containerRequestContext, lraId, "ServerLRAFilter before: removing header");
            // the method call needs to run without a transaction
            Current.clearContext(headers);

            if (suspendedLRA != null) {
                containerRequestContext.setProperty(SUSPENDED_LRA_PROP, suspendedLRA);
            }

            return; // non transactional
        } else {
            lraTrace(containerRequestContext, lraId, "ServerLRAFilter before: adding header");
//            headers.putSingle(LRA_HTTP_HEADER, lraId.toString());
        }

        if (!delayCommit) {
            containerRequestContext.setProperty(TERMINAL_LRA_PROP, lraId);
            newLRA = null;
        }

        // store state with the current thread. TODO for the async version use containerRequestContext.setProperty("lra", Current.peek());
        Current.updateLRAContext(lraId, headers); // make the current LRA available to the called method

        if (newLRA != null) {
            if (suspendedLRA != null) {
                containerRequestContext.setProperty(SUSPENDED_LRA_PROP, incommingLRA);
            }

//            Current.putState("newLRA", newLRA);
//            containerRequestContext.setProperty(NEW_LRA_PROP, newLRA);
        }

        lraTrace(containerRequestContext, lraId, "ServerLRAFilter before: making LRA available to injected SRAClient");
        getSRAClient().setCurrentSRA(lraId); // make the current LRA available to the called method

        // TODO make sure it is possible to do compensations inside a new LRA
        if (!endAnnotation && enlist) { // don't enlist for methods marked with Compensate, Complete or Leave
            Map<String, String> terminateURIs = getSRAClient().getTerminationUris(lraId, resourceInfo.getResourceClass(), containerRequestContext.getUriInfo(), true);
            String timeLimitStr = terminateURIs.get(SRAClient.TIMELIMIT_PARAM_NAME);
            long timeLimit = timeLimitStr == null ? SRAClient.DEFAULT_TIMEOUT_MILLIS : Long.parseLong(timeLimitStr);

            try {
                recoveryUrl = getSRAClient().joinSRAWithLinkHeader(lraId, timeLimit, terminateURIs.get("Link"));
            } catch (IllegalSRAStateException e) {
                lraTrace(containerRequestContext, lraId, "ServerLRAFilter before: aborting with " + e.getMessage());
                throw e;
            } catch (WebApplicationException e) {
                lraTrace(containerRequestContext, lraId, "ServerLRAFilter before: aborting with " + e.getMessage());
                throw new GenericSRAException(lraId, e.getResponse().getStatus(), e.getMessage(), e);
            }

            headers.putSingle(RTS_HTTP_RECOVERY_HEADER, recoveryUrl);
        }

        lraTrace(containerRequestContext, lraId, "ServerLRAFilter before: making LRA available as a thread local");
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        // a request is leaving the container so clear any context on the thread and fix up the LRA response header
//        ArrayList<Progress> progress = cast(requestContext.getProperty(ABORT_WITH_PROP));
        Object suspendedLRA = requestContext.getProperty(SUSPENDED_LRA_PROP);
        URL current = Current.peek();
        URL toClose = (URL) requestContext.getProperty(TERMINAL_LRA_PROP);
        boolean isCancel = isJaxRsCancel(requestContext, responseContext);
//resourceInfo.getResourceMethod();
        try {
            if (current != null && isCancel) {
                try {
                    // do not attempt to cancel if the request filter tried but failed to start a new LRA
//                    if (progress == null || progressDoesNotContain(progress, ProgressStep.StartFailed)) {
                        getSRAClient().cancelSRA(current);
//                        progress = updateProgress(progress, ProgressStep.Ended, null);
//                    }
                } catch (NotFoundException ignore) {
                    // must already be cancelled (if the intercepted method caused it to cancel)
                    // or completed (if the intercepted method caused it to complete)
//                    progress = updateProgress(progress, ProgressStep.Ended, null);
                } catch (WebApplicationException e) {
//                    progress = updateProgress(progress, ProgressStep.CancelFailed, e.getMessage());
                } catch (ProcessingException e) {
                    Method method = resourceInfo.getResourceMethod();
                    logger.warnf("ProcessingException: " + e.getMessage(),
                            method.getDeclaringClass().getName() + "#" + method.getName(), current.toString());

//                    progress = updateProgress(progress, ProgressStep.CancelFailed, e.getMessage());
                    toClose = null;
                } finally {
                    if (current.toString().equals(
                            Current.getLast(requestContext.getHeaders().get(RTS_HTTP_CONTEXT_HEADER)))) {
                        // the callers context was ended so invalidate it
                        requestContext.getHeaders().remove(RTS_HTTP_CONTEXT_HEADER);
                    }

                    if (toClose != null && toClose.toString().equals(current.toString())) {
                        toClose = null; // don't attempt to finish the LRA twice
                    }
                }
            }

            if (toClose != null) {
                try {
                    // do not attempt to close or cancel if the request filter tried but failed to start a new LRA
 //                   if (progress == null || progressDoesNotContain(progress, ProgressStep.StartFailed)) {
                        if (isCancel) {
                            getSRAClient().cancelSRA(toClose);
                        } else {
                            getSRAClient().commitSRA(toClose);
                        }

//                        progress = updateProgress(progress, ProgressStep.Ended, null);
//                    }
                } catch (NotFoundException ignore) {
                    // must already be cancelled (if the intercepted method caused it to cancel)
                    // or completed (if the intercepted method caused it to complete
//                    progress = updateProgress(progress, ProgressStep.Ended, null);
                } catch (WebApplicationException | ProcessingException e) {
//                    progress = updateProgress(progress,
//                            isCancel ? ProgressStep.CancelFailed : ProgressStep.CloseFailed, e.getMessage());
                } finally {
                    requestContext.getHeaders().remove(RTS_HTTP_CONTEXT_HEADER);

                    if (toClose.toString().equals(
                            Current.getLast(requestContext.getHeaders().get(RTS_HTTP_CONTEXT_HEADER)))) {
                        // the callers context was ended so invalidate it
                        requestContext.getHeaders().remove(RTS_HTTP_CONTEXT_HEADER);
                    }
                }
            }

/*            if (responseContext.getStatus() == Response.Status.OK.getStatusCode() &&
                    getSRAClient().isAsyncCompletion(resourceInfo.getResourceMethod())) {
                logger.warnf(
                        resourceInfo.getResourceMethod().getDeclaringClass().getName(),
                        resourceInfo.getResourceMethod().getName(),
                        Response.Status.ACCEPTED.getStatusCode(),
                        Response.Status.OK.getStatusCode());
            }*/

            /*
             * report any failed steps (ie if progress contains any failures) to the caller.
             * If either filter encountered a failure they may have completed partial actions and
             * we need tell the caller which steps failed and which ones succeeded. We use a
             * different warning code for each scenario:
             */
//            if (progress != null) {
//                String failureMessage =  processLRAOperationFailures(progress);
//
//                if (failureMessage != null) {
//                    LRALogger.logger.warn(failureMessage);
//
//                    // the actual failure(s) will also have been added to the i18NLogger logs at the time they occured
//                    responseContext.setEntity(failureMessage, null, MediaType.TEXT_PLAIN_TYPE);
//                }
//            }
        } finally {
            if (suspendedLRA != null) {
                Current.push((URL) suspendedLRA);
            }

// TODO            Current.updateLRAContext(responseContext);

            Current.popAll();
        }
    }

    private boolean isJaxRsCancel(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        int status = responseContext.getStatus();
        Response.Status.Family[] cancel0nFamily = (Response.Status.Family[]) requestContext.getProperty(CANCEL_ON_FAMILY_PROP);
        Response.Status[] cancel0n = (Response.Status[]) requestContext.getProperty(CANCEL_ON_PROP);

        if (cancel0nFamily != null) {
            if (Arrays.stream(cancel0nFamily).anyMatch(f -> Response.Status.Family.familyOf(status) == f)) {
                return true;
            }
        }

        if (cancel0n != null) {
            return Arrays.stream(cancel0n).anyMatch(f -> status == f.getStatusCode());
        }

        return false;
    }

    private long getTimeOut(Method method) {
        Annotation timeLimit = method.getDeclaredAnnotation(TimeLimit.class);

        if (timeLimit == null)
            timeLimit = method.getDeclaringClass().getDeclaredAnnotation(TimeLimit.class);

        if (timeLimit == null)
            return SRAClient.DEFAULT_TIMEOUT_MILLIS;

        TimeLimit tl = (TimeLimit) timeLimit;

        return tl.unit().toMillis(tl.limit());
    }

    private SRAClient getSRAClient() {
        if (sraClient == null) {
            try {
                sraClient = new SRAClient();
            } catch (URISyntaxException | MalformedURLException e) {
                throw new GenericSRAException(null, Response.Status.PRECONDITION_FAILED.getStatusCode(), e.getMessage(), e);
            }
        }

        return sraClient;
    }
    private URL startLRA(URL parentLRA, Method method, long timeout) {
//        getSRAClient(true);
        String clientId = method.getDeclaringClass().getName() +"#" + method.getName();

        return getSRAClient().startSRA(parentLRA, clientId, timeout, TimeUnit.MILLISECONDS);
    }

    private void resumeTransaction(URL lraId) {
        // nothing to do
    }

    private StringBuilder getParticipantLink(StringBuilder b, String uriPrefix, String key, String value) {

        String terminationUri = String.format("%s%s", uriPrefix, value);
        Link link =  Link.fromUri(terminationUri).title(key + " URI").rel(key).type(MediaType.TEXT_PLAIN).build();

        if (b.length() != 0)
            b.append(',');

        return b.append(link);
    }

    private String getCompensatorId(URL lraId, URI baseUri) {
        Map<String, String> terminateURIs = getTerminationUris(resourceInfo.getResourceClass());

        if (terminateURIs.size() < 3)
            throw new GenericSRAException(lraId, Response.Status.BAD_REQUEST.getStatusCode(),
                    "Missing complete, compensate or status annotations", null);

        // register with the coordinator

        StringBuilder linkHeaderValue = new StringBuilder();
        Annotation resourcePathAnnotation = resourceInfo.getResourceClass().getAnnotation(Path.class);
        String resourcePath = resourcePathAnnotation == null ? "/" : ((Path) resourcePathAnnotation).value();

        String uriPrefix = String.format("%s:%s%s",
                baseUri.getScheme(), baseUri.getSchemeSpecificPart(), resourcePath.substring(1));

        terminateURIs.forEach((k, v) -> getParticipantLink(linkHeaderValue, uriPrefix, k, v));

        return linkHeaderValue.toString();
    }

    /**
     * Checks for Complete, Compensate and Status annotations and returns the JAX-RS paths of the methods
     * they are associated with
     */
    private Map<String, String> getTerminationUris(Class<?> compensatorClass) {
        Map<String, String> paths = new HashMap<>();

        Arrays.stream(compensatorClass.getMethods()).forEach(method -> {
            Annotation pathAnnotation = method.getAnnotation(Path.class);

            if (pathAnnotation != null) {
                checkMethod(paths, SRAClient.COMMIT, (Path) pathAnnotation, method.getAnnotation(Commit.class));
                checkMethod(paths, SRAClient.PREPARE, (Path) pathAnnotation, method.getAnnotation(Prepare.class));
                checkMethod(paths, SRAClient.ROLLBACK, (Path) pathAnnotation, method.getAnnotation(Rollback.class));
                checkMethod(paths, SRAClient.STATUS, (Path) pathAnnotation, method.getAnnotation(Status.class));
                checkMethod(paths, SRAClient.ONEPHASECOMMIT, (Path) pathAnnotation, method.getAnnotation(OnePhaseCommit.class));
            }

            // TODO do we need to tell the coordinaor which HTTP verb the annotations are using
        });

        return paths;
    }

    private void checkMethod(Map<String, String> paths, String rel, Path pathAnnotation, Annotation annotationClass) {
        if (annotationClass != null)
            paths.put(rel, pathAnnotation.value());
    }

    protected void lraTrace(ContainerRequestContext context, URL lraId, String reason) {
        if (isTrace) {
            Method method = resourceInfo.getResourceMethod();
            System.out.printf("%s: container request for method %s: lra: %s%n",
                    reason, method.getDeclaringClass().getName() + "#" + method.getName(),
                    lraId == null ? "context" : lraId);
        }
    }
}
