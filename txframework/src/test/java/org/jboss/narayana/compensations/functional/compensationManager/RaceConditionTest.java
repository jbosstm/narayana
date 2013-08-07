package org.jboss.narayana.compensations.functional.compensationManager;

import com.arjuna.mw.wst11.UserBusinessActivityFactory;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst.WrongStateException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.xts.bytemanSupport.BMScript;
import org.jboss.narayana.compensations.functional.common.SingleService;
import org.jboss.narayana.compensations.impl.CompensationManagerImpl;
import org.jboss.narayana.compensations.impl.CompensationManagerState;
import org.jboss.narayana.txframework.impl.TXDataMapImpl;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.HashMap;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@RunWith(Arquillian.class)
public class RaceConditionTest {

    @Inject
    SingleService singleService;

    @Deployment
    public static JavaArchive createTestArchive() {

        JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "test.jar")
                .addPackages(true, "org.jboss.narayana.compensations.functional")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        archive.delete(ArchivePaths.create("META-INF/MANIFEST.MF"));

        String ManifestMF = "Manifest-Version: 1.0\n"
                + "Dependencies: org.jboss.narayana.txframework,org.jboss.xts\n";

        archive.setManifest(new StringAsset(ManifestMF));

        return archive;
    }

    @BeforeClass()
    public static void submitBytemanScript() throws Exception {
        BMScript.submit("race-condition-test-rules.btm");
    }

    @AfterClass()
    public static void removeBytemanScript() {
        BMScript.remove("race-condition-test-rules.btm");
    }

    @Test
    public void test() throws WrongStateException, SystemException, UnknownTransactionException, TransactionRolledBackException {
        beginBusinessActivity();
        singleService.noTransactionPresent();
        closeBusinessActivity();
    }

    private void beginBusinessActivity() throws WrongStateException, SystemException {
        UserBusinessActivityFactory.userBusinessActivity().begin();
        CompensationManagerImpl.resume(new CompensationManagerState());
        TXDataMapImpl.resume(new HashMap());
    }

    private void closeBusinessActivity() throws WrongStateException, UnknownTransactionException, TransactionRolledBackException, SystemException {
        UserBusinessActivityFactory.userBusinessActivity().close();
        CompensationManagerImpl.suspend();
        TXDataMapImpl.suspend();
    }

}
