/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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