package org.jboss.narayana.compensations.impl.remote;

import com.arjuna.wst.SystemException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst11.BAParticipantManager;
import org.jboss.narayana.compensations.impl.ParticipantManager;

import javax.xml.namespace.QName;

/**
 * @author paul.robinson@redhat.com 19/04/2014
 */
public class RemoteParticipantManager implements ParticipantManager {

    BAParticipantManager baParticipantManager;

    public RemoteParticipantManager(BAParticipantManager baParticipantManager) {

        this.baParticipantManager = baParticipantManager;
    }

    @Override
    public void exit() throws WrongStateException, UnknownTransactionException, SystemException {

        baParticipantManager.exit();
    }

    @Override
    public void completed() throws WrongStateException, UnknownTransactionException, SystemException {

        baParticipantManager.completed();
    }

    @Override
    public void cannotComplete() throws WrongStateException, UnknownTransactionException, SystemException {

        baParticipantManager.cannotComplete();
    }

    @Override
    public void fail(QName exceptionIdentifier) throws SystemException {

        baParticipantManager.fail(exceptionIdentifier);
    }
}
