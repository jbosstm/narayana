/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2020, Red Hat, Inc., and individual contributors
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
package io.narayana.lra.client.internal.proxy.nonjaxrs;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Annotation resolver that resolves annotation in a matter similar to JAX-RS/Jakarta REST definitions.
 */
public class AnnotationResolver {

    /**
     * Finds the annotation on the method with the following criteria:
     *
     * 1. Find the annotation on method directly. If not present,
     * 2. Find the annotation on the same method in the superclass (superclass hierarchy). If not present,
     * 3. Find the annotation on the same method in the implemented interfaces (and interfaces implemented by
     * its superclasses). If not found return null.
     *
     * @param annotationClass annotation to look for
     * @param method method to scan
     * @param <T> the actual type of the annotation
     * @return the found annotation object or null if not found
     */
    public static <T extends Annotation> T resolveAnnotation(Class<T> annotationClass, Method method) {
        // current method
        T annotation = method.getAnnotation(annotationClass);
        if (annotation != null) {
            return annotation;
        }

        // search the superclass hierarchy
        annotation = resolveAnnotationInSuperClass(annotationClass, method, method.getDeclaringClass().getSuperclass());
        if (annotation != null) {
            return annotation;
        }

        // search the implemented interfaces in the hierarchy
        annotation = resolveAnnotationInInterfaces(annotationClass, method, method.getDeclaringClass());
        if (annotation != null) {
            return annotation;
        }

        return null;
    }

    /**
     * Returns whether or not the queried annotation is present on the method.
     *
     * @see AnnotationResolver#resolveAnnotation
     * @return true if the annotation is found, false otherwise
     */
    public static boolean isAnnotationPresent(Class<? extends Annotation> annotationClass, Method method) {
        return resolveAnnotation(annotationClass, method) != null;
    }

    private static <T extends Annotation> T resolveAnnotationInSuperClass(Class<T> annotationClass, Method method, Class<?> clazz) {
        if (clazz == null) {
            return null;
        }

        try {
            Method superclassMethod = clazz.getMethod(method.getName(), method.getParameterTypes());
            T annotation = superclassMethod.getAnnotation(annotationClass);
            return annotation != null ? annotation : resolveAnnotationInSuperClass(annotationClass, method, clazz.getSuperclass());
        } catch (NoSuchMethodException e) {
            return resolveAnnotationInSuperClass(annotationClass, method, clazz.getSuperclass());
        }
    }

    private static <T extends Annotation> T resolveAnnotationInInterfaces(Class<T> annotationClass, Method method, Class<?> clazz) {
        if (clazz == null) {
            return null;
        }

        T annotation;
        Method interfaceMethod;

        for (Class<?> anInterface : clazz.getInterfaces()) {
            try {
                interfaceMethod = anInterface.getMethod(method.getName(), method.getParameterTypes());
                annotation = interfaceMethod.getAnnotation(annotationClass);

                if (annotation != null) {
                    return annotation;
                }

            } catch (NoSuchMethodException e) {
                continue;
            }
        }

        return resolveAnnotationInInterfaces(annotationClass, method, clazz.getSuperclass());
    }
}
