package org.jboss.narayana.blacktie.jatmibroker.core.conf;

import java.net.URL;

import junit.framework.TestCase;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class TestConfigfileReading extends TestCase {
    private static final Logger log = LogManager.getLogger(TestConfigfileReading.class);

    public void testGetResource() {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("btconfig.xml");
        URL resource2 = Thread.currentThread().getContextClassLoader().getResource("btconfig.xml");

        log.info(resource);
        log.info(resource2);
        assertTrue(resource.equals(resource2));
    }
}
