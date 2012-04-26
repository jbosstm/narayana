package org.jboss.narayana.txframework.functional.rest.at.simpleEJB;

import junit.framework.Assert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Commit;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Prepare;
import org.jboss.narayana.txframework.functional.common.EventLog;
import org.jboss.narayana.txframework.functional.common.ServiceCommand;
import org.jboss.narayana.txframework.functional.common.SomeApplicationException;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.util.Arrays;

/**
 * @Author paul.robinson@redhat.com 06/04/2012
 */
@RunWith(Arquillian.class)
public class ClientTXInterceptorTest {

    @Inject
    Client client;

    @Deployment
    public static WebArchive createTestArchive() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackage("org.jboss.narayana.txframework.functional.rest.at.simpleEJB")
                .addClasses(EventLog.class, SomeApplicationException.class, ServiceCommand.class)
                .addAsWebInfResource(new ByteArrayAsset("<interceptors><class>org.jboss.narayana.txframework.impl.handlers.restat.client.RestTXRequiredInterceptor</class><class>org.jboss.narayana.txframework.impl.ServiceRequestInterceptor</class></interceptors>".getBytes()),
                        ArchivePaths.create("beans.xml"))
                .addAsWebInfResource("resttx.ejb.web.xml", "web.xml");
        archive.delete(ArchivePaths.create("META-INF/MANIFEST.MF"));

        String ManifestMF = "Manifest-Version: 1.0\n"
                + "Dependencies: org.jboss.resteasy.resteasy-jaxrs,javax.ws.rs.api,javax.ejb.api,org.jboss.jts,org.jboss.narayana.txframework\n";
        archive.setManifest(new StringAsset(ManifestMF));

        return archive;

    }

    @After
    public void teardownTest() throws Exception {
        client.clearLogs();
        //rollbackIfActive(ut);
    }

    @Test
    public void clientDrivenCommitTest() throws Exception {
        client.invoke();
        assertOrder(Prepare.class, Prepare.class, Commit.class, Commit.class);
    }


    private void assertOrder(Class<? extends Annotation>... expectedOrder) {
        String eventLog = client.getEventLog();
        Assert.assertEquals(EventLog.asString(Arrays.asList(expectedOrder)), eventLog);
    }
}
