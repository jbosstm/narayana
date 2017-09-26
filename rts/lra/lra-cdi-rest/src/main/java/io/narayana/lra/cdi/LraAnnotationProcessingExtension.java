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

package io.narayana.lra.cdi;

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
import javax.enterprise.inject.spi.DeploymentException;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import io.narayana.lra.annotation.Compensate;
import io.narayana.lra.annotation.Complete;
import io.narayana.lra.annotation.Forget;
import io.narayana.lra.annotation.LRA;
import io.narayana.lra.annotation.Leave;
import io.narayana.lra.annotation.Status;


/**
 * <p>
 * CDI extension working in concord of LRA filters.
 * <p>
 * When added at the class path of the project this extension validates
 * if the classes contain compulsory annotation complementary to {@link LRA}.
 * The rules of what are compulsory annotations and their attributes
 * are defined in LRA specification.
 * <p>
 * In case of validity violation the {@link DeploymentException} is thrown.
 * 
 * @author Ondra Chaloupka <ochaloup@redhat.com>
 */
public class LraAnnotationProcessingExtension implements Extension {

    <X> void processLraAnnotatedType(@Observes @WithAnnotations({LRA.class}) ProcessAnnotatedType<X> classAnnotatedWithLra) {

        // All compulsory LRA annotations are available at the class
        Supplier<Stream<AnnotatedMethod<? super X>>> sup = () -> classAnnotatedWithLra.getAnnotatedType().getMethods().stream();
        Set<Class<? extends Annotation>> missing = new HashSet<>();
        if(!sup.get().anyMatch(m -> m.isAnnotationPresent(Compensate.class))) missing.add(Compensate.class);
        if(!sup.get().anyMatch(m -> m.isAnnotationPresent(Status.class))) missing.add(Status.class);

        // gathering all LRA annotations in the class
        List<LRA> lraAnnotations = new ArrayList<>();
        LRA classLraAnnotation = classAnnotatedWithLra.getAnnotatedType().getAnnotation(LRA.class);
        if(classLraAnnotation != null)lraAnnotations.add(classLraAnnotation);
        List<LRA> methodlraAnnotations = sup.get()
                .filter(m -> m.isAnnotationPresent(LRA.class))
                .map(m -> m.getAnnotation(LRA.class))
                .collect(Collectors.toList());
        lraAnnotations.addAll(methodlraAnnotations);
        // if any of the LRA annotations have set the join attribute to true (join se to true means
        // handling it as full LRA participant which needs to be completed, compensated...) 
        // then have to process checks for compulsory LRA annotations
        boolean isJoin = lraAnnotations.stream().anyMatch(m -> m.join());

        if(!missing.isEmpty() && isJoin) {
            throw new DeploymentException("Class " + classAnnotatedWithLra.getAnnotatedType().getJavaClass().getName() + " uses "
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
            clazz.getName(), classAnnotatedWithLra.getAnnotatedType().getJavaClass().getName(),
            methods.stream().map(a -> a.getJavaMember().getName()).collect(Collectors.toList()));
        if(methodsWithCompensate.size() > 1) {
            throw new DeploymentException(errorMsg.apply(Compensate.class, methodsWithCompensate));
        }
        if(methodsWithComplete.size() > 1) {
            throw new DeploymentException(errorMsg.apply(Complete.class, methodsWithComplete));
        }
        if(methodsWithStatus.size() > 1) {
            throw new DeploymentException(errorMsg.apply(Status.class, methodsWithStatus));
        }
        if(methodsWithLeave.size() > 1) {
            throw new DeploymentException(errorMsg.apply(Leave.class, methodsWithLeave));
        }
        if(methodsWithForget.size() > 1) {
            throw new DeploymentException(errorMsg.apply(Forget.class, methodsWithForget));
        }

        if(methodsWithCompensate.size() > 0) {
            // Each method annotated with LRA-style annotations contain all necessary REST annotations
            // @Compensate - requires @Path and @POST
            final AnnotatedMethod<? super X> methodWithCompensate = methodsWithCompensate.get(0);
            Function<Class<?>, String> getCompensateMissingErrMsg = (wrongAnnotation) ->
                getMissingAnnotationError(methodWithCompensate, classAnnotatedWithLra, Compensate.class, wrongAnnotation);
            boolean isCompensateContainsPathAnnotation = methodWithCompensate.getAnnotations().stream().anyMatch(a -> a.annotationType().equals(Path.class));
            if(!isCompensateContainsPathAnnotation) {
                throw new DeploymentException(getCompensateMissingErrMsg.apply(Path.class));
            }
            boolean isCompensateContainsPostAnnotation = methodWithCompensate.getAnnotations().stream().anyMatch(a -> a.annotationType().equals(POST.class));
            if(!isCompensateContainsPostAnnotation) {
                throw new DeploymentException(getCompensateMissingErrMsg.apply(POST.class));
            }
        }
        
        if(methodsWithStatus.size() > 0) {
            // @Status - requires @Path and @GET
            final AnnotatedMethod<? super X> methodWithStatus = methodsWithStatus.get(0);
            Function<Class<?>, String> getStatusMissingErrMsg = (wrongAnnotation) ->
            getMissingAnnotationError(methodWithStatus, classAnnotatedWithLra, Status.class, wrongAnnotation);
            boolean isStatusContainsPathAnnotation = methodWithStatus.getAnnotations().stream().anyMatch(a -> a.annotationType().equals(Path.class));
            if(!isStatusContainsPathAnnotation) {
                throw new DeploymentException(getStatusMissingErrMsg.apply(Path.class));
            }
            boolean isStatusContainsPostAnnotation = methodWithStatus.getAnnotations().stream().anyMatch(a -> a.annotationType().equals(GET.class));
            if(!isStatusContainsPostAnnotation) {
                throw new DeploymentException(getStatusMissingErrMsg.apply(GET.class));
            }
        }

        if(methodsWithComplete.size() > 0) {
            // @Complete - requires @Path and @POST
            final AnnotatedMethod<? super X> methodWithComplete = methodsWithComplete.get(0);
            Function<Class<?>, String> getCompleteMissingErrMsg = (wrongAnnotation) ->
                getMissingAnnotationError(methodWithComplete, classAnnotatedWithLra, Complete.class, wrongAnnotation);
            boolean isCompleteContainsPathAnnotation = methodWithComplete.getAnnotations().stream().anyMatch(a -> a.annotationType().equals(Path.class));
            if(!isCompleteContainsPathAnnotation) {
                throw new DeploymentException(getCompleteMissingErrMsg.apply(Path.class));
            }
            boolean isCompleteContainsPostAnnotation = methodWithComplete.getAnnotations().stream().anyMatch(a -> a.annotationType().equals(POST.class));
            if(!isCompleteContainsPostAnnotation) {
                throw new DeploymentException(getCompleteMissingErrMsg.apply(POST.class));
            }
        }

        if(methodsWithLeave.size() > 0) {
            // @Leave - requires @PUT
            final AnnotatedMethod<? super X> methodWithLeave = methodsWithLeave.get(0);
            boolean isLeaveContainsPutAnnotation = methodWithLeave.getAnnotations().stream().anyMatch(a -> a.annotationType().equals(PUT.class));
            if(!isLeaveContainsPutAnnotation) {
                throw new DeploymentException(getMissingAnnotationError(methodWithLeave, classAnnotatedWithLra, Leave.class, PUT.class));
            }
        }
        
        if(methodsWithForget.size() > 0) {
            // @Forget - requires @DELETE
            final AnnotatedMethod<? super X> methodWithForget = methodsWithForget.get(0);
            boolean isForgetContainsPutAnnotation = methodWithForget.getAnnotations().stream().anyMatch(a -> a.annotationType().equals(DELETE.class));
            if(!isForgetContainsPutAnnotation) {
                throw new DeploymentException(getMissingAnnotationError(methodWithForget, classAnnotatedWithLra, Forget.class, DELETE.class));
            }
        }
    }

    private String getMissingAnnotationError(AnnotatedMethod<?> method, ProcessAnnotatedType<?> classAnnotated,
        Class<?> lraTypeAnnotation, Class<?> complementaryAnnotation) {
        return String.format("Method '%s' of class '%s' annotated by '%s' should use complementary annotation %s",
            method.getJavaMember().getName(), classAnnotated.getAnnotatedType().getJavaClass().getName(),
            lraTypeAnnotation.getName(), complementaryAnnotation.getName());
    }
}
