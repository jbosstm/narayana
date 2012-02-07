package org.jboss.narayana.txframework.functional;

import junit.framework.Assert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.narayana.txframework.functional.clients.JAXWSHandlerAnnotationClient;
import org.jboss.narayana.txframework.functional.common.SimpleHandler;
import org.jboss.narayana.txframework.functional.interfaces.JAXWSHandlerAnnotation;
import org.jboss.narayana.txframework.functional.services.JAXWSHandlerAnnotatonImpl;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author paul.robinson@redhat.com, 2012-02-06
 */
@RunWith(Arquillian.class)
public class JAXWSHandlerAnnotatonTest {

    @Deployment
    public static JavaArchive createTestArchive() {
        //todo: Does the application developer have to specify the interceptor?
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "test.jar")
                .addAsResource("jaxws-handlers-jaxws-service.xml")
                .addClass(SimpleHandler.class)
                .addClass(JAXWSHandlerAnnotationClient.class)
                .addClass(JAXWSHandlerAnnotation.class)
                .addClass(JAXWSHandlerAnnotatonImpl.class);

        archive.delete(ArchivePaths.create("META-INF/MANIFEST.MF"));

        String ManifestMF = "Manifest-Version: 1.0\n"
                + "Dependencies: org.jboss.modules,deployment.arquillian-service\n";
        archive.setManifest(new StringAsset(ManifestMF));

        return archive;
    }

    @Test
    public void echoTest() throws Exception {
        JAXWSHandlerAnnotation client = JAXWSHandlerAnnotationClient.newInstance();
        Assert.assertEquals("Hello Paul", client.sayHello("Paul"));
        //Assert.assertTrue("Expected handler to be called 5 times, was called " + MyServerHandler.getCount() + " times", MyServerHandler.getCount() == 5);
    }

}
