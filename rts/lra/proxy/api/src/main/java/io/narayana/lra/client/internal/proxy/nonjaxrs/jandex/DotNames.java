/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */

package io.narayana.lra.client.internal.proxy.nonjaxrs.jandex;

import org.eclipse.microprofile.lra.annotation.AfterLRA;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.jboss.jandex.DotName;

public class DotNames {

    public static final DotName LRA = DotName.createSimple(org.eclipse.microprofile.lra.annotation.ws.rs.LRA.class.getName());
    public static final DotName COMPENSATE = DotName.createSimple(Compensate.class.getName());
    public static final DotName AFTER_LRA = DotName.createSimple(AfterLRA.class.getName());
    public static final DotName OBJECT = DotName.createSimple(Object.class.getName());
}