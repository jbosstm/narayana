/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package io.narayana.lra.filter;

import io.narayana.lra.AnnotationResolver;
import io.narayana.lra.Current;
import io.narayana.lra.client.LRAParticipantData;
import io.narayana.lra.client.NarayanaLRAClient;
import io.narayana.lra.client.internal.proxy.nonjaxrs.LRAParticipant;
import io.narayana.lra.client.internal.proxy.nonjaxrs.LRAParticipantRegistry;
import io.narayana.lra.logging.LRALogger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.lra.annotation.AfterLRA;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.Forget;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.eclipse.microprofile.lra.annotation.Status;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;
import org.eclipse.microprofile.lra.annotation.ws.rs.Leave;

import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Pattern;

import static io.narayana.lra.LRAConstants.AFTER;
import static io.narayana.lra.LRAConstants.COMPENSATE;
import static io.narayana.lra.LRAConstants.COMPLETE;
import static io.narayana.lra.LRAConstants.FORGET;
import static io.narayana.lra.LRAConstants.LEAVE;
import static io.narayana.lra.LRAConstants.STATUS;
import static io.narayana.lra.LRAConstants.TIMELIMIT_PARAM_NAME;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_PARENT_CONTEXT_HEADER;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_RECOVERY_HEADER;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.Type.MANDATORY;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.Type.NESTED;

