/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
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

package org.jboss.narayana.compensations.internal.context;

import org.jboss.narayana.compensations.api.CompensationScoped;

import javax.enterprise.context.ContextException;
import javax.enterprise.context.spi.AlterableContext;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.PassivationCapable;
import java.lang.annotation.Annotation;

/**
 * CDI context for {@link CompensationScoped} marked beans.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class CompensationContext implements AlterableContext {

    private final CompensationContextStateManager compensationContextStateManager;

    public CompensationContext(CompensationContextStateManager compensationContextStateManager) {
        this.compensationContextStateManager = compensationContextStateManager;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return CompensationScoped.class;
    }

    @Override
    public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
        if (contextual == null) {
            throw new ContextException("contextual is null");
        }
        if (!(contextual instanceof PassivationCapable)) {
            return null;
        }

        PassivationCapable bean = (PassivationCapable) contextual;
        Object resource = compensationContextStateManager.getCurrent().getResource(bean.getId());
        if (resource != null) {
            return (T) resource;
        }

        if (creationalContext != null) {
            T t = contextual.create(creationalContext);
            compensationContextStateManager.getCurrent().addResource(bean.getId(), t);
            return t;
        }

        return null;
    }

    @Override
    public <T> T get(Contextual<T> contextual) {
        return get(contextual, null);
    }

    @Override
    public boolean isActive() {
        return compensationContextStateManager.isActive();
    }

    @Override
    public void destroy(Contextual<?> contextual) {
        if (contextual instanceof PassivationCapable) {
            PassivationCapable bean = (PassivationCapable) contextual;
            compensationContextStateManager.getCurrent().removeResource(bean.getId());
        }
    }

}
