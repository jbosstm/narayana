package org.jboss.jbossts.txframework.functional;

import com.arjuna.mw.wst11.UserBusinessActivity;
import com.arjuna.mw.wst11.UserBusinessActivityFactory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.jbossts.txframework.functional.clients.BACoordinatorCompletionClient;
import org.jboss.jbossts.txframework.functional.interfaces.BACoordinatorCompletion;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import java.lang.annotation.Annotation;
import java.util.Arrays;

public class BaseFunctionalTest
{
    @Deployment
    public static JavaArchive createTestArchive()
    {
        //todo: Does the application developer have to specify the interceptor?
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "test.jar")
                .addPackages(true, "org.jboss.jbossts.txframework")
                .addAsResource("context-handlers.xml")
                .addAsManifestResource(new ByteArrayAsset("<interceptors><class>org.jboss.jbossts.txframework.impl.ServiceRequestInterceptor</class></interceptors>".getBytes()),
                        ArchivePaths.create("beans.xml"));

        archive.delete(ArchivePaths.create("META-INF/MANIFEST.MF"));

        String ManifestMF = "Manifest-Version: 1.0\n"
                + "Dependencies: org.jboss.xts,org.jboss.modules,deployment.arquillian-service,org.jboss.msc,org.jboss.jts\n";
        archive.setManifest(new StringAsset(ManifestMF));

        return archive;
    }
}