@Provider
@ApplicationScoped
public class ServerLRAFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final String CANCEL_ON_FAMILY_PROP = "CancelOnFamily";
    private static final String CANCEL_ON_PROP = "CancelOn";
    private static final String TERMINAL_LRA_PROP = "terminateLRA";
    private static final String SUSPENDED_LRA_PROP = "suspendLRA";
    private static final String CURRENT_LRA_PROP = "currentLRA";
    private static final String NEW_LRA_PROP = "newLRA";
    private static final String ABORT_WITH_PROP = "abortWith";
    private static final String PARTICIPANT_LINK_PROP = "compensatorURI";
    private static final Pattern START_END_QUOTES_PATTERN = Pattern.compile("^\"|\"$");
    private static final long DEFAULT_TIMEOUT_MILLIS = 0L;

    @Context
    protected ResourceInfo resourceInfo;

    @Inject
    private LRAParticipantRegistry lraParticipantRegistry;

    private NarayanaLRAClient lraClient;

    @Inject
    LRAParticipantData data;

    private boolean isTxInvalid(ContainerRequestContext containerRequestContext, LRA.Type type, URI lraId,
                                boolean shouldNotBeNull, ArrayList<Progress> progress) {
        if (lraId == null && shouldNotBeNull) {
            abortWith(containerRequestContext, null, Response.Status.PRECONDITION_FAILED.getStatusCode(),
                    type.name() + " but no tx", progress);
            return true;
        } else if (lraId != null && !shouldNotBeNull) {
            abortWith(containerRequestContext, lraId.toASCIIString(), Response.Status.PRECONDITION_FAILED.getStatusCode(),
                    type.name() + " but found tx", progress);
            return true;
        }

        return false;
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext) {
        // Note that this filter uses abortWith instead of throwing exceptions on encountering exceptional
        // conditions. This facilitates async because filters for asynchronous JAX-RS methods are
        // not allowed to throw exceptions.
        Method method = resourceInfo.getResourceMethod();
        MultivaluedMap<String, String> headers = containerRequestContext.getHeaders();
        LRA.Type type = null;
        LRA transactional = AnnotationResolver.resolveAnnotation(LRA.class, method);
        URI lraId;
        URI newLRA = null;
        Long timeout = null;

        URI suspendedLRA = null;
        URI incomingLRA = null;
        URI recoveryUrl;
        boolean isLongRunning = false;
        boolean requiresActiveLRA = false;
        ArrayList<Progress> progress = null;

        if (transactional == null) {
            transactional = method.getDeclaringClass().getDeclaredAnnotation(LRA.class);
        }

        if (transactional != null) {
            type = transactional.value();
            isLongRunning = !transactional.end();
            Response.Status.Family[] cancel0nFamily = transactional.cancelOnFamily();
            Response.Status[] cancel0n = transactional.cancelOn();

            if (cancel0nFamily.length != 0) {
                containerRequestContext.setProperty(CANCEL_ON_FAMILY_PROP, cancel0nFamily);
            }

            if (cancel0n.length != 0) {
                containerRequestContext.setProperty(CANCEL_ON_PROP, cancel0n);
            }

            if (transactional.timeLimit() != 0) {
                timeout = Duration.of(transactional.timeLimit(), transactional.timeUnit()).toMillis();
            }
        }

        boolean endAnnotation = AnnotationResolver.isAnnotationPresent(Complete.class, method)
                || AnnotationResolver.isAnnotationPresent(Compensate.class, method)
                || AnnotationResolver.isAnnotationPresent(Leave.class, method)
                || AnnotationResolver.isAnnotationPresent(Status.class, method)
                || AnnotationResolver.isAnnotationPresent(Forget.class, method)
                || AnnotationResolver.isAnnotationPresent(AfterLRA.class, method);

        if (headers.containsKey(LRA_HTTP_CONTEXT_HEADER)) {
            try {
                incomingLRA = new URI(Current.getLast(headers.get(LRA_HTTP_CONTEXT_HEADER)));
            } catch (URISyntaxException e) {
                String msg = String.format("header %s contains an invalid URL %s",
                        LRA_HTTP_CONTEXT_HEADER, Current.getLast(headers.get(LRA_HTTP_CONTEXT_HEADER)));

                abortWith(containerRequestContext, null, Response.Status.PRECONDITION_FAILED.getStatusCode(),
                        msg, null);
                return; // user error, bail out
            }

            if (AnnotationResolver.isAnnotationPresent(Leave.class, method)) {
                // leave the LRA
                Map<String, String> terminateURIs = NarayanaLRAClient.getTerminationUris(
                        resourceInfo.getResourceClass(), createUriPrefix(containerRequestContext), timeout);
                String compensatorId = terminateURIs.get("Link");

                if (compensatorId == null) {
                    abortWith(containerRequestContext, incomingLRA.toASCIIString(),
                            Response.Status.BAD_REQUEST.getStatusCode(),
                            "Missing complete or compensate annotations", null);
                    return; // user error, bail out
                }

                progress = new ArrayList<>();

                try {
                    getLRAClient().leaveLRA(incomingLRA, compensatorId);
                    progress.add(new Progress(ProgressStep.Left, null)); // leave succeeded
                } catch (WebApplicationException e) {
                    progress.add(new Progress(ProgressStep.LeaveFailed, e.getMessage())); // leave may have failed
                    abortWith(containerRequestContext, incomingLRA.toASCIIString(),
                            e.getResponse().getStatus(),
                            e.getMessage(), progress);
                    return; // the error will be handled or reported via the response filter
                } catch (ProcessingException e) { // a remote coordinator was unavailable
                    progress.add(new Progress(ProgressStep.LeaveFailed, e.getMessage())); // leave may have failed
                    abortWith(containerRequestContext, incomingLRA.toASCIIString(),
                            Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                            e.getMessage(), progress);
                    return; // the error will be handled or reported via the response filter
                }

                // let the participant know which lra he left by leaving the header intact
            }
        }

        if (type == null) {
            if (!endAnnotation) {
                Current.clearContext(headers);
            }

            if (incomingLRA != null) {
                Current.push(incomingLRA);
                containerRequestContext.setProperty(SUSPENDED_LRA_PROP, incomingLRA);
                containerRequestContext.setProperty(CURRENT_LRA_PROP, incomingLRA);
                Current.addActiveLRACache(incomingLRA);
            }

            return; // not transactional
        }

        // check the incoming request for an LRA context
        if (!headers.containsKey(LRA_HTTP_CONTEXT_HEADER)) {
            Object lraContext = containerRequestContext.getProperty(LRA_HTTP_CONTEXT_HEADER);

            if (lraContext != null) {
                incomingLRA = (URI) lraContext;
            }
        }

        if (endAnnotation && incomingLRA == null) {
            return;
        }

        if (incomingLRA != null) {
            // set the parent context header
            try {
                headers.putSingle(LRA_HTTP_PARENT_CONTEXT_HEADER, Current.getFirstParent(incomingLRA));
            } catch (UnsupportedEncodingException e) {
                abortWith(containerRequestContext, incomingLRA.toASCIIString(),
                        Response.Status.PRECONDITION_FAILED.getStatusCode(),
                        String.format("incoming LRA %s contains an invalid parent: %s", incomingLRA, e.getMessage()),
                        progress);
                return; // any previous actions (the leave request) will be reported via the response filter
            }
        }

        switch (type) {
            case MANDATORY: // a txn must be present
                if (isTxInvalid(containerRequestContext, type, incomingLRA, true, progress)) {
                    // isTxInvalid will have called abortWith (thus aborting the rest of the filter chain)
                    return; // any previous actions (e.g. the leave request) will be reported via the response filter
                }

                lraId = incomingLRA;
                requiresActiveLRA = true;

                break;
            case NEVER: // a txn must not be present
                if (isTxInvalid(containerRequestContext, type, incomingLRA, false, progress)) {
                    // isTxInvalid will have called abortWith (thus aborting the rest of the filter chain)
                    return; // any previous actions (the leave request) will be reported via the response filter
                }

                lraId = null; // must not run with any context

                break;
            case NOT_SUPPORTED:
                suspendedLRA = incomingLRA;
                lraId = null; // must not run with any context

                break;
            case NESTED:
                // FALLTHROUGH
            case REQUIRED:
                if (incomingLRA != null) {
                    if (type == NESTED) {
                        headers.putSingle(LRA_HTTP_PARENT_CONTEXT_HEADER, incomingLRA.toASCIIString());

                        // if there is an LRA present nest a new LRA under it
                        suspendedLRA = incomingLRA;

                        if (progress == null) {
                            progress = new ArrayList<>();
                        }

                        newLRA = lraId = startLRA(containerRequestContext, incomingLRA, method, timeout, progress);

                        if (newLRA == null) {
                            // startLRA will have called abortWith on the request context
                            // the failure plus any previous actions (the leave request) will be reported via the response filter
                            return;
                        }
                    } else {
                        lraId = incomingLRA;
                        // incomingLRA will be resumed
                        requiresActiveLRA = true;
                    }

                } else {
                    progress = new ArrayList<>();
                    newLRA = lraId = startLRA(containerRequestContext, null, method, timeout, progress);

                    if (newLRA == null) {
                        // startLRA will have called abortWith on the request context
                        // the failure and any previous actions (the leave request) will be reported via the response filter
                        return;
                    }
                }

                break;
            case REQUIRES_NEW:
//                    previous = AtomicAction.suspend();
                suspendedLRA = incomingLRA;

                if (progress == null) {
                    progress = new ArrayList<>();
                }
                newLRA = lraId = startLRA(containerRequestContext,null, method, timeout, progress);

                if (newLRA == null) {
                    // startLRA will have called abortWith on the request context
                    // the failure and any previous actions (the leave request) will be reported via the response filter
                    return;
                }

                break;
            case SUPPORTS:
                lraId = incomingLRA;

                // incomingLRA will be resumed if not null

                break;
            default:
                lraId = incomingLRA;
        }

        if (lraId == null) {
            // the method call needs to run without a transaction
            Current.clearContext(headers);

            if (suspendedLRA != null) {
                containerRequestContext.setProperty(SUSPENDED_LRA_PROP, suspendedLRA);
            }

            return; // non transactional
        }

        if (!isLongRunning) {
            containerRequestContext.setProperty(TERMINAL_LRA_PROP, lraId);
        }

        // store state with the current thread
        Current.updateLRAContext(lraId, headers); // make the current LRA available to the called method

        if (newLRA != null) {
            if (suspendedLRA != null) {
                containerRequestContext.setProperty(SUSPENDED_LRA_PROP, incomingLRA);
            }

            containerRequestContext.setProperty(NEW_LRA_PROP, newLRA);
        }

        Current.push(lraId);

        try {
            getLRAClient().setCurrentLRA(lraId); // make the current LRA available to the called method
        } catch (Exception e) {
            // should not happen since lraId has already been validated
            // (perhaps we should not use the client API to set the context)
            abortWith(containerRequestContext, lraId.toASCIIString(),
                    Response.Status.BAD_REQUEST.getStatusCode(),
                    e.getMessage(),
                    progress);
            return; // any previous actions (such as leave and start requests) will be reported via the response filter
        }

        if (!endAnnotation) { // don't enlist for methods marked with Compensate, Complete or Leave
            Map<String, String> terminateURIs = NarayanaLRAClient.getTerminationUris(resourceInfo.getResourceClass(),
                createUriPrefix(containerRequestContext), timeout);
            String timeLimitStr = terminateURIs.get(TIMELIMIT_PARAM_NAME);
            long timeLimit = timeLimitStr == null ? DEFAULT_TIMEOUT_MILLIS : Long.parseLong(timeLimitStr);

            LRAParticipant participant = lraParticipantRegistry != null ?
                lraParticipantRegistry.getParticipant(resourceInfo.getResourceClass().getName()) : null;

            if (terminateURIs.containsKey("Link") || participant != null) {
                try {
                    if (participant != null) {
                        participant.augmentTerminationURIs(terminateURIs, containerRequestContext.getUriInfo().getBaseUri());
                    }

                    String compensatorLink = buildCompensatorURI(
                            toURI(terminateURIs.get(COMPENSATE)),
                            toURI(terminateURIs.get(COMPLETE)),
                            toURI(terminateURIs.get(FORGET)),
                            toURI(terminateURIs.get(LEAVE)),
                            toURI(terminateURIs.get(AFTER)),
                            toURI(terminateURIs.get(STATUS)));
                    StringBuilder previousParticipantData = new StringBuilder();

                    // store the registration link in case the participant wants to associate data with the enlistment in the LRA
                    containerRequestContext.setProperty(PARTICIPANT_LINK_PROP, compensatorLink);

                    recoveryUrl = getLRAClient().enlistCompensator(lraId, timeLimit, compensatorLink, previousParticipantData);

                    if (previousParticipantData.length() != 0) {
                        // this participant has previously updated the LRAParticipantData bean so make it available for this invocation
                        setUserDefinedData(previousParticipantData.toString());
                    }

                    progress = updateProgress(progress, ProgressStep.Joined, null);

                    headers.putSingle(LRA_HTTP_RECOVERY_HEADER,
                            START_END_QUOTES_PATTERN.matcher(recoveryUrl.toASCIIString()).replaceAll(""));
                } catch (WebApplicationException e) {
                    progress = updateProgress(progress, ProgressStep.JoinFailed, e.getMessage());
                    abortWith(containerRequestContext, lraId.toASCIIString(),
                            e.getResponse().getStatus(),
                            String.format("%s: %s", e.getClass().getSimpleName(), e.getMessage()), progress);
                    // the failure plus any previous actions (such as leave and start requests) will be reported via the response filter
                } catch (URISyntaxException e) {
                    progress = updateProgress(progress, ProgressStep.JoinFailed, e.getMessage()); // one or more of the participant end points was invalid
                    abortWith(containerRequestContext, lraId.toASCIIString(),
                            Response.Status.BAD_REQUEST.getStatusCode(),
                            String.format("%s %s: %s", lraId, e.getClass().getSimpleName(), e.getMessage()), progress);
                    // the failure plus any previous actions (such as leave and start requests) will be reported via the response filter
                } catch (ProcessingException e) {
                    progress = updateProgress(progress, ProgressStep.JoinFailed, e.getMessage()); // a remote coordinator was unavailable
                    abortWith(containerRequestContext, lraId.toASCIIString(),
                            Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                            String.format("%s %s,", e.getClass().getSimpleName(), e.getMessage()), progress);
                    // the failure plus any previous actions (such as leave and start requests) will be reported via the response filter
                }
            } else if (requiresActiveLRA && getLRAClient().getStatus(lraId) != LRAStatus.Active) {
                Current.clearContext(headers);
                Current.pop(lraId);
                containerRequestContext.removeProperty(SUSPENDED_LRA_PROP);

                if (type == MANDATORY) {
                    abortWith(containerRequestContext, lraId.toASCIIString(),
                            Response.Status.PRECONDITION_FAILED.getStatusCode(),
                            "LRA should have been active: ", progress);
                    // any previous actions (such as leave and start requests) will be reported via the response filter
                }
            }
        }

        containerRequestContext.setProperty(CURRENT_LRA_PROP, lraId);
        Current.addActiveLRACache(lraId);
    }

    private String createUriPrefix(ContainerRequestContext containerRequestContext) {
        return ConfigProvider.getConfig().getOptionalValue("narayana.lra.base-uri", String.class)
            .orElseGet(() -> {
                UriInfo uriInfo = containerRequestContext.getUriInfo();

                /*
                 * Calculate which path to prepend to the LRA participant methods. If there is more than one matching URI
                 * then the second matched URI comes from either the class level Path annotation or from a sub-resource locator.
                 * In both cases the second matched URI can be used as a prefix for the LRA participant URIs:
                 */
                List<String> matchedURIs = uriInfo.getMatchedURIs();
                int matchedURI = (matchedURIs.size() > 1 ? 1 : 0);
                return uriInfo.getBaseUri() + matchedURIs.get(matchedURI);
            });
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        // a request is leaving the container so clear any context on the thread and fix up the LRA response header
        ArrayList<Progress> progress = cast(requestContext.getProperty(ABORT_WITH_PROP));
        Object suspendedLRA = requestContext.getProperty(SUSPENDED_LRA_PROP);
        URI current = (URI) requestContext.getProperty(CURRENT_LRA_PROP);
        URI toClose = (URI) requestContext.getProperty(TERMINAL_LRA_PROP);
        boolean isCancel = isJaxRsCancel(requestContext, responseContext);
        // the service method has finished but the user data may have changed
        String userData = getUserDefinedData();
        String compensator = (String) requestContext.getProperty(PARTICIPANT_LINK_PROP);

        try {
            if (current != null && isCancel) {
                try {
                    // do not attempt to cancel if the request filter tried but failed to start a new LRA
                    if (progress == null || progressDoesNotContain(progress, ProgressStep.StartFailed)) {
                        getLRAClient().cancelLRA(current);
                        progress = updateProgress(progress, ProgressStep.Ended, null);
                    }
                } catch (NotFoundException ignore) {
                    // must already be cancelled (if the intercepted method caused it to cancel)
                    // or completed (if the intercepted method caused it to complete)
                    progress = updateProgress(progress, ProgressStep.Ended, null);
                } catch (WebApplicationException e) {
                    progress = updateProgress(progress, ProgressStep.CancelFailed, e.getMessage());
                } catch (ProcessingException e) {
                    Method method = resourceInfo.getResourceMethod();
                    LRALogger.i18nLogger.warn_lraFilterContainerRequest("ProcessingException: " + e.getMessage(),
                            method.getDeclaringClass().getName() + "#" + method.getName(), current.toASCIIString());

                    progress = updateProgress(progress, ProgressStep.CancelFailed, e.getMessage());
                    toClose = null;
                } finally {
                    if (current.toASCIIString().equals(
                            Current.getLast(requestContext.getHeaders().get(LRA_HTTP_CONTEXT_HEADER)))) {
                        // the callers context was ended so invalidate it
                        requestContext.getHeaders().remove(LRA_HTTP_CONTEXT_HEADER);
                    }

                    if (toClose != null && toClose.toASCIIString().equals(current.toASCIIString())) {
                        toClose = null; // don't attempt to finish the LRA twice
                    }
                }
            }

            if (toClose != null) {
                try {
                    // do not attempt to close or cancel if the request filter tried but failed to start a new LRA
                    if (progress == null || progressDoesNotContain(progress, ProgressStep.StartFailed)) {
                        if (isCancel) {
                            getLRAClient().cancelLRA(toClose, compensator, getUserDefinedData());
                        } else {
                            getLRAClient().closeLRA(toClose, compensator, getUserDefinedData());
                        }

                        progress = updateProgress(progress, ProgressStep.Ended, null);
                    }
                } catch (NotFoundException ignore) {
                    // must already be cancelled (if the intercepted method caused it to cancel)
                    // or completed (if the intercepted method caused it to complete
                    progress = updateProgress(progress, ProgressStep.Ended, null);
                } catch (WebApplicationException | ProcessingException e) {
                    progress = updateProgress(progress,
                            isCancel ? ProgressStep.CancelFailed : ProgressStep.CloseFailed, e.getMessage());
                } finally {
                    requestContext.getHeaders().remove(LRA_HTTP_CONTEXT_HEADER);

                    if (toClose.toASCIIString().equals(
                            Current.getLast(requestContext.getHeaders().get(LRA_HTTP_CONTEXT_HEADER)))) {
                        // the callers context was ended so invalidate it
                        requestContext.getHeaders().remove(LRA_HTTP_CONTEXT_HEADER);
                    }
                }
            } else if (current != null && compensator != null && userData != null) {
                getLRAClient().enlistCompensator(current, 0L, compensator, new StringBuilder(userData));
            }

            if (responseContext.getStatus() == Response.Status.OK.getStatusCode()
                && resourceInfo.getResourceMethod() != null
                && NarayanaLRAClient.isAsyncCompletion(resourceInfo.getResourceMethod())) {
                LRALogger.i18nLogger.warn_lraParticipantqForAsync(
                        resourceInfo.getResourceMethod().getDeclaringClass().getName(),
                        resourceInfo.getResourceMethod().getName(),
                        Response.Status.ACCEPTED.getStatusCode(),
                        Response.Status.OK.getStatusCode());
            }

            /*
             * report any failed steps (ie if progress contains any failures) to the caller.
             * If either filter encountered a failure they may have completed partial actions, and
             * we need tell the caller which steps failed and which ones succeeded. We use a
             * different warning code for each scenario:
             */
            if (progress != null) {
                String failureMessage =  processLRAOperationFailures(progress);

                if (failureMessage != null) {
                    LRALogger.logger.warn(failureMessage);

                    // the actual failure(s) will also have been added to the i18NLogger logs at the time they occurred
                    responseContext.setEntity(failureMessage, null, MediaType.TEXT_PLAIN_TYPE);
                }
            }
        } finally {
            if (suspendedLRA != null) {
                Current.push((URI) suspendedLRA);
            }

            Current.updateLRAContext(responseContext);

            Current.popAll();
            Current.removeActiveLRACache(current);
        }
    }

    private NarayanaLRAClient getLRAClient() {
        if (lraClient == null) {
            // no need to lock
            lraClient = new NarayanaLRAClient();
        }

        return lraClient;
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

    // the request filter may perform multiple and in failure scenarios the LRA may be left in an ambiguous state:
    // the following structure is used to track progress so that such failures can be reported in the response
    // filter processing
    private enum ProgressStep {
        Left ("leave succeeded"),
        LeaveFailed("leave failed"),
        Started("start succeeded"),
        StartFailed("start failed"),
        Joined("join succeeded"),
        JoinFailed("join failed"),
        Ended("end succeeded"),
        CloseFailed("close failed"),
        CancelFailed("cancel failed");

        final String status;

        ProgressStep(final String status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return status;
        }
    }

    // list of steps (both successful and unsuccessful) performed so far by the request and response filter
    // and is used for error reporting
    private static class Progress {
        static EnumSet<ProgressStep> failures = EnumSet.of(
                ProgressStep.LeaveFailed,
                ProgressStep.StartFailed,
                ProgressStep.JoinFailed,
                ProgressStep.CloseFailed,
                ProgressStep.CancelFailed);

        ProgressStep progress;
        String reason;

        public Progress(ProgressStep progress, String reason) {
            this.progress = progress;
            this.reason = reason;
        }

        public boolean wasSuccessful() {
            return !failures.contains(progress);
        }
    }

    // convert the list of steps carried out by the filters into a warning message
    private String processLRAOperationFailures(ArrayList<Progress> progress) {
        StringJoiner badOps = new StringJoiner(", ");
        StringJoiner goodOps = new StringJoiner(", ");
        StringBuilder code = new StringBuilder("-");

        progress.forEach(p -> {
            if (p.wasSuccessful()) {
                code.insert(0, p.progress.ordinal());
                goodOps.add(String.format("%s (%s)", p.progress.name(), p.progress.status));
            } else {
                code.append(p.progress.ordinal());
                badOps.add(String.format("%s (%s)", p.progress.name(), p.reason));
            }
        });

        /*
         * return a string which encodes the result:
         * <major code>-<failed op codes>-<successful op codes>: <details of failed ops> (<details of successful ops>)
         *
         * where
         *
         * <major code>: corresponds to the id of the message in the logs
         * <failed op codes>: each digit corresponds to the enum ordinal value of the ProgressStep enum value that was successful
         * <successful op codes>: each digit corresponds to the enum ordinal value of the ProgressStep enum value that failed
         * <details of failed ops>: comma separated list of failed operation details "<op name> (<exception message>)"
         * <details of successful ops>: comma separated list of successful operation details "<op name> (<op description>)"
         */

        if (badOps.length() != 0) {
            return LRALogger.i18nLogger.warn_LRAStatusInDoubt(String.format("%s: %s (%s)", code, badOps, goodOps));
        }

        return null;
    }

    private boolean progressDoesNotContain(ArrayList<Progress> progress, ProgressStep step) {
        return progress.stream().noneMatch(p -> p.progress == step);
    }

    // add another step to the list of steps performed so far
    private ArrayList<Progress> updateProgress(ArrayList<Progress> progress, ProgressStep step, String reason) {
        if (progress == null) {
            progress = new ArrayList<>();
        }

        progress.add(new Progress(step, reason));

        return progress;
    }

    // the processing performed by the request filter caused the request to abort (without executing application code)
    private void abortWith(ContainerRequestContext containerRequestContext, String lraId, int statusCode,
                           String message, Collection<Progress> reasons) {
        // the response filter will set the entity body
        containerRequestContext.abortWith(Response.status(statusCode).build());
        // make the reason for the failure available to the response filter
        containerRequestContext.setProperty(ABORT_WITH_PROP, reasons);

        Method method = resourceInfo.getResourceMethod();
        LRALogger.i18nLogger.warn_lraFilterContainerRequest(message,
                method.getDeclaringClass().getName() + "#" + method.getName(),
                lraId == null ? "context" : lraId);
    }

    private URI toURI(String uri) throws URISyntaxException {
        return uri == null ? null : new URI(uri);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Collection<?>> T cast(Object obj) {
        return (T) obj;
    }

    private URI startLRA(ContainerRequestContext containerRequestContext, URI parentLRA, Method method, Long timeout,
                         ArrayList<Progress> progress) {
        // timeout should already have been converted to milliseconds
        String clientId = method.getDeclaringClass().getName() + "#" + method.getName();

        try {
            URI lra = getLRAClient().startLRA(parentLRA, clientId, timeout, ChronoUnit.MILLIS, false);
            updateProgress(progress, ProgressStep.Started, null);
            return lra;
        } catch (WebApplicationException e) {
            updateProgress(progress, ProgressStep.StartFailed, e.getMessage());

            abortWith(containerRequestContext, null,
                    e.getResponse().getStatus(),
                    String.format("%s %s", e.getClass().getSimpleName(), e.getMessage()),
                    progress);
        } catch (ProcessingException e) {
            updateProgress(progress, ProgressStep.StartFailed, e.getMessage());

            abortWith(containerRequestContext, null,
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    String.format("%s %s", e.getClass().getSimpleName(), e.getMessage()),
                    progress);
        }
        return null;
    }

    private String getUserDefinedData() {
        try {
            return data != null ? data.getData() : null;
        } catch (ContextNotActiveException e) {
            LRALogger.i18nLogger.warn_missingContexts("CDI bean of type LRAParticipantData is not available", e);
        }

        return null;
    }

    private void setUserDefinedData(String userDefinedData) {
        try {
            if (data != null) {
                data.setData(userDefinedData);
            }
        } catch (ContextNotActiveException e) {
            LRALogger.i18nLogger.warn_missingContexts("CDI bean of type LRAParticipantData is not available", e);
        }
    }

    private String buildCompensatorURI(URI compensate, URI complete, URI forget, URI leave, URI after, URI status) {
        StringBuilder linkHeaderValue = new StringBuilder();

        makeLink(linkHeaderValue, COMPENSATE, compensate);
        makeLink(linkHeaderValue, COMPLETE, complete);
        makeLink(linkHeaderValue, FORGET, forget);
        makeLink(linkHeaderValue, LEAVE, leave);
        makeLink(linkHeaderValue, AFTER, after);
        makeLink(linkHeaderValue, STATUS, status);

        return linkHeaderValue.toString();
    }

    private static void makeLink(StringBuilder b, String key, URI value) {
        if (key == null || value == null) {
            return;
        }

        String uri = value.toASCIIString();
        Link link =  Link.fromUri(uri).title(key + " URI").rel(key).type(MediaType.TEXT_PLAIN).build();

        if (b.length() != 0) {
            b.append(',');
        }

        b.append(link);
    }
}