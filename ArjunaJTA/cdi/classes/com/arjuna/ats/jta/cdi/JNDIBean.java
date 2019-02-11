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
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.CreationException;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.enterprise.util.AnnotationLiteral;
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
public class JNDIBean<T> implements Bean<T>, PassivationCapable {

    private final Set<Annotation> qualifiers;
    
    private final String name;

    private final Class<? extends T> type;

    private final Class<? extends Annotation> scope;

    /**
     * Creates a new {@link JNDIBean}.
     *
     * @param name the name of the object to retrieve; must not be {@code null}
     *
     * @param type the {@link Class} to which the object retrieved
     * will be {@linkplain Class#cast(Object) cast}; must not be
     * {@code null}
     *
     * @param scope the scope that will be returned from the {@link
     * #getScope()} method; may be {@code null} in which case {@link
     * Dependent Dependent.class} will be used instead
     *
     * @exception NullPointerException if any parameter value is
     * {@code null}
     */
    public JNDIBean(final String name, final Class<? extends T> type, final Class<? extends Annotation> scope) {
        super();
        this.name = Objects.requireNonNull(name);
        this.type = Objects.requireNonNull(type);
        this.scope = scope == null ? Dependent.class : scope;
        this.qualifiers = new HashSet<>();
        this.qualifiers.add(AnyLiteral.INSTANCE);
        this.qualifiers.add(DefaultLiteral.INSTANCE);
    }

    /**
     * {@linkplain #getNewInitialContext() Acquires an initial
     * <code>Context</code>} and uses it to {@linkplain
     * Context#lookup(String) look up} the object logically identified
     * by the {@code name} and {@code type} supplied {@linkplain
     * #JNDIBean(String, Class, Class) at construction time}.
     *
     * <p>This method may return {@code null}.</p>
     *
     * @param cc a {@link CreationalContext}; may be {@code null}
     *
     * @return the object sought, or {@code null}
     *
     * @exception NullPointerException if the {@link
     * #getNewInitialContext()} method returns {@code null}
     *
     * @exception ClassCastException if the retrieved object could not
     * be cast to the {@link Class} supplied {@linkplain
     * #JNDIBean(String, Class, Class) at construction time}
     *
     * @exception CreationException if a {@link NamingException}
     * occurs
     */
    @Override
    public final T create(final CreationalContext<T> cc) {
        Context initialContext = null;
        CreationException e = null;
        try {
            initialContext = this.getNewInitialContext();
            return this.type.cast(initialContext.lookup(this.name));
        } catch (final NamingException namingException) {            
            e = new CreationException(namingException.getMessage(), namingException);
            throw e;
        } finally {
            if (initialContext != null) {
                try {
                    initialContext.close();
                } catch (final NamingException namingException) {
                    if (e != null) {
                        e.addSuppressed(namingException);
                    } else {
                        e = new CreationException(namingException.getMessage(), namingException);
                    }
                    throw e;
                }
            }
        }
    }

    @Override
    public void destroy(final T instance, final CreationalContext<T> cc) {
        if (cc != null) {
            cc.release();
        }
    }
    
    @Override
    public Class<?> getBeanClass() {
        return this.getClass();
    }

    @Override
    public String getId() {
        final String COMMA = ",";
        final String CLASS_DELIMITER = "%";
        return getBeanClass().getName() + CLASS_DELIMITER
                + getName() + COMMA
                + getScope().getName() + COMMA
                + isAlternative() + COMMA
                + getQualifiers() + COMMA
                + getStereotypes() + COMMA
                + getTypes();
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return this.qualifiers;
    }

    @Override
    public String getName() {
        return null;
    }

    /**
     * Returns a new {@link Context} suitable for {@linkplain
     * Context#lookup(String) looking up} objects.
     *
     * <p>This method never returns {@code null}.</p>
     *
     * <p>Overrides of this method must not return {@code null}.</p>
     *
     * <p>Overrides of this method must return {@link Context}s that
     * may be safely {@linkplain Context#close() closed}.
     *
     * @return a new {@link Context}; never {@code null}
     *
     * @exception NamingException if a {@link Context} could not be
     * acquired
     */
    protected Context getNewInitialContext() throws NamingException {
        return new InitialContext();
    }
    
    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    @Override
    public final Set<Type> getTypes() {
        return Collections.singleton(this.type);
    }

    @Override
    public final Class<? extends Annotation> getScope() {
        return this.scope;
    }

    @Override
    public boolean isAlternative() {
        return false;
    }

    @Override
    public final boolean isNullable() {
        return false;
    }

    private static final class AnyLiteral extends AnnotationLiteral<Any> implements Any {

        private static final Any INSTANCE = new AnyLiteral();

    }

    private static final class DefaultLiteral extends AnnotationLiteral<Default> implements Default {

        private static final Default INSTANCE = new DefaultLiteral();

    }

}
