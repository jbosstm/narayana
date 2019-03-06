/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat Middleware LLC, and individual contributors
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

package io.narayana.lra.checker.cdi;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.container.Suspended;

import io.narayana.lra.checker.FailureCatalog;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.Forget;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;
import org.eclipse.microprofile.lra.annotation.ws.rs.Leave;
import org.eclipse.microprofile.lra.annotation.Status;


/**
 * <p>
 * CDI extension working in concord of LRA filters.
 * <p>
 * When added at the class path of the project this extension validates
 * if the classes contain compulsory annotation complementary to {@link LRA}.
 * The rules of what are compulsory annotations and their attributes
 * are defined in LRA specification.
 * <p>
 * Failures are gathered under the list of failures in {@link FailureCatalog}.
 *
 * @author Ondra Chaloupka <ochaloup@redhat.com>
 */
public class LraAnnotationProcessingExtension implements Extension {

    <X> void processLraAnnotatedType(@Observes @WithAnnotations({LRA.class}) ProcessAnnotatedType<X> classAnnotatedWithLra) {

        // All compulsory LRA annotations are available at the class
        Supplier<Stream<AnnotatedMethod<? super X>>> sup = () -> classAnnotatedWithLra.getAnnotatedType().getMethods().stream();
        Set<Class<? extends Annotation>> missing = new HashSet<>();
        if (!sup.get().anyMatch(m -> m.isAnnotationPresent(Compensate.class))) {
            missing.add(Compensate.class);
        }

        // gathering all LRA annotations in the class
        List<LRA> lraAnnotations = new ArrayList<>();
        LRA classLraAnnotation = classAnnotatedWithLra.getAnnotatedType().getAnnotation(LRA.class);
        if (classLraAnnotation != null) {
            lraAnnotations.add(classLraAnnotation);
        }
        List<LRA> methodlraAnnotations = sup.get()
                .filter(m -> m.isAnnotationPresent(LRA.class))
                .map(m -> m.getAnnotation(LRA.class))
                .collect(Collectors.toList());
        lraAnnotations.addAll(methodlraAnnotations);

        // when LRA annotations expect no context then they are not part of the LRA and no handling
        // of the completion or compensation is needed
        boolean isNoLRAContext = lraAnnotations.stream().allMatch(
                lraAnn -> (lraAnn.value() == LRA.Type.NEVER || lraAnn.value() == LRA.Type.NOT_SUPPORTED));
        if (isNoLRAContext) {
            return;
        }

        final String classAnnotatedWithLraName = classAnnotatedWithLra.getAnnotatedType().getJavaClass().getName();
        if (!missing.isEmpty()) {
            FailureCatalog.INSTANCE.add("Class " + classAnnotatedWithLraName + " uses "
                + LRA.class.getName() + " which requires methods handling LRA events. Missing annotations in the class: " + missing);
        }

        // Only one of each LRA annotation is placed in the class
        List<AnnotatedMethod<? super X>> methodsWithCompensate = sup.get()
            .filter(m -> m.isAnnotationPresent(Compensate.class))
            .collect(Collectors.toList());
        List<AnnotatedMethod<? super X>> methodsWithComplete = sup.get()
            .filter(m -> m.isAnnotationPresent(Complete.class))
            .collect(Collectors.toList());
        List<AnnotatedMethod<? super X>> methodsWithStatus = sup.get()
            .filter(m -> m.isAnnotationPresent(Status.class))
            .collect(Collectors.toList());
        List<AnnotatedMethod<? super X>> methodsWithLeave = sup.get()
            .filter(m -> m.isAnnotationPresent(Leave.class))
            .collect(Collectors.toList());
        List<AnnotatedMethod<? super X>> methodsWithForget = sup.get()
            .filter(m -> m.isAnnotationPresent(Forget.class))
            .collect(Collectors.toList());

        BiFunction<Class<?>, List<AnnotatedMethod<? super X>>, String> errorMsg = (clazz, methods) -> String.format(
            "There are used multiple annotations '%s' in the class '%s' on methods %s. Only one per the class is expected.",
            clazz.getName(), classAnnotatedWithLraName,
            methods.stream().map(a -> a.getJavaMember().getName()).collect(Collectors.toList()));
        if (methodsWithCompensate.size() > 1) {
            FailureCatalog.INSTANCE.add(errorMsg.apply(Compensate.class, methodsWithCompensate));
        }
        if (methodsWithComplete.size() > 1) {
            FailureCatalog.INSTANCE.add(errorMsg.apply(Complete.class, methodsWithComplete));
        }
        if (methodsWithStatus.size() > 1) {
            FailureCatalog.INSTANCE.add(errorMsg.apply(Status.class, methodsWithStatus));
        }
        if (methodsWithLeave.size() > 1) {
            FailureCatalog.INSTANCE.add(errorMsg.apply(Leave.class, methodsWithLeave));
        }
        if (methodsWithForget.size() > 1) {
            FailureCatalog.INSTANCE.add(errorMsg.apply(Forget.class, methodsWithForget));
        }

        if (methodsWithCompensate.size() > 0) {
            // Each method annotated with LRA-style annotations contain all necessary REST annotations
            // @Compensate - requires @Path and @PUT
            final AnnotatedMethod<? super X> methodWithCompensate = methodsWithCompensate.get(0);
            Function<Class<?>, String> getCompensateMissingErrMsg = (wrongAnnotation) ->
                getMissingAnnotationError(methodWithCompensate, classAnnotatedWithLra, Compensate.class, wrongAnnotation);
            boolean isCompensateContainsPathAnnotation = methodWithCompensate.getAnnotations().stream().anyMatch(a -> a.annotationType().equals(Path.class));
            if (!isCompensateContainsPathAnnotation) {
                FailureCatalog.INSTANCE.add(getCompensateMissingErrMsg.apply(Path.class));
            }
            boolean isCompensateContainsPutAnnotation = methodWithCompensate.getAnnotations().stream().anyMatch(a -> a.annotationType().equals(PUT.class));
            if (!isCompensateContainsPutAnnotation) {
                FailureCatalog.INSTANCE.add(getCompensateMissingErrMsg.apply(PUT.class));
            }
            boolean isCompensateParametersContainsSuspended = methodWithCompensate.getParameters().stream().flatMap(p -> p.getAnnotations().stream())
                    .anyMatch(a -> a.annotationType().equals(Suspended.class));
            if (isCompensateParametersContainsSuspended) {
                if (methodsWithStatus.size() == 0 || methodsWithForget.size() == 0) {
                    FailureCatalog.INSTANCE.add(getMissingAnnotationsForAsynchHandling(methodWithCompensate, classAnnotatedWithLra, Compensate.class));
                }
            }
        }

        if (methodsWithComplete.size() > 0) {
            // @Complete - requires @Path and @PUT
            final AnnotatedMethod<? super X> methodWithComplete = methodsWithComplete.get(0);
            Function<Class<?>, String> getCompleteMissingErrMsg = (wrongAnnotation) ->
                getMissingAnnotationError(methodWithComplete, classAnnotatedWithLra, Complete.class, wrongAnnotation);
            boolean isCompleteContainsPathAnnotation = methodWithComplete.getAnnotations().stream().anyMatch(a -> a.annotationType().equals(Path.class));
            if (!isCompleteContainsPathAnnotation) {
                FailureCatalog.INSTANCE.add(getCompleteMissingErrMsg.apply(Path.class));
            }
            boolean isCompleteContainsPutAnnotation = methodWithComplete.getAnnotations().stream().anyMatch(a -> a.annotationType().equals(PUT.class));
            if (!isCompleteContainsPutAnnotation) {
                FailureCatalog.INSTANCE.add(getCompleteMissingErrMsg.apply(PUT.class));
            }
            boolean isCompleteParametersContainsSuspended = methodWithComplete.getParameters().stream().flatMap(p -> p.getAnnotations().stream())
                    .anyMatch(a -> a.annotationType().equals(Suspended.class));
            if (isCompleteParametersContainsSuspended) {
                if (methodsWithStatus.size() == 0 || methodsWithForget.size() == 0) {
                    FailureCatalog.INSTANCE.add(getMissingAnnotationsForAsynchHandling(methodWithComplete, classAnnotatedWithLra, Complete.class));
                }
            }
        }

        if (methodsWithStatus.size() > 0) {
            // @Status - requires @Path and @GET
            final AnnotatedMethod<? super X> methodWithStatus = methodsWithStatus.get(0);
            Function<Class<?>, String> getStatusMissingErrMsg = (wrongAnnotation) ->
                getMissingAnnotationError(methodWithStatus, classAnnotatedWithLra, Status.class, wrongAnnotation);
            boolean isStatusContainsPathAnnotation = methodWithStatus.getAnnotations().stream().anyMatch(a -> a.annotationType().equals(Path.class));
            if (!isStatusContainsPathAnnotation) {
                FailureCatalog.INSTANCE.add(getStatusMissingErrMsg.apply(Path.class));
            }
            boolean isStatusContainsGetAnnotation = methodWithStatus.getAnnotations().stream().anyMatch(a -> a.annotationType().equals(GET.class));
            if (!isStatusContainsGetAnnotation) {
                FailureCatalog.INSTANCE.add(getStatusMissingErrMsg.apply(GET.class));
            }
        }

        if (methodsWithLeave.size() > 0) {
            // @Leave - requires @PUT
            final AnnotatedMethod<? super X> methodWithLeave = methodsWithLeave.get(0);
            boolean isLeaveContainsPutAnnotation = methodWithLeave.getAnnotations().stream().anyMatch(a -> a.annotationType().equals(PUT.class));
            if (!isLeaveContainsPutAnnotation) {
                FailureCatalog.INSTANCE.add(getMissingAnnotationError(methodWithLeave, classAnnotatedWithLra, Leave.class, PUT.class));
            }
        }

        if (methodsWithForget.size() > 0) {
            // @Forget - requires @DELETE
            final AnnotatedMethod<? super X> methodWithForget = methodsWithForget.get(0);
            boolean isForgetContainsPutAnnotation = methodWithForget.getAnnotations().stream().anyMatch(a -> a.annotationType().equals(DELETE.class));
            if (!isForgetContainsPutAnnotation) {
                FailureCatalog.INSTANCE.add(getMissingAnnotationError(methodWithForget, classAnnotatedWithLra, Forget.class, DELETE.class));
            }
        }
    }

    private String getMissingAnnotationError(AnnotatedMethod<?> method, ProcessAnnotatedType<?> classAnnotated,
        Class<?> lraTypeAnnotation, Class<?> complementaryAnnotation) {
        return String.format("Method '%s' of class '%s' annotated with '%s' should use complementary annotation %s",
            method.getJavaMember().getName(), classAnnotated.getAnnotatedType().getJavaClass().getName(),
            lraTypeAnnotation.getName(), complementaryAnnotation.getName());
    }

    private String getMissingAnnotationsForAsynchHandling(AnnotatedMethod<?> method, ProcessAnnotatedType<?> classAnnotated,
            Class<?> completionAnnotation) {
        return String.format("Method '%s' of class '%s' annotated with '%s' is defined being asynchronous via @Suspend parameter annotation. " +
            "The LRA class has to contain @Status and @Forget annotations to activate such handling.",
                method.getJavaMember().getName(), classAnnotated.getAnnotatedType().getJavaClass().getName(),
                completionAnnotation.getName());
    }
}
