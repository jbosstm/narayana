package org.jboss.narayana.compensations.functional;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.xts.bytemanSupport.participantCompletion.ParticipantCompletionCoordinatorRules;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

/**
 * @author paul.robinson@redhat.com 29/04/2014
 */
@RunWith(Arquillian.class)
public class BasicTestLocal extends BasicTest {

    @Deployment
    public static WebArchive createTestArchive() {

        WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackage("org.jboss.narayana.compensations.functional")
                .addPackage("org.jboss.narayana.compensations.functional.common")
                .addClass(ParticipantCompletionCoordinatorRules.class)
                .addAsManifestResource("services/javax.enterprise.inject.spi.Extension")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        return archive;
    }
}
