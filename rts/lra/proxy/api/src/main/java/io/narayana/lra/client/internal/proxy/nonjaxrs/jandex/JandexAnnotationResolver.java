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
package io.narayana.lra.client.internal.proxy.nonjaxrs.jandex;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JandexAnnotationResolver {

    public static Map<DotName, List<AnnotationInstance>> getAllAnnotationsFromClassInfoHierarchy(DotName name, Index index) {
        Map<DotName, List<AnnotationInstance>> annotations = new HashMap<>();

        if (name == null || name.equals(DotNames.OBJECT)) {
            return annotations;
        }

        ClassInfo classInfo = index.getClassByName(name);
        annotations.putAll(classInfo.annotations());
        annotations.putAll(getInterfaceAnnotations(classInfo.interfaceNames(), index));
        annotations.putAll(getAllAnnotationsFromClassInfoHierarchy(classInfo.superName(), index));

        return annotations;
    }

    private static Map<DotName, List<AnnotationInstance>> getInterfaceAnnotations(List<DotName> interfaceNames, Index index) {
        Map<DotName, List<AnnotationInstance>> annotations = new HashMap<>();
        ClassInfo interfaceClassInfo = null;

        for (DotName interfaceName : interfaceNames) {
            interfaceClassInfo = index.getClassByName(interfaceName);
            Map<DotName, List<AnnotationInstance>> interfaceAnnotations = interfaceClassInfo.annotations();
            annotations.forEach((k, v) -> interfaceAnnotations.merge(k, v, (v1, v2) -> {
                v1.addAll(v2);
                return v1;
            }));
        }

        return annotations;
    }
}
