import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.narayana.blacktie.administration.Authentication;
import org.jboss.narayana.blacktie.administration.BlacktieAdministration;
import org.jboss.narayana.blacktie.administration.BlacktieStompAdministrationService;
import org.jboss.narayana.blacktie.administration.core.AdministrationProxy;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.Message;
import org.jboss.narayana.blacktie.jatmibroker.core.tx.TransactionException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.BlackTieService;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.mdb.MDBBlacktieService;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.impl.X_OCTET_Impl;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class AdvertiseUnadvertiseTest {
    private static final Logger log = LogManager.getLogger(AdvertiseUnadvertiseTest.class);

    @Deployment
    public static Archive<?> createTestArchive() {
        final String ManifestMF = "Manifest-Version: 1.0\n"
                + "Dependencies: org.jboss.jts,org.jboss.as.controller-client,org.jboss.dmr\n";
        return ShrinkWrap
                .create(WebArchive.class, "test.war")
                .addClasses(BlacktieStompAdministrationService.class, Authentication.class, AdministrationProxy.class,
                        BlacktieAdministration.class)
                        .addPackage(MDBBlacktieService.class.getPackage())
                        .addPackage(BlackTieService.class.getPackage())
                        .addPackage(TransactionException.class.getPackage())
                        .addPackage(ConfigurationException.class.getPackage())
                        .addPackage(Message.class.getPackage())
                        .addPackage(X_OCTET_Impl.class.getPackage())
                        .addAsResource("btconfig.xsd")
                .addAsResource("btconfig.xml").setManifest(new StringAsset(ManifestMF));
    }

    @Test
    public void testAdvertiseUnadvertise() throws Exception {
        BlacktieStompAdministrationService service = new BlacktieStompAdministrationService();
        log.info("Got the service");

        try {
            new InitialContext().lookup("java:/queue/BTR_.testsui1");
            fail("Should not be able to resolve the queue before it is created");
        } catch (NameNotFoundException e) {
            // Expected
            log.info("Got the exception");
        }

        assertTrue(service.deployQueue(".testsui1", "testui", false, "queue", "5.2.10.Final") == 1);
        try {
            new InitialContext().lookup("java:/queue/BTR_.testsui1");
            log.info("Got the queue");
        } catch (NameNotFoundException e) {
            try {
                assertTrue(service.decrementConsumer(".testsui1") == 1);
            } finally {
                fail("Could not resolve the queue");
            }
        }
        assertTrue(service.decrementConsumer(".testsui1") == 1);
        log.info("Undeployed queue");
        try {
            new InitialContext().lookup("java:/queue/BTR_.testsui1");
            fail("Should not be able to resolve the queue after it is destroyed");
        } catch (NameNotFoundException e) {
            // Expected
            log.info("Got the exception 2");
        }

    }
}
