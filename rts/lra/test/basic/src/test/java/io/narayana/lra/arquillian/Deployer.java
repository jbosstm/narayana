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

package io.narayana.lra.arquillian;

import org.eclipse.microprofile.lra.tck.LRAClientOps;
import org.eclipse.microprofile.lra.tck.participant.api.WrongHeaderException;
import org.eclipse.microprofile.lra.tck.service.LRAMetricService;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public class Deployer {

    public static WebArchive deploy(String appName) {
        // manifest for WildFly deployment which requires access to transaction jboss module
        final String ManifestMF = "Manifest-Version: 1.0\n"
            + "Dependencies: org.jboss.jts, org.jboss.logging\n";

        return ShrinkWrap.create(WebArchive.class, appName + ".war")
                .addPackages(true,
                    LRAMetricService.class.getPackage(),
                    org.codehaus.jettison.JSONSequenceTooLargeException.class.getPackage())
                .addClasses(LRAClientOps.class, WrongHeaderException.class)
                .addAsManifestResource(new StringAsset(ManifestMF), "MANIFEST.MF");
    }
}
