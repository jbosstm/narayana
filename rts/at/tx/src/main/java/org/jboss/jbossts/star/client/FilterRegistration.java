/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.star.client;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Provider
public class FilterRegistration implements DynamicFeature {
    private boolean isRegistered;

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext ctx) {
        if (!isRegistered) {
            Method method = resourceInfo.getResourceMethod();
            Annotation transactional = method.getDeclaredAnnotation(Transactional.class);

            if (transactional != null || method.getDeclaringClass().getDeclaredAnnotation(Transactional.class) != null) {
                ctx.register(ServerSRAFilter.class);
                isRegistered = true;
            }
        }
    }
}