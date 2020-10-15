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

package io.narayana.lra.arquillian.client;

import io.narayana.lra.LRAData;
import io.narayana.lra.arquillian.Deployer;
import io.narayana.lra.client.NarayanaLRAClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.net.URI;
import java.util.List;

@RunWith(Arquillian.class)
public class NarayanaLRAClientIT {

    @Inject
    private NarayanaLRAClient lraClient;

    @Deployment
    public static WebArchive deploy() {
        return Deployer.deploy(NarayanaLRAClientIT.class.getSimpleName());
    }

    @Test
    public void testGetAllLRAs() {
        URI lra = lraClient.startLRA("test-lra");

        List<LRAData> allLRAs = lraClient.getAllLRAs();

        Assert.assertTrue(allLRAs.stream().anyMatch(lraData -> lraData.getLraId().equals(lra.toASCIIString())));
    }

}
