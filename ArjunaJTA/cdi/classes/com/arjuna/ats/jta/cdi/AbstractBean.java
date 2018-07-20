/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2018, Red Hat, Inc., and individual contributors
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

package com.arjuna.ats.jta.cdi;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.AnnotationLiteral;

/**
 * An opinionated and skeletal {@link Bean} implementation.  It is, by
 * default, {@linkplain #getScope() in <code>Dependent</code> scope},
 * has {@linkplain #getQualifiers() only the <code>Default</code>
 * qualifier}, has {@linkplain #getStereotypes() no stereotypes},
 * {@linkplain #isAlternative() is not an alternative}, has
 * {@linkplain #getInjectionPoints() no injection points} and
 * {@linkplain #destroy(Object, CreationalContext) does not need
 * cleanup}.
 *
 * <p>Subclasses may override any of the methods in this class to
 * customize its behavior.</p>
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
abstract class AbstractBean<T> implements Bean<T> {

    protected AbstractBean() {
        super();
    }
    
    @Override
    public Class<?> getBeanClass() {
        return this.getClass();
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public void destroy(final T instance, final CreationalContext<T> cc) {

    }

    @Override
    public Set<Annotation> getQualifiers() {
        return Collections.singleton(DefaultLiteral.INSTANCE);
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return Dependent.class;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    @Override
    public boolean isAlternative() {
        return false;
    }

    private static final class DefaultLiteral extends AnnotationLiteral<Default> implements Default {

        private static final Default INSTANCE = new DefaultLiteral();
        
    }
    
}
