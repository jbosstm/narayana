package com.arjuna.wstx.tests.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ejb.Stateless;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import com.arjuna.mw.wst11.TransactionManager;
import com.arjuna.mw.wst11.TransactionManagerFactory;
import com.arjuna.wst.Durable2PCParticipant;
import com.arjuna.wst.Prepared;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst.Vote;
import com.arjuna.wst.WrongStateException;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
@Stateless
@WebService(name = "TestServiceAT", targetNamespace = "http://arjuna.com/wstx/tests/common", serviceName = "TestServiceATService", portName = "TestServiceAT")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@HandlerChain(file = "/context-handlers.xml")
public class TestServiceATImple implements TestServiceAT, Durable2PCParticipant, Serializable {

    private static final long serialVersionUID = 1L;

    private static final AtomicInteger counter = new AtomicInteger();

    private static volatile List<String> twoPhaseCommitInvocations = new ArrayList<String>();

    @Override
    public void increment() {
        try {
            TransactionManager transactionManager = TransactionManagerFactory.transactionManager();
            transactionManager.enlistForDurableTwoPhase(this, "testServiceAT:" + UUID.randomUUID());
        } catch (UnknownTransactionException e) {
            // Ignore if no transaction
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        counter.incrementAndGet();
    }

    @Override
    public int getCounter() {
        return counter.get();
    }

    @Override
    public List<String> getTwoPhaseCommitInvocations() {
        return twoPhaseCommitInvocations;
    }

    @Override
    public void reset() {
        twoPhaseCommitInvocations.clear();
        counter.set(0);
    }

    @Override
    public Vote prepare() throws WrongStateException, SystemException {
        twoPhaseCommitInvocations.add("prepare");
        return new Prepared();
    }

    @Override
    public void commit() throws WrongStateException, SystemException {
        twoPhaseCommitInvocations.add("commit");
    }

    @Override
    public void rollback() throws WrongStateException, SystemException {
        twoPhaseCommitInvocations.add("rollback");
        counter.decrementAndGet();
    }

    @Override
    public void unknown() throws SystemException {
        twoPhaseCommitInvocations.add("unknown");
    }

    @Override
    public void error() throws SystemException {
        twoPhaseCommitInvocations.add("error");
    }

}