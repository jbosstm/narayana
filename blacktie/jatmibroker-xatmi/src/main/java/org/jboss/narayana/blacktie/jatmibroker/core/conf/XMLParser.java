/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.jboss.narayana.blacktie.jatmibroker.core.conf;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Class to create a parser and parse an XML file
 */
public class XMLParser {

    private static final Logger log = LogManager.getLogger(XMLParser.class);

    private DefaultHandler handler;
    private SAXParser saxParser;
    private Schema schema;

    private Properties properties = new Properties();

    private static Map<URL, XMLParser> parsedFiles = new HashMap<URL, XMLParser>();

    /**
     * Constructor
     * 
     * @param handler - DefaultHandler for the SAX parser
     */
    private XMLParser(String xsdFilename, String env) throws ConfigurationException {
        this.handler = new XMLEnvHandler(properties);
        log.debug("Creating the parser: " + xsdFilename);
        try {
            String schemaDir = System.getenv("BLACKTIE_SCHEMA_DIR");
            if (schemaDir != null) {
                log.debug("SCHEMA DIR: " + schemaDir);
                schemaDir = schemaDir + System.getProperty("file.separator");
                xsdFilename = schemaDir + xsdFilename;
            }

            // Obtain a new instance of a SAXParserFactory.
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(true);

            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            URL resource = Thread.currentThread().getContextClassLoader().getResource(xsdFilename);
            if (resource != null) {
                schema = schemaFactory.newSchema(resource);
            } else {
                File file = new File(xsdFilename);
                if (file.exists()) {
                    schema = schemaFactory.newSchema(file);
                } else {
                    throw new ConfigurationException("Could not load the schema: " + xsdFilename);
                }
            }
            factory.setSchema(schema);

            saxParser = factory.newSAXParser();
        } catch (SAXException e) {
            log.error("Could not create a SAXParser: " + e.getMessage(), e);
            throw new ConfigurationException("Could not create a SAXParser: " + e.getMessage(), e);
        } catch (ParserConfigurationException e) {
            log.error("Could not create a SAXParser: " + e.getMessage(), e);
            throw new ConfigurationException("Could not create a SAXParser: " + e.getMessage(), e);
        } catch (Throwable e) {
            log.error("Could not parse configuration: " + e.getMessage(), e);
            throw new ConfigurationException("Could not parse configuration: " + e.getMessage(), e);
        }
        String configDir = System.getenv("BLACKTIE_CONFIGURATION_DIR");
        if (configDir != null && !configDir.equals("")) {
            env = configDir + "/" + env;
        }

        log.debug("read " + env + " configuration from " + configDir + " directory");

        // File file = new File(env);
        InputStream resource = Thread.currentThread().getContextClassLoader().getResourceAsStream(env);
        if (resource != null) {
            try {
                schema.newValidator().validate(new StreamSource(resource));
                saxParser.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream(env), handler);
            } catch (Throwable t) {
                throw new ConfigurationException("Errors parse : " + env + " due to: " + t.getMessage(), t);
            }
        } else {
            throw new ConfigurationException("Could not load the configuration file: " + env + " please update your CLASSPATH");
        }
    }

    public Properties getLoadedProperties() {
        return properties;
    }

    public static void loadProperties(String schemaName, String configFile, Properties prop) throws ConfigurationException {
        log.trace("Detecting the filename");
        String env = configFile;
        String configDir = System.getenv("BLACKTIE_CONFIGURATION_DIR");
        if (configDir != null && !configDir.equals("")) {
            env = configDir + "/" + env;
        }
        URL resource = Thread.currentThread().getContextClassLoader().getResource(env);
        if (resource == null) {
            throw new ConfigurationException("Could not find the file: " + env);
        }
        log.trace("Detected the filename");
        XMLParser parser = parsedFiles.get(resource);
        if (parser == null) {
            log.debug("Configfile not loaded yet: " + resource.getFile());
            parser = new XMLParser(schemaName, configFile);
            parsedFiles.put(resource, parser);
        } else {
            log.debug("Configfile loaded before: " + resource.getFile());
        }
        prop.putAll(parser.getLoadedProperties());
    }
}
