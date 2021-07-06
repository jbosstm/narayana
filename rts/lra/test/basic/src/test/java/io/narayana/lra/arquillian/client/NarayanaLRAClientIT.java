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
