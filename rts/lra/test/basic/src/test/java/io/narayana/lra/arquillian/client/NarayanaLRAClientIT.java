/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */


package io.narayana.lra.arquillian.client;

import io.narayana.lra.LRAData;
import io.narayana.lra.arquillian.TestBase;
import org.jboss.logging.Logger;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.net.URI;
import java.util.List;

public class NarayanaLRAClientIT extends TestBase {

    private static final Logger log = Logger.getLogger(NarayanaLRAClientIT.class);

    @Rule
    public TestName testName = new TestName();

    @Override
    public void before() {
        super.before();
        log.info("Running test " + testName.getMethodName());
    }

    @Test
    public void testGetAllLRAs() {
        URI lra = lraClient.startLRA("test-lra");
        lrasToAfterFinish.add(lra);

        List<LRAData> allLRAs = lraClient.getAllLRAs();
        Assert.assertTrue("Expected to find the LRA " + lra + " amongst all active ones: " + allLRAs,
                allLRAs.stream().anyMatch(lraData -> lraData.getLraId().equals(lra)));

        lraClient.closeLRA(lra);

        allLRAs = lraClient.getAllLRAs();
        Assert.assertTrue("LRA " + lra + " was closed but is still referred as active one at: " + allLRAs,
                allLRAs.stream().noneMatch(lraData -> lraData.getLraId().equals(lra)));
    }

}