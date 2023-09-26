/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.common.tests;

import com.arjuna.common.util.ConfigurationInfo;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ConfigurationInfoTest {

    @Before
    public void setUp() {
    }

    @Test
    public void testBuildId() {
        String buildId = System.getProperty("test_build_id");
        assertEquals(buildId, ConfigurationInfo.getBuildId());
    }

    @Test
    public void testPropertyFileName() {
        String propFileName = System.getProperty("test_build_prop_file");
        assertEquals(propFileName, ConfigurationInfo.getPropertiesFile());
    }

    @Test
    public void testVersion() {
        String version = System.getProperty("test_build_version");
        assertEquals(version, ConfigurationInfo.getVersion());
    }
}
