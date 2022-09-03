/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2010,
 * @author JBoss, by Red Hat.
 */
package org.jboss.jbossts.txbridge.tests.inbound.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.logging.Logger;

import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;

/**
 * Implementation of XAResourceRecoveryHelper for use in txbridge recovery tests.
 * Provides persistence for TestXAResource via a file in the ObjectStore.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-01
 */
@Singleton
@Startup
public class TestXAResourceRecoveryHelper implements XAResourceRecoveryHelper {
    private static Logger log = Logger.getLogger(TestXAResourceRecoveryHelper.class);

    private static final TestXAResourceRecoveryHelper instance = new TestXAResourceRecoveryHelper();
    private static final TestXAResourceRecovered xaResourceInstance = new TestXAResourceRecovered();

    private final List<Xid> preparedXids = new ArrayList<Xid>();

    public static TestXAResourceRecoveryHelper getInstance() {
        return instance;
    }

    /**
     * This is required to be public (not protected) because of 
     * Caused by: org.jboss.as.server.deployment.DeploymentUnitProcessingException: JBAS014227: EJB TestXAResourceRecoveryHelper of type org.jboss.jbossts.txbridge.tests.inbound.utility.TestXAResourceRecoveryHelper must have public default constructor
     * Clearly two instances will be created, one by the container for postConstruct/preDestroy and one by ourselves but as they both use getInstance() the code should work fine. 
     * This is not a new way of working it would have been like that before.
     */
    public TestXAResourceRecoveryHelper() {
    }

    /**
     * MC lifecycle callback, used to register the recovery module with the transaction manager.
     */
    @PostConstruct
    public void postConstruct() {
        log.info("TestXAResourceRecoveryHelper starting");

        getRecoveryModule().addXAResourceRecoveryHelper(getInstance());

        getInstance().recoverFromDisk();
    }

    /**
     * MC lifecycle callback, used to unregister the recovery module from the transaction manager.
     */
    @PreDestroy
    public void preDestroy() {
        log.info("TestXAResourceRecoveryHelper stopping");

        getRecoveryModule().removeXAResourceRecoveryHelper(getInstance());
    }

    private XARecoveryModule getRecoveryModule() {
        for (RecoveryModule recoveryModule : ((Vector<RecoveryModule>) RecoveryManager.manager().getModules())) {
            if (recoveryModule instanceof XARecoveryModule) {
                return (XARecoveryModule) recoveryModule;
            }
        }
        return null;
    }

    @Override
    public boolean initialise(String param) throws Exception {
        log.trace("initialise(param=" + param + ")");

        return true;
    }

    @Override
    public XAResource[] getXAResources() throws Exception {
        log.trace("getXAResources()");

        XAResource values[] = new XAResource[1];
        values[0] = xaResourceInstance;

        return values;
    }

    ///////////////////////////

    public void logPrepared(Xid xid) throws XAException {
        log.trace("logPrepared(xid=" + xid + ")");

        synchronized (preparedXids) {
            if (preparedXids.add(xid)) {
                writeToDisk();
            } else {
                throw new XAException(XAException.XAER_PROTO);
            }
        }
    }

    public void removeLog(Xid xid) throws XAException {
        log.trace("removeLog(xid=" + xid);

        synchronized (preparedXids) {
            if (preparedXids.remove(xid)) {
                writeToDisk();
            } else {
                log.trace("no log present for " + xid);
            }
        }
    }

    public Xid[] recover() {
        log.trace("recover()");

        return preparedXids.toArray(new Xid[preparedXids.size()]);
    }


    private void writeToDisk() {
        File logFile = getLogFile();
        log.tracef("logging %s records to %s with content %s",
                preparedXids.size(), logFile.getAbsolutePath(), preparedXids);

        try {
            FileOutputStream fos = new FileOutputStream(logFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(preparedXids);
            oos.close();
            fos.close();
        } catch (IOException e) {
            log.errorf(e, "cannot write records %s to %s",
                    preparedXids, logFile.getAbsolutePath());
        }
    }

    private void recoverFromDisk() {
        File logFile = getLogFile();
        log.trace("recovering records from " + logFile.getAbsolutePath());

        if (!logFile.exists()) {
            return;
        }

        try {
            FileInputStream fis = new FileInputStream(logFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            List<Xid> xids = (List<Xid>) ois.readObject();
            preparedXids.addAll(xids);
            log.tracef("recovered records %s", xids);
            ois.close();
            fis.close();
        } catch (Exception e) {
            log.errorf(e, "cannot recover records from %s", logFile.getAbsolutePath());
        }
    }

    private File getLogFile() {
        String parentDir = arjPropertyManager.getObjectStoreEnvironmentBean().getObjectStoreDir();
        String childDir = arjPropertyManager.getObjectStoreEnvironmentBean().getLocalOSRoot();
        File logDir = new File(parentDir, childDir);
        logDir.mkdirs();
        File logFile = new File(logDir, "TestXAResource.ser");
        return logFile;
    }

}
