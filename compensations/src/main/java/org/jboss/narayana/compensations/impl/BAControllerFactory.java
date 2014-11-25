package org.jboss.narayana.compensations.impl;

import com.arjuna.mw.wscf.protocols.ProtocolRegistry;
import org.jboss.narayana.compensations.impl.local.LocalBAControler;
import org.jboss.narayana.compensations.impl.remote.RemoteBAControler;

/**
 * @author paul.robinson@redhat.com 19/04/2014
 */
public class BAControllerFactory {

    public static BAControler getInstance() {

        BAControler remoteBAControler = getRemoteInstance();
        if (remoteBAControler.isBARunning()) {
            return remoteBAControler;
        }
        BAControler localBAControler = getLocalInstance();
        if (localBAControler.isBARunning()) {
            return localBAControler;
        }
        return localBAControler;
    }

    public static BAControler getRemoteInstance() {

        return new RemoteBAControler();
    }

    public static BAControler getLocalInstance() {

        ProtocolRegistry.sharedManager().initialise();
        return new LocalBAControler();
    }

    public static boolean isLocalTransactionRunning() {

        if (getRemoteInstance().isBARunning()) {
            return false;
        }
        BAControler localBAControler = getLocalInstance();
        return localBAControler.isBARunning();
    }

    public static boolean isRemoteTransactionRunning() {

        return getRemoteInstance().isBARunning();
    }

}
