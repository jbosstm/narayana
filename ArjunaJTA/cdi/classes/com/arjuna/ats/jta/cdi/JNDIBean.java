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
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.CreationException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Singleton;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;

/**
 * A {@link Bean} that {@linkplain #create(CreationalContext) creates}
 * its instances by {@linkplain Context#lookup(Name) looking them up}
 * in a JNDI {@link Context}.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
final class JNDIBean<T> extends AbstractBean<T> {

    private final String name;

    private final Class<? extends T> type;

    JNDIBean(final String name, final Class<? extends T> type) {
        super();
        this.name = Objects.requireNonNull(name);
        this.type = Objects.requireNonNull(type);
    }

    JNDIBean(final Name name, final Class<? extends T> type) {
        this(Objects.requireNonNull(name).toString(), Objects.requireNonNull(type));
    }

    @Override
    public final T create(final CreationalContext<T> cc) {
        try {
            return this.type.cast(CDI.current().select(InitialContext.class).get().lookup(this.name));
        } catch (final NamingException namingException) {
            throw new CreationException(namingException.getMessage(), namingException);
        }
    }

    @Override
    public final Set<Type> getTypes() {
        return Collections.singleton(this.type);
    }

    @Override
    public final Class<? extends Annotation> getScope() {
        return Singleton.class;
    }

    @Override
    protected String getTypeName() {
        return type.getName();
    }

}