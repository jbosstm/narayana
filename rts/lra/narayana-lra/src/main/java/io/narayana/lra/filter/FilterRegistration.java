/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
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
package io.narayana.lra.filter;

import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Provider
public class FilterRegistration implements DynamicFeature {
    private boolean isRegistered;

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext ctx) {
        if (!isRegistered) {
            Method method = resourceInfo.getResourceMethod();
            Annotation transactional = method.getDeclaredAnnotation(LRA.class);

            if (transactional != null || method.getDeclaringClass().getDeclaredAnnotation(LRA.class) != null) {
                ctx.register(ServerLRAFilter.class);
                isRegistered = true;
            }
        }
    }
}
