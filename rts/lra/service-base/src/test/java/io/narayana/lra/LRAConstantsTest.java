/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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