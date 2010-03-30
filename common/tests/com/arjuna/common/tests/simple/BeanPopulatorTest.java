package com.arjuna.common.tests.simple;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import com.arjuna.common.internal.util.logging.LoggingEnvironmentBean;
import com.arjuna.common.internal.util.logging.basic.BasicLogEnvironmentBean;

import java.util.Set;
import java.util.HashSet;

public class BeanPopulatorTest
{
    @Test
    public void testLoggingPropertiesPopulation() throws Exception {

        // check that all the Environment properties are looked for
        // by the set of beans which wrap them and conversely that no undefined
        // properties are looked for. i.e. that the Environment and Beans are in sync

        DummyProperties testProperties = new DummyProperties();

        BeanPopulator.configureFromProperties(new LoggingEnvironmentBean(), testProperties);

        Set<String> expectedKeys = new HashSet<String>();

        expectedKeys.add("com.arjuna.common.util.logging.language");
        expectedKeys.add("com.arjuna.common.util.logging.country");

        System.out.println("expected: "+expectedKeys);
        System.out.println("used: "+testProperties.usedKeys);

        assertTrue( testProperties.usedKeys.containsAll(expectedKeys) );
    }

    @Test
    public void testDefaultLogPropertiesPopulation() throws Exception {

        DummyProperties testProperties = new DummyProperties();

        BeanPopulator.configureFromProperties(new BasicLogEnvironmentBean(), testProperties);

        Set<String> expectedKeys = new HashSet<String>();

        expectedKeys.add("com.arjuna.common.util.logging.default.showLogName");
        expectedKeys.add("com.arjuna.common.util.logging.default.showShortLogName");
        expectedKeys.add("com.arjuna.common.util.logging.default.showDate");
        expectedKeys.add("com.arjuna.common.util.logging.default.logFileAppend");
        expectedKeys.add("com.arjuna.common.util.logging.default.defaultLevel");
        expectedKeys.add("com.arjuna.common.util.logging.default.logFile");

        System.out.println("expected: "+expectedKeys);
        System.out.println("used: "+testProperties.usedKeys);

        assertTrue( testProperties.usedKeys.containsAll(expectedKeys) );
    }
}