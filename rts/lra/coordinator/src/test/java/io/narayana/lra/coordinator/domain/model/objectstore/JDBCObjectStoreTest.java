package io.narayana.lra.coordinator.domain.model.objectstore;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.internal.arjuna.objectstore.jdbc.JDBCStore;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import io.narayana.lra.LRAData;
import io.narayana.lra.logging.LRALogger;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class JDBCObjectStoreTest extends TestBase {

    @BeforeClass
    public static void start() {
        TestBase.start();
        System.setProperty("com.arjuna.ats.arjuna.common.propertiesFile", "h2jbossts-properties.xml");
    }

    /**
     * This test checks that a new LRA transaction can be created when
     * Narayana is configured to use a JDBC Object Store. This test fails
     * if the Object Store is not set to JDBCStore
     */
    @Test
    public void jdbcStoreTest() {

        String objectStoreType = BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).getObjectStoreType();
        // This test fails if the Object Store is not set to JDBCStore
        assertEquals("The Object Store type should have been set to JDBCStore", JDBCStore.class.getName(), objectStoreType);

        LRALogger.logger.infof("%s: the Object Store type is set to: %s",testName.getMethodName(), objectStoreType);

        // Starts a new LRA
        URI lraIdUri = lraClient.startLRA(testName.getMethodName() + "#newLRA");
        // Checks that the LRA transaction has been created
        assertNotNull("An LRA should have been added to the object store", lraIdUri);
        // Using NarayanaLRAClient, the following statement checks that the status of the new LRA is active
        assertEquals("Expected Active", LRAStatus.Active, getStatus(lraIdUri));

        // Extracts the id from the URI
        String lraId = convertLraUriToString(lraIdUri).replace('_', ':');

        LRAData lraData = getLastCreatedLRA();
        assertEquals("Expected that the LRA transaction just started matches the LRA transaction fetched through the Narayana LRA client",
                lraData.getLraId(),
                lraIdUri);

        // Connecting to the database to double check that everything is fine
        String jdbcAccess = BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).getJdbcAccess();
        Pattern pattern = Pattern.compile(".*URL=(.*);User=(.*);Password=(.*).*");
        Matcher matcher = pattern.matcher(jdbcAccess);
        // In case the RegEx pattern does not work
        Assert.assertTrue(
                String.format("The Arjuna's JDBCAccess string:\n %s\n is not formatted as it should", jdbcAccess),
                matcher.find());

        try (Connection conn = DriverManager.getConnection(matcher.group(1), matcher.group(2), matcher.group(3))) {

            String tablePrefix = BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).getTablePrefix();
            Statement st = conn.createStatement();

            // Simple SQL statement to fetch all data from the (PREFIX)JBOSSTSTXTABLE
            ResultSet resultSet = st.executeQuery("SELECT * FROM " +
                    (Objects.isNull(tablePrefix) ? "" : tablePrefix) +
                    "JBOSSTSTXTABLE");

            // Fetches all info from the first row of the ResultSet
            resultSet.first();
            int dbLraStatus = resultSet.getInt(2);
            String dbLraType = resultSet.getString(3);
            String dbLraId = resultSet.getString(4); // Column where the LRA ID is

            // Checks that the status of the LRA found in the database is ACTIVE
            assertTrue("Expected that the database holds a Long Running Action transaction",
                    dbLraType.contains("LongRunningAction"));

            // Checks that the ID of the LRA created previously is equal to the ID of the LRA found in the database
            assertEquals(
                    String.format("Expected that the database holds an LRA with ID %s", lraId),
                    dbLraId,
                    lraId);

            // Checks that the status of the LRA found in the database is ACTIVE
            assertEquals("Expected that the database holds an active LRA",
                    LRAStatus.Active.ordinal(),
                    dbLraStatus);

        } catch (SQLException sqlException) {
            LRALogger.logger.errorf("%s: %s", testName.getMethodName(), sqlException.getMessage());
            fail(sqlException.getMessage());
        }
    }
}
