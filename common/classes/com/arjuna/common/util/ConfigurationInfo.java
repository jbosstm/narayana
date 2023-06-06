/*
 * SPDX short identifier: Apache-2.0
 */

package com.arjuna.common.util;

import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Utility class providing access to build time and runtime configuration reporting functions.
 *
 * Replaces the old per-module Info (and in some cases Configuration and report) classes.
 *
 * The actual build information is injected during the build via the
 * Maven Resources Plugin
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2009-10
 */
public class ConfigurationInfo
{
    private static final String SOURCE_ID;
    private static final String PROPERTIES_FILE_NAME;
    private static final String BUILD_ID;

    static {
        Properties configInfoProps = new Properties();
        String sourceId = "(unknown)";
        String propertiesFileName = "(unknown)";
        String buildId = "(unknown)";
        try (final InputStream stream = ConfigurationInfo.class.getResourceAsStream("/ConfigurationInfo.properties")) {
            if (stream != null)
                try (final InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                    configInfoProps.load(reader);
                    sourceId = configInfoProps.getProperty("sourceId", sourceId);
                    propertiesFileName = configInfoProps.getProperty("propertiesFileName", propertiesFileName);
                    buildId = configInfoProps.getProperty("buildId", buildId);
                }
        } catch (IOException ignored) {
        }
        SOURCE_ID = sourceId;
        PROPERTIES_FILE_NAME = propertiesFileName;
        BUILD_ID = buildId;
    }

    /**
     * @see getSourceId
     * @return the version, if known.
     */
    public static String getVersion() {
        return getSourceId();
    }

    /**
     * @return the version control tag of the source used, or "unknown".
     */
    public static String getSourceId() {
        return SOURCE_ID; // reads the property from properties file in whilch the value of keys are replaced at build time
    }

    /**
     * @return the name (not path) of the properties file
     */
    public static String getPropertiesFile() {
        return PROPERTIES_FILE_NAME; // define a property as <propertiesFileName></propertiesFileName> in pom.xml
    }

    /**
     * @return the build identification line indicating the os name and version and build date
     */
    public static String getBuildId() {
        return BUILD_ID; // reads the property from properties file in whilch the value of keys are replaced at build time
    }

    /**
     * Print config info to stdout.
     * @param args unused
     */
    public static void main (String[] args)
    {
        // build time info:
        System.out.println("sourceId: "+getSourceId());
        System.out.println("propertiesFile: "+getPropertiesFile());

        // run time info (probably empty as beans only load on demand):
        String beans = BeanPopulator.printState();
        System.out.print(beans);
    }
}