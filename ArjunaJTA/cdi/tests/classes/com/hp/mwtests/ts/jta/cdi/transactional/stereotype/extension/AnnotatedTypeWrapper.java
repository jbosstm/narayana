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

package com.hp.mwtests.ts.jta.cdi.transactional.stereotype.extension;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.inject.spi.AnnotatedConstructor;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;

public class AnnotatedTypeWrapper<T> implements AnnotatedType<T> {

    private final AnnotatedType<T> wrapped;
    private final Set<Annotation> annotations;

    public AnnotatedTypeWrapper(AnnotatedType<T> wrapped, Set<Annotation> annotations) {
        this.wrapped = wrapped;
        this.annotations = new HashSet<>(annotations);
    }

    public AnnotatedTypeWrapper(ProcessAnnotatedType<T> processAnnType) {
        this(processAnnType.getAnnotatedType(), processAnnType.getAnnotatedType().getAnnotations());
    }

    public void addAnnotation(Annotation annotation) {
        annotations.add(annotation);
    }

    public void addAnnotation(Class<? extends Annotation> classAnnotation) {
        Annotation annotation = new Annotation() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return classAnnotation;
            }
        };
        addAnnotation(annotation);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        for (Annotation annotation : annotations) {
            if (annotationType.isInstance(annotation)) {
                return (A) annotation;
            }
        }
        return null;
    }

    @Override
    public Set<Annotation> getAnnotations() {
        return annotations;
    }

    @Override
    public Type getBaseType() {
        return wrapped.getBaseType();
    }

    @Override
    public Set<Type> getTypeClosure() {
        return wrapped.getTypeClosure();
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        for (Annotation annotation : annotations) {
            if (annotationType.isInstance(annotation)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<AnnotatedConstructor<T>> getConstructors() {
        return wrapped.getConstructors();
    }

    @Override
    public Set<AnnotatedField<? super T>> getFields() {
        return wrapped.getFields();
    }

    @Override
    public Class<T> getJavaClass() {
        return wrapped.getJavaClass();
    }

    @Override
    public Set<AnnotatedMethod<? super T>> getMethods() {
        return wrapped.getMethods();
    }
}