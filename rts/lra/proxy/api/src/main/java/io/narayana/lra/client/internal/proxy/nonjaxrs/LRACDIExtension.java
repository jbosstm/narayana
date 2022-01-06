/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2019, Red Hat, Inc., and individual contributors
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

import io.narayana.lra.client.internal.proxy.nonjaxrs.jandex.DotNames;
import io.narayana.lra.client.internal.proxy.nonjaxrs.jandex.JandexAnnotationResolver;
import io.narayana.lra.logging.LRALogger;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.util.AnnotationLiteral;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This CDI extension collects all LRA participants that contain
 * one or more non-JAX-RS participant methods. The collected classes are stored
 * in {@link LRAParticipantRegistry}.
 */
public class LRACDIExtension implements Extension {

    private ClassPathIndexer classPathIndexer = new ClassPathIndexer();
    private Index index;
    private final Map<String, LRAParticipant> participants = new HashMap<>();

    public void observe(@Observes AfterBeanDiscovery event, BeanManager beanManager) throws IOException, ClassNotFoundException {
        index = classPathIndexer.createIndex();

        List<AnnotationInstance> annotations = index.getAnnotations(DotName.createSimple("jakarta.ws.rs.Path"));

        for (AnnotationInstance annotation : annotations) {
            ClassInfo classInfo;
            AnnotationTarget target = annotation.target();

            if (target.kind().equals(AnnotationTarget.Kind.CLASS)) {
                classInfo = target.asClass();
            } else if (target.kind().equals(AnnotationTarget.Kind.METHOD)) {
                classInfo = target.asMethod().declaringClass();
            } else {
                continue;
            }

            LRAParticipant participant = getAsParticipant(classInfo);
            if (participant != null) {
                participants.put(participant.getJavaClass().getName(), participant);
                Set<Bean<?>> participantBeans = beanManager.getBeans(participant.getJavaClass(), new AnnotationLiteral<Any>() {});
                if (participantBeans.isEmpty()) {
                    // resource is not registered as managed bean so register a custom managed instance
                    try {
                        participant.setInstance(participant.getJavaClass().newInstance());
                    } catch (InstantiationException | IllegalAccessException e) {
                        LRALogger.i18nLogger.error_cannotProcessParticipant(e);
                    }
                }
            }
        }

        event.addBean()
            .read(beanManager.createAnnotatedType(LRAParticipantRegistry.class))
            .beanClass(LRAParticipantRegistry.class)
            .scope(ApplicationScoped.class)
            .createWith(context -> new LRAParticipantRegistry(participants));
    }

    /**
     * Collects all non-JAX-RS participant methods in the defined Java class
     *
     * @param classInfo a Jandex class info of the class to be scanned
     * @return Collected methods wrapped in {@link LRAParticipant} class or null if no non-JAX-RS methods have been found
     */
    private LRAParticipant getAsParticipant(ClassInfo classInfo) throws ClassNotFoundException {
        Class<?> javaClass;
        String className = classInfo.name().toString();
        try {
            javaClass = getClass().getClassLoader().loadClass(className);
        } catch (ClassNotFoundException cnfe) {
            javaClass = Thread.currentThread().getContextClassLoader().loadClass(className);
        }

        if (javaClass.isInterface() || Modifier.isAbstract(javaClass.getModifiers()) || !isLRAParticipant(classInfo)) {
            return null;
        }

        LRAParticipant participant = new LRAParticipant(javaClass);
        return participant.hasNonJaxRsMethods() ? participant : null;
    }

    /**
     * Returns whether the classinfo represents an LRA participant --
     * Class contains LRA method and either one or both of Compensate and/or AfterLRA methods.
     *
     * @param classInfo Jandex class object to scan for annotations
     *
     * @return true if the class is a valid LRA participant, false otherwise
     * @throws IllegalStateException if there is LRA annotation but no Compensate or AfterLRA is found
     */
    private boolean isLRAParticipant(ClassInfo classInfo) {
        Map<DotName, List<AnnotationInstance>> annotations = JandexAnnotationResolver.getAllAnnotationsFromClassInfoHierarchy(classInfo.name(), index);

        if (!annotations.containsKey(DotNames.LRA)) {
            return false;
        } else if (!annotations.containsKey(DotNames.COMPENSATE) && !annotations.containsKey(DotNames.AFTER_LRA)) {
            throw new IllegalStateException(String.format("%s: %s",
                classInfo.name(), "The class contains an LRA method and no Compensate or AfterLRA method was found."));
        } else {
            return true;
        }
    }
}
