import javax.transaction.TransactionManager;

import org.junit.Test;

import com.arjuna.ats.jta.common.jtaPropertyManager;

public class TransactionManagerTest {


    @Test
    public void testSettingUpTransactionManager() throws Exception {
        TransactionManager tm = jtaPropertyManager.getJTAEnvironmentBean().getTransactionManager();

        tm.begin();

        System.err.println( tm.getTransaction() );

        tm.rollback();
        System.err.println( tm.getTransaction() );
    }

}
