/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
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
 * (C) 2009,
 * @author Red Hat Middleware LLC.
 */
package com.arjuna.ats.jbossatx.jta;

import java.io.*;
import java.util.Vector;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.logging.arjunaI18NLogger;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.objectstore.ObjectStoreIterator;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.recovery.Resumable;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.jta.logging.jtaLogger;
import org.jboss.tm.XAResourceRecovery;
import org.jboss.tm.XAResourceRecoveryRegistry;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.internal.jbossatx.jta.XAResourceRecoveryHelperWrapper;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jbossatx.logging.jbossatxLogger;
import com.arjuna.ats.jta.recovery.SerializableXAResourceDeserializer;
import com.arjuna.common.util.ConfigurationInfo;

/**
 * JBoss Transaction Recovery Service.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 * @version $Id$
 */
public class RecoveryManagerService implements XAResourceRecoveryRegistry, Resumable
{
    private static String XARESOURCE_RECOVERY_TYPE = "/XARecovery/Resource";
    private static String SERIALIZABLE_XARESOURCE_DESERIALIZER_TYPE = "/XARecovery/ResourceDeserializer";

    private RecoveryManager _recoveryManager;
    private boolean suspended;

    public void create()
    {
        String tag = ConfigurationInfo.getSourceId();

        jbossatxLogger.i18NLogger.info_jta_RecoveryManagerService_create(tag);

        RecoveryManager.delayRecoveryManagerThread();
        // listener (if any) is created here:
        _recoveryManager = RecoveryManager.manager();
    }

    public void destroy()
    {
    }

    public void start()
    {
        jbossatxLogger.i18NLogger.info_jta_RecoveryManagerService_start();
        _recoveryManager.register(this);
        _recoveryManager.initialize();
        resume();
        _recoveryManager.startRecoveryManagerThread() ;
    }

    public void stop() throws Exception
    {
        jbossatxLogger.i18NLogger.info_jta_RecoveryManagerService_stop();
        suspend();
        _recoveryManager.terminate();
    }

    private XARecoveryModule getXARecoveryModule() {
        if(_recoveryManager == null) {
            throw new IllegalStateException(jbossatxLogger.i18NLogger.get_jta_RecoveryManagerService_norecoverysystem());
        }

        for(RecoveryModule recoveryModule : _recoveryManager.getModules()) {
            if(recoveryModule instanceof XARecoveryModule) {
                return (XARecoveryModule)recoveryModule;
            }
        }

        throw new IllegalStateException(jbossatxLogger.i18NLogger.get_jta_RecoveryManagerService_norecoverymodule());
    }

    public void addXAResourceRecovery(XAResourceRecovery xaResourceRecovery)
    {
        if(_recoveryManager == null) {
            throw new IllegalStateException(jbossatxLogger.i18NLogger.get_jta_RecoveryManagerService_norecoverysystem());
        }

        XARecoveryModule xaRecoveryModule = null;
        for(RecoveryModule recoveryModule : ((Vector<RecoveryModule>)_recoveryManager.getModules())) {
            if(recoveryModule instanceof XARecoveryModule) {
                xaRecoveryModule = (XARecoveryModule)recoveryModule;
                break;
            }
        }

        if(xaRecoveryModule == null) {
            throw new IllegalStateException(jbossatxLogger.i18NLogger.get_jta_RecoveryManagerService_norecoverymodule());
        }

        xaRecoveryModule.addXAResourceRecoveryHelper(new XAResourceRecoveryHelperWrapper(xaResourceRecovery));

        if (xaResourceRecovery instanceof Serializable)
            persistXAResourceRecovery(xaResourceRecovery);
        // TODO remember to include deserialization helpers
    }

    private boolean persistXAResourceRecovery(XAResourceRecovery xaResourceRecovery)
    {
        RecoveryStore recoveryStore = StoreManager.getRecoveryStore();
        OutputObjectState os = new OutputObjectState();
        Uid uid = new Uid();

        try
        {
            ByteArrayOutputStream s = new ByteArrayOutputStream();
            ObjectOutputStream o = new ObjectOutputStream(s);

            o.writeObject(xaResourceRecovery);
            o.close();

            os.packBytes(s.toByteArray());
            s.close();
            recoveryStore.write_committed(uid,  XARESOURCE_RECOVERY_TYPE, os);

            return true;
        } catch (IOException e) {
            jtaLogger.i18NLogger.warn_transaction_arjunacore_threadexception(e);
        } catch (ObjectStoreException e) {
            jtaLogger.i18NLogger.warn_transaction_arjunacore_threadexception(e);
        }

        return false;
    }

    public void removeXAResourceRecovery(XAResourceRecovery xaResourceRecovery)
    {
        if(_recoveryManager == null) {
            throw new IllegalStateException(jbossatxLogger.i18NLogger.get_jta_RecoveryManagerService_norecoverysystem());
        }

        XARecoveryModule xaRecoveryModule = null;
        for(RecoveryModule recoveryModule : _recoveryManager.getModules()) {
            if(recoveryModule instanceof XARecoveryModule) {
                xaRecoveryModule = (XARecoveryModule)recoveryModule;
                break;
            }
        }

        if(xaRecoveryModule == null) {
            throw new IllegalStateException(jbossatxLogger.i18NLogger.get_jta_RecoveryManagerService_norecoverymodule());
        }

        xaRecoveryModule.removeXAResourceRecoveryHelper(new XAResourceRecoveryHelperWrapper(xaResourceRecovery));
    }

	public void addSerializableXAResourceDeserializer(SerializableXAResourceDeserializer serializableXAResourceDeserializer) {

        XARecoveryModule xaRecoveryModule = null;
        for(RecoveryModule recoveryModule : ((Vector<RecoveryModule>)_recoveryManager.getModules())) {
            if(recoveryModule instanceof XARecoveryModule) {
                xaRecoveryModule = (XARecoveryModule)recoveryModule;
                break;
            }
        }

        if(xaRecoveryModule == null) {
            throw new IllegalStateException(jbossatxLogger.i18NLogger.get_jta_RecoveryManagerService_norecoverymodule());
        }

        xaRecoveryModule.addSerializableXAResourceDeserializer(serializableXAResourceDeserializer);
		
	}

    @Override
    public void resume() {
        RecoveryStore recoveryStore = StoreManager.getRecoveryStore();
        ObjectStoreIterator iter = new ObjectStoreIterator(recoveryStore, XARESOURCE_RECOVERY_TYPE);
        Uid u;
        XARecoveryModule xaRecoveryModule = getXARecoveryModule();

        suspended = false;

        while ((u = iter.iterate()) != null && Uid.nullUid().notEquals(u)) {
            try {
                InputObjectState ios = recoveryStore.read_committed(u,XARESOURCE_RECOVERY_TYPE);
                byte[] b = ios.unpackBytes();
                ByteArrayInputStream s = new ByteArrayInputStream(b);
                ObjectInputStream o = new ObjectInputStream(s);
                XAResourceRecovery xaResourceRecovery = (XAResourceRecovery) o.readObject();

                xaRecoveryModule.addXAResourceRecoveryHelper(new XAResourceRecoveryHelperWrapper(xaResourceRecovery));
            } catch (Exception e) {
                tsLogger.i18NLogger.warn_recovery_TransactionStatusConnectionManager_2(e);
            }
        }
    }

    @Override
    public void suspend() {
        suspended = true;
    }

    @Override
    public boolean isSuspended() {
        return suspended;
    }
}
