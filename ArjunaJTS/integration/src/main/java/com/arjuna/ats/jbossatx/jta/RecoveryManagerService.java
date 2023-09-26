/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.jbossatx.jta;

import java.util.Vector;

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
public class RecoveryManagerService implements XAResourceRecoveryRegistry
{
    private RecoveryManager _recoveryManager;

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

        _recoveryManager.initialize();
        _recoveryManager.startRecoveryManagerThread() ;
    }

    public void stop() throws Exception
    {
        jbossatxLogger.i18NLogger.info_jta_RecoveryManagerService_stop();

        _recoveryManager.terminate();
    }

    public void suspend()
    {
        jbossatxLogger.i18NLogger.info_jta_RecoveryManagerService_suspend();
        _recoveryManager.suspend(false);
    }

    public void resume()
    {
        jbossatxLogger.i18NLogger.info_jta_RecoveryManagerService_resume();
        _recoveryManager.resume();
    }

    //////////////////////////////

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
    }

    public void removeXAResourceRecovery(XAResourceRecovery xaResourceRecovery)
    {
        if(_recoveryManager == null) {
            throw new IllegalStateException(jbossatxLogger.i18NLogger.get_jta_RecoveryManagerService_norecoverysystem());
        }

        XARecoveryModule xaRecoveryModule = null;
        for(RecoveryModule recoveryModule : ((Vector <RecoveryModule>)_recoveryManager.getModules())) {
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
}