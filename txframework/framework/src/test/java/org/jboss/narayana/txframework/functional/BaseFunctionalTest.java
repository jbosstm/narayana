package org.jboss.narayana.txframework.functional;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

public class BaseFunctionalTest
{
    @Deployment
    public static JavaArchive createTestArchive()
    {
        //todo: Does the application developer have to specify the interceptor?
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "test.jar")
                .addPackages(true, "org.jboss.narayana.txframework")
                .addAsResource("jaxws-handlers-jaxws-service.xml")
                .addAsManifestResource("persistence.xml")
                .addAsManifestResource(new ByteArrayAsset("<interceptors><class>org.jboss.narayana.txframework.impl.ServiceRequestInterceptor</class></interceptors>".getBytes()),
                        ArchivePaths.create("beans.xml"));

        archive.delete(ArchivePaths.create("META-INF/MANIFEST.MF"));

        String ManifestMF = "Manifest-Version: 1.0\n"
                + "Dependencies: org.jboss.as.xts,org.jboss.xts,org.jboss.modules,deployment.arquillian-service,org.jboss.msc,org.jboss.jts\n";
        archive.setManifest(new StringAsset(ManifestMF));

        return archive;
    }
}
