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

import com.arjuna.mw.wst11.BusinessActivityManager;
import com.arjuna.mw.wst11.BusinessActivityManagerFactory;
import com.arjuna.wst.BusinessAgreementWithCoordinatorCompletionParticipant;
import com.arjuna.wst.FaultedException;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst11.ConfirmCompletedParticipant;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
@Stateless
@WebService(name = "TestServiceBA", targetNamespace = "http://arjuna.com/wstx/tests/common", serviceName = "TestServiceBAService", portName = "TestServiceBA")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@HandlerChain(file = "/context-handlers.xml")
public class TestServiceBAImple implements TestServiceBA, BusinessAgreementWithCoordinatorCompletionParticipant,
        ConfirmCompletedParticipant, Serializable {

    private static final long serialVersionUID = 1L;

    private static final AtomicInteger counter = new AtomicInteger();

    private static volatile List<String> businessActivityInvocations = new ArrayList<String>();

    @Override
    public void increment() {
        try {
            BusinessActivityManager activityManager = BusinessActivityManagerFactory.businessActivityManager();
            activityManager.enlistForBusinessAgreementWithCoordinatorCompletion(this, "TestService:" + UUID.randomUUID());
        } catch (NullPointerException e) {
            // Ignore if no activity
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
    public List<String> getBusinessActivityInvocations() {
        return businessActivityInvocations;
    }

    @Override
    public void reset() {
        businessActivityInvocations.clear();
        counter.set(0);
    }

    @Override
    public void close() throws WrongStateException, SystemException {
        businessActivityInvocations.add("close");
    }

    @Override
    public void cancel() throws FaultedException, WrongStateException, SystemException {
        businessActivityInvocations.add("cancel");
    }

    @Override
    public void compensate() throws FaultedException, WrongStateException, SystemException {
        businessActivityInvocations.add("compensate");
        counter.decrementAndGet();
    }

    @Override
    public String status() throws SystemException {
        businessActivityInvocations.add("status");
        return null;
    }

    @Override
    @Deprecated
    public void unknown() throws SystemException {
        businessActivityInvocations.add("unknown");
    }

    @Override
    public void error() throws SystemException {
        businessActivityInvocations.add("error");
    }

    @Override
    public void confirmCompleted(boolean confirmed) {
        businessActivityInvocations.add("confirmCompleted");

        if (!confirmed) {
            counter.decrementAndGet();
        }
    }

    @Override
    public void complete() throws WrongStateException, SystemException {
        businessActivityInvocations.add("complete");
    }

}