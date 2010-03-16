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

import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import org.apache.log4j.Logger;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * Implementation of XAResourceRecoveryHelper for use in txbridge recovery tests.
 * Provides persistence for TestXAResource via a file in the ObjectStore.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-01
 */
public class TestXAResourceRecoveryHelper implements XAResourceRecoveryHelper
{
    private static Logger log = Logger.getLogger(TestXAResourceRecoveryHelper.class);

    private static final TestXAResourceRecoveryHelper instance = new TestXAResourceRecoveryHelper();

    private final Set<Xid> preparedXids = new HashSet<Xid>();

    public static TestXAResourceRecoveryHelper getInstance()
    {
        return instance;
    }

    protected TestXAResourceRecoveryHelper() {}

    /**
     * MC lifecycle callback, used to register the recovery module with the transaction manager.
     */
    public void start()
    {
        log.info("TestXAResourceRecoveryHelper starting");

        getRecoveryModule().addXAResourceRecoveryHelper(getInstance());

        recoverFromDisk();
    }

    /**
     * MC lifecycle callback, used to unregister the recovery module from the transaction manager.
     */
    public void stop()
    {
        log.info("TestXAResourceRecoveryHelper stopping");

        getRecoveryModule().removeXAResourceRecoveryHelper(getInstance());
    }

    private XARecoveryModule getRecoveryModule()
    {
        for(RecoveryModule recoveryModule : ((Vector<RecoveryModule>) RecoveryManager.manager().getModules())) {
            if(recoveryModule instanceof XARecoveryModule) {
                return (XARecoveryModule)recoveryModule;
            }
        }
        return null;
    }

    @Override
    public boolean initialise(String param) throws Exception
    {
        log.trace("initialise(param="+param+")");

        return true;
    }

    @Override
    public XAResource[] getXAResources() throws Exception
    {
        log.trace("getXAResources()");

        XAResource values[] = new XAResource[1];
        values[0] = new TestXAResourceRecovered();

        return values;
    }

    ///////////////////////////

    public void logPrepared(Xid xid) throws XAException
    {
        log.trace("logPrepared(xid="+xid+")");

        synchronized (preparedXids) {
            if(preparedXids.add(xid)) {
                writeToDisk();
            } else {
                throw new XAException(XAException.XAER_PROTO);
            }
        }
    }

    public void removeLog(Xid xid) throws XAException
    {
        log.trace("removeLog(xid="+xid);

        synchronized (preparedXids) {
            if(preparedXids.remove(xid)) {
                writeToDisk();
            } else {
                log.trace("no log present for "+xid);
            }
        }
    }

    public Xid[] recover()
    {
        log.trace("recover()");

        return preparedXids.toArray(new Xid[preparedXids.size()]);
    }


    private void writeToDisk() {
        File logFile = getLogFile();
        log.trace("logging "+preparedXids.size()+" records to "+logFile.getAbsolutePath());

        try {
            FileOutputStream fos = new FileOutputStream(logFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(preparedXids);
            oos.close();
            fos.close();
        } catch(IOException e) {
            log.error(e);
        }
    }

    private void recoverFromDisk() {
        File logFile = getLogFile();
        log.trace("recovering from "+logFile.getAbsolutePath());

        if(!logFile.exists()) {
            return;
        }

        try {
            FileInputStream fis = new FileInputStream(logFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Set<Xid> xids = (Set<Xid>)ois.readObject();
            preparedXids.addAll(xids);
            log.trace("Recovered "+xids+" Xids");
            ois.close();
            fis.close();
        } catch(Exception e) {
            log.error(e);
        }

    }

    private File getLogFile() {
        String parentDir = arjPropertyManager.getObjectStoreEnvironmentBean().getObjectStoreDir();
        String childDir = arjPropertyManager.getObjectStoreEnvironmentBean().getLocalOSRoot();
        File logDir = new File(parentDir, childDir);
        File logFile = new File(logDir, "TestXAResource.ser");
        return logFile;        
    }


}