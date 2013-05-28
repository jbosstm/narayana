package com.hp.mwtests.ts.jta.cdi.transactional;


import javax.ejb.EJB;
import javax.inject.Inject;
import javax.transaction.Transaction;
import javax.transaction.Transactional;
import javax.transaction.UserTransaction;

/**
 * @author paul.robinson@redhat.com 02/05/2013
 */
@Transactional(Transactional.TxType.NEVER)
public class TestTransactionalBean {


    @EJB
    TestSessionBean testSessionBean;

    @Inject
    UserTransaction userTransaction;

    public void invokeWithCLassLevelDefault() throws Exception {

        Utills.assertTransactionActive(false);
    }

    @Transactional
    public void invokeWithDefault() throws Exception {

        AssertionParticipant.enlist();
        Utills.assertTransactionActive(true);
    }

    @Transactional
    public void invokeWithDefault(Transaction expectedTransaction) throws Exception {

        AssertionParticipant.enlist();
        Utills.assertTransactionActive(true);
        Utills.assertSameTransaction(expectedTransaction);
    }

    @Transactional
    public void invokeWithDefault(Class<? extends Throwable> throwable) throws Throwable {

        AssertionParticipant.enlist();
        throw throwable.newInstance();
    }


    @Transactional(rollbackOn = TestException.class)
    public void invokeWithDefaultAndRollbackOn(Class<? extends Throwable> throwable) throws Throwable {

        AssertionParticipant.enlist();
        throw throwable.newInstance();
    }

    @Transactional(dontRollbackOn = TestRuntimeException.class)
    public void invokeWithDefaultAndDontRollbackOn(Class<? extends Throwable> throwable) throws Throwable {

        AssertionParticipant.enlist();
        throw throwable.newInstance();
    }

    @Transactional(dontRollbackOn = TestException.class, rollbackOn = TestException.class)
    public void invokeWithDefaultAndDoAndDontRollbackOn(Class<? extends Throwable> throwable) throws Throwable {

        AssertionParticipant.enlist();
        throw throwable.newInstance();
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void invokeWithRequiresNew(Transaction oldTransaction) throws Exception {

        AssertionParticipant.enlist();
        Utills.assertTransactionActive(true);
        Utills.assertDifferentTransaction(oldTransaction);
    }

    @Transactional(value=Transactional.TxType.MANDATORY)
    public void invokeWithMandatory(Transaction expectedTransaction) throws Exception {

        AssertionParticipant.enlist();
        Utills.assertTransactionActive(true);
        Utills.assertSameTransaction(expectedTransaction);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public void invokeWithSupports(Transaction expectedTransaction) throws Exception {

        if (expectedTransaction != null) {
            AssertionParticipant.enlist();
            Utills.assertTransactionActive(true);
        }
        Utills.assertSameTransaction(expectedTransaction);
    }

    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    public void invokeWithNotSupported() throws Exception {

        Utills.assertTransactionActive(false);
    }

    @Transactional(Transactional.TxType.NEVER)
    public void invokeWithNever() throws Exception {

        Utills.assertTransactionActive(false);
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void invokeWithRequiresNewUseUserTransaction() throws Exception {

        userTransaction.getStatus();
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void invokeWithRequiredUseUserTransaction() throws Exception {

        userTransaction.getStatus();
    }

    @Transactional(Transactional.TxType.NEVER)
    public void invokeWithNeverUseUserTransaction() throws Exception {

        userTransaction.getStatus();
    }

    @Transactional(Transactional.TxType.MANDATORY)
    public void invokeWithMandatoryUseUserTransaction() throws Exception {

        userTransaction.getStatus();
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public void invokeWithSupportsUseUserTransaction() throws Exception {

        userTransaction.getStatus();
    }

    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    public void invokeWithNotSupportedUseUserTransaction() throws Exception {

        userTransaction.getStatus();
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void invokeEJB() throws Exception {

        testSessionBean.invokeWithRequired();
    }

}
