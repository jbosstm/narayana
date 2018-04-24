package org.jboss.narayana.tomcat.jta.integration;

import io.narayana.db.Allocator;
import io.narayana.db.DB;
import io.narayana.db.DBAllocator;
import io.narayana.db.H2Allocator;
import org.apache.commons.lang.StringUtils;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Shared functionality for test cases, mainly DB allocation.
 *
 * @author <a href="mailto:karm@redhat.com">Michal Karm Babacek</a>
 */
public abstract class AbstractCase {

    private static final Logger LOGGER = Logger.getLogger(AbstractCase.class.getName());

    /**
     * The deployment is the same for each test, but context.xml and DB driver jar
     * changes according to the database used.
     */
    static WebArchive webArchive;
    static Allocator dba;
    static DB db;
    private static File[] libFiles;
    static String catalinaHome;

    /**
     * Acquire resources and prepare configuration
     */
    @BeforeClass
    public static void init() {
        try {
            dba = Allocator.getInstance();
            LOGGER.info("Allocating a new database might take many minutes, depending on the mode the test suite operates in.");
            db = dba.allocateDB();
            assertNotNull("Failed to allocate DB. Check logs for the root cause.", db);
            // Configuration of deployment, driver, data sources XML, ...
            prepareContextXML();
            final File[] dbDriver = (dba instanceof DBAllocator) ? new File[]{new File(db.dbDriverArtifact)} :
                    Maven.resolver().resolve(db.dbDriverArtifact).withTransitivity().asFile();
            assertNotNull("WebArchive was not created by @Deployment before @BeforeClass. Arquillian lifecycle config error?", webArchive);
            webArchive.addAsLibraries(dbDriver);
            webArchive.addAsManifestResource("context.xml", "context.xml");

            final String versionTXSpec = System.getProperty("version.org.jboss.spec.javax.transaction");
            if (StringUtils.isEmpty(versionTXSpec)) {
                throw new IllegalArgumentException("version.org.jboss.spec.javax.transaction must not be empty");
            }
            catalinaHome = System.getenv("CATALINA_HOME");
            if (StringUtils.isEmpty(catalinaHome)) {
                throw new IllegalArgumentException("CATALINA_HOME must not be empty");
            }
            // @see https://issues.jboss.org/browse/JWS-976
            libFiles = Maven.resolver()
                    .resolve("org.jboss.spec.javax.transaction:jboss-transaction-api_1.2_spec:" + versionTXSpec)
                    .withTransitivity().asFile();
            final String dest = catalinaHome + File.separator + "lib" + File.separator + libFiles[0].getName();
            try {
                Files.copy(Paths.get(libFiles[0].toURI()),
                        Paths.get(dest),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Either CATALINA_HOME or version.org.jboss.spec.javax.transaction is invalid, we cannot copy to " + dest, e);
                fail(e.getMessage());
            }
        } catch (IOException | NullPointerException e) {
            LOGGER.log(Level.SEVERE, "Web app war archive generating or /lib dir configuration failed.", e);
            if (dba != null && db != null) {
                dba.deallocateDB(db);
            }
            fail(e.getMessage());
        }
    }

    /**
     * Release resources
     */
    @AfterClass
    public static void clean() {
        if (dba != null && db != null) {
            dba.deallocateDB(db);
        }
        // @see https://issues.jboss.org/browse/JWS-976
        if (libFiles != null && libFiles[0] != null) {
            final String file0 = catalinaHome + File.separator + "lib" + File.separator + libFiles[0].getName();
            try {
                Files.delete(Paths.get(file0));
            } catch (IOException e) {
                LOGGER.info(file0 + " not found.");
            }
        }
    }

    /**
     * Edits context.xml so as it reflects the current database being used.
     */
    private static void prepareContextXML() {
        try {
            final File contextXML = new File(URLDecoder.decode(AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                @Override
                public ClassLoader run() {
                    return Thread.currentThread().getContextClassLoader();
                }
            }).getResource("context.xml").getFile(), "UTF-8"));

            final Document context = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(contextXML);

            final Element dbSource = context.createElement("Resource");
            dbSource.setAttribute("name", "myDataSource");
            dbSource.setAttribute("uniqueName", "myDataSource");
            dbSource.setAttribute("auth", "Container");
            dbSource.setAttribute("type", db.dsType);
            dbSource.setAttribute("username", db.dsUsername);
            dbSource.setAttribute("user", db.dsUser);
            dbSource.setAttribute("password", db.dsPassword);
            dbSource.setAttribute("url", db.dsUrl);
            dbSource.setAttribute("description", "Data Source");
            dbSource.setAttribute("loginTimeout", db.dsLoginTimeout);
            dbSource.setAttribute("factory", db.dsFactory);
            if (!(dba instanceof H2Allocator)) {
                dbSource.setAttribute("databaseName", db.dsDbName);
                dbSource.setAttribute("portNumber", db.dsDbPort);
                dbSource.setAttribute("serverName", db.dsDbHostname);
            }
            context.getDocumentElement().appendChild(dbSource);

            final Element tsDbSource = context.createElement("Resource");
            tsDbSource.setAttribute("name", "transactionalDataSource");
            tsDbSource.setAttribute("uniqueName", "transactionalDataSource");
            tsDbSource.setAttribute("auth", "Container");
            tsDbSource.setAttribute("type", db.tdsType);
            tsDbSource.setAttribute("username", db.dsUser);
            tsDbSource.setAttribute("password", db.dsPassword);
            tsDbSource.setAttribute("transactionManager", "TransactionManager");
            tsDbSource.setAttribute("xaDataSource", "myDataSource");
            tsDbSource.setAttribute("description", "Transactional Driver Data Source");
            // Connection pool settings
            tsDbSource.setAttribute("factory", "org.jboss.narayana.tomcat.jta.TransactionalDataSourceFactory");
            tsDbSource.setAttribute("initialSize", "10");
            tsDbSource.setAttribute("maxWaitMillis", "10000");
            tsDbSource.setAttribute("maxTotal", "4");
            tsDbSource.setAttribute("maxIdle", "16");
            tsDbSource.setAttribute("minIdle", "8");
            tsDbSource.setAttribute("maxAge", "30000");
            tsDbSource.setAttribute("testOnBorrow", "true");
            tsDbSource.setAttribute("validationQuery", "select 1");
            tsDbSource.setAttribute("removeAbandonedTimeout", "60");
            tsDbSource.setAttribute("removeAbandoned", "true");
            tsDbSource.setAttribute("logAbandoned", "true");
            tsDbSource.setAttribute("jmxEnabled", "true");
            context.getDocumentElement().appendChild(tsDbSource);

            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(context), new StreamResult(contextXML));
        } catch (ParserConfigurationException | TransformerException | SAXException e) {
            fail("Failed to parse, update and serialize web app's context.xml for data source configuration.");
        } catch (IOException e) {
            fail("Failed to locate context.xml to process.");
        }
    }
}
