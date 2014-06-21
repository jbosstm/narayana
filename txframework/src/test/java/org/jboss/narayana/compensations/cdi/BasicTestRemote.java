package org.jboss.narayana.compensations.cdi;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.xts.bytemanSupport.BMScript;
import org.jboss.jbossts.xts.bytemanSupport.participantCompletion.ParticipantCompletionCoordinatorRules;
import org.jboss.narayana.compensations.cdi.common.DummyCompensationHandler1;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

/**
 * @author paul.robinson@redhat.com 29/04/2014
 */
@RunWith(Arquillian.class)
public class BasicTestRemote extends BasicTest {

        @Deployment
        public static JavaArchive createTestArchive() {

            JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "test.jar")
                    .addPackage(BasicTestLocal.class.getPackage())
                    .addPackage(DummyCompensationHandler1.class.getPackage())
                    .addClass(ParticipantCompletionCoordinatorRules.class)
                    .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

            return archive;
        }


        @BeforeClass()
        public static void submitBytemanScript() throws Exception {

            BMScript.submit(ParticipantCompletionCoordinatorRules.RESOURCE_PATH);
        }

        @AfterClass()
        public static void removeBytemanScript() {

            BMScript.remove(ParticipantCompletionCoordinatorRules.RESOURCE_PATH);
        }
}
