/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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

        if (classInfo != null) {
            annotations.putAll(classInfo.annotationsMap());
            annotations.putAll(getInterfaceAnnotations(classInfo.interfaceNames(), index));
            annotations.putAll(getAllAnnotationsFromClassInfoHierarchy(classInfo.superName(), index));
        }

        return annotations;
    }

    private static Map<DotName, List<AnnotationInstance>> getInterfaceAnnotations(List<DotName> interfaceNames, Index index) {
        Map<DotName, List<AnnotationInstance>> annotations = new HashMap<>();
        ClassInfo interfaceClassInfo = null;

        for (DotName interfaceName : interfaceNames) {
            interfaceClassInfo = index.getClassByName(interfaceName);
            Map<DotName, List<AnnotationInstance>> interfaceAnnotations = interfaceClassInfo.annotationsMap();
            annotations.forEach((k, v) -> interfaceAnnotations.merge(k, v, (v1, v2) -> {
                v1.addAll(v2);
                return v1;
            }));
        }

        return annotations;
    }
}