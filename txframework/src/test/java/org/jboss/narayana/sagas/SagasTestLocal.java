/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
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
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package org.jboss.narayana.sagas;

import com.arjuna.mw.wscf.model.sagas.api.CoordinatorManager;
import com.arjuna.mw.wscf.model.sagas.exceptions.CoordinatorCancelledException;
import com.arjuna.mw.wscf.protocols.ProtocolRegistry;
import com.arjuna.mw.wscf11.model.sagas.CoordinatorManagerFactory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(Arquillian.class)
public class SagasTestLocal {

    private static final String ManifestMF = "Manifest-Version: 1.0\n"
            + "Dependencies: org.jboss.xts,org.jboss.xts,org.jboss.msc,org.jboss.jts\n";

    @Deployment
    public static WebArchive createTestArchive() {

        WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war");
        archive.addClass(Participant.class);

        archive.delete(ArchivePaths.create("META-INF/MANIFEST.MF"));
        archive.setManifest(new StringAsset(ManifestMF));

        return archive;
    }

    @Before
    public void initSagas() {
        ProtocolRegistry.sharedManager().initialise();
    }

    @Test
    public void completeTest() throws Exception {
        CoordinatorManager cm = CoordinatorManagerFactory.coordinatorManager();

        cm.begin("Sagas11HLS");
        cm.enlistParticipant(new Participant("p1"));
        cm.enlistParticipant(new Participant("p2"));
        cm.participantCompleted("p1");
        cm.participantCompleted("p2");
        cm.close();

    }

    @Test
    public void cancelTest() throws Exception {
        CoordinatorManager cm = CoordinatorManagerFactory.coordinatorManager();

        cm.begin("Sagas11HLS");
        cm.enlistParticipant(new Participant("p1"));
        cm.enlistParticipant(new Participant("p2"));
        cm.participantCompleted("p1");
        cm.participantCompleted("p2");
        cm.cancel();

    }

    @Test(expected = CoordinatorCancelledException.class)
    public void cannotComplete() throws Exception {
        CoordinatorManager cm = CoordinatorManagerFactory.coordinatorManager();

        cm.begin("Sagas11HLS");
        cm.enlistParticipant(new Participant("p1"));
        cm.enlistParticipant(new Participant("p2"));
        cm.participantCompleted("p1");
        cm.participantCannotComplete("p2");
        cm.close();

    }
}
