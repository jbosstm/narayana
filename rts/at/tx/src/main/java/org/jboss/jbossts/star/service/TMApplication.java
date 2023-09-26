/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.star.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.ws.rs.core.Application;

import org.jboss.jbossts.star.logging.RESTATLogger;
import org.jboss.jbossts.star.provider.HttpResponseMapper;
import org.jboss.jbossts.star.provider.NotFoundMapper;
import org.jboss.jbossts.star.provider.TMUnavailableMapper;
import org.jboss.jbossts.star.provider.TransactionStatusMapper;
import org.jboss.jbossts.star.resource.RESTRecord;
import org.jboss.logging.Logger;

import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeManager;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeMap;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;

public class TMApplication extends Application {
    private static final Logger log = Logger.getLogger(TMApplication.class);

    HashSet<Object> singletons = new HashSet<Object>();
    Set<Class<?>> classes = new HashSet<Class<?>> ();

    public TMApplication(Class<?> ... extraClasses) {
        this();

        try {
            Collections.addAll(classes, extraClasses);
        } catch (Throwable e) {
          RESTATLogger.atI18NLogger.warn_jaxrsTM(e.getMessage(), e);
        }
    }
    public TMApplication() {
//        singletons.addAll(Arrays.asList(resources));
        try {
            // TODO move com/arjuna/ats/jbossatx/jt[as]/TransactionManagerService.isRecoveryManagerRunning
            // to RecoveryManager and change logging
            // by default do not colocate the coordinator and recovery manager
            if ("true".equals(System.getProperty("recovery", "false")))
                RecoveryManager.manager();

           // register RESTRecord record type so that it is persisted in the object store correctly
           RecordTypeManager.manager().add(new RecordTypeMap() {
               public Class<? extends AbstractRecord> getRecordClass () {
                   return RESTRecord.class;
                   }
               public int getType () {
                   return RecordType.RESTAT_RECORD;
                   }
           });

            Collections.addAll(classes, resourceClasses);
            Collections.addAll(classes, mappers);
        } catch (Throwable e) {
          RESTATLogger.atI18NLogger.warn_jaxrsTM(e.getMessage(), e);
        }
    }

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }

    private static Class<?>[] mappers = {
        TMUnavailableMapper.class,
        TransactionStatusMapper.class,
        HttpResponseMapper.class,
        NotFoundMapper.class
    };

    private static Class<?>[] resourceClasses = {
            Coordinator.class,
    };
}