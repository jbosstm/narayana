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

package io.narayana.lra;

import org.junit.Assert;
import org.junit.Test;

import java.net.URI;

public class LRAConstantsTest {

    @Test
    public void getCoordinatorFromUsualLRAId() {
        URI lraId = URI.create("http://localhost:8080/lra-coordinator/0_ffff0a28054b_9133_5f855916_a7?query=1#fragment");
        URI coordinatorUri = LRAConstants.getLRACoordinatorUrl(lraId);
        Assert.assertEquals("http", coordinatorUri.getScheme());
        Assert.assertEquals("localhost", coordinatorUri.getHost());
        Assert.assertEquals(8080, coordinatorUri.getPort());
        Assert.assertEquals("/lra-coordinator", coordinatorUri.getPath());
        Assert.assertNull(coordinatorUri.getQuery());
        Assert.assertNull(coordinatorUri.getFragment());
        Assert.assertEquals("http://localhost:8080/lra-coordinator", coordinatorUri.toASCIIString());
    }

    @Test
    public void getCoordinatorWithMultipleCoordinatorPaths() {
        URI lraId = URI.create("http://198.10.0.10:8999/lra-coordinator/lra-coordinator");
        URI coordinatorUri = LRAConstants.getLRACoordinatorUrl(lraId);
        Assert.assertEquals("http://198.10.0.10:8999/lra-coordinator/lra-coordinator", coordinatorUri.toASCIIString());
    }
}
