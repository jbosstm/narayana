/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.narayana.osgi.jta.internal;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.ObjectStoreIterator;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.tools.log.TransactionTypeManager;
import com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreBrowser;
import com.arjuna.ats.arjuna.tools.osb.util.JMXServer;
import com.arjuna.ats.internal.arjuna.tools.log.EditableTransaction;
import org.jboss.narayana.osgi.jta.ObjStoreBrowserService;

import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:zfeng@redhat.com">Amos Feng</a>
 */

public class ObjStoreBrowserImpl implements ObjStoreBrowserService{
    private ObjStoreBrowser osb;
    private PrintStream printStream;
    private List<String> recordTypes = new ArrayList<String>();
    private String currentType = null;
    private String currentLog = "";
    private boolean attached = false;

    public ObjStoreBrowserImpl(ObjStoreBrowser osb) {
        try {
            printStream = new PrintStream(System.out, true, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            System.err.println("Encoding " + StandardCharsets.UTF_8.name() + " is not supported");
            throw new IllegalStateException(StandardCharsets.UTF_8.name() + " is not supported");
        }
        this.osb = osb;
    }

    @Override
    public void probe() throws MBeanException {
        osb.probe();
    }

    @Override
    public List<String> types() {
        recordTypes.clear();

        InputObjectState types = new InputObjectState();

        try {
            if (StoreManager.getRecoveryStore().allTypes(types)) {
                String typeName;

                do {
                    try {
                        typeName = types.unpackString();
                        if (!recordTypes.contains(typeName))
                            recordTypes.add(typeName);
                    } catch (IOException e) {
                        typeName = "";
                    }
                } while (typeName.length() != 0);
            }
        } catch (ObjectStoreException e) {
            System.out.println(e);
        }

        return recordTypes;
    }

    @Override
    public boolean select(String itype) {
        if (attached) {
            attached = false;
        }

        if (recordTypes.size() == 0) {
            types();
        }

        if (!recordTypes.contains(itype)) {
            printStream.printf("%s is not a valid transaction type%n", itype);
            return false;
        } else {
            currentType = itype;
            return true;
        }
    }

    @Override
    public void list(String itype) {
        MBeanServer mbs = JMXServer.getAgent().getServer();
        Set<ObjectInstance> transactions;
        String osMBeanName = "jboss.jta:type=ObjectStore";

        if (itype != null) {
            if (select(itype) == false) return;
            osMBeanName += ",itype=" + itype;
        } else if (currentType != null) {
            osMBeanName += ",itype=" + currentType;
        } else {
            printStream.printf("No type selected%n");
            return;
        }

        try {
            transactions = mbs.queryMBeans(new ObjectName(osMBeanName + ",*"), null);
        } catch (Exception e) {
            System.out.println(e);
            return;
        }

        for (ObjectInstance oi : transactions) {
            String transactionId = oi.getObjectName().getCanonicalName();

            if (!transactionId.contains("puid") && transactionId.contains("itype")) {
                printStream.printf("Transaction: %s%n", oi.getObjectName());
                printStream.printf("-----------------------------------%n");

                String participantQuery =  transactionId + ",puid=*";
                try {
                    Set<ObjectInstance> participants = mbs.queryMBeans(new ObjectName(participantQuery), null);
                    printAtrributes(printStream, "\t", mbs, oi);
                    printStream.printf("\tParticipants:%n");
                    for (ObjectInstance poi : participants) {
                        printStream.printf("\t\tParticipant: %s%n", poi);
                        printAtrributes(printStream, "\t\t\t", mbs, poi);
                    }
                } catch (Exception e){

                }
                printStream.printf("%n");
            }
        }
    }

    private void printAtrributes(PrintStream printStream, String printPrefix, MBeanServer mbs, ObjectInstance oi)
            throws IntrospectionException, InstanceNotFoundException, ReflectionException {
        MBeanInfo info = mbs.getMBeanInfo( oi.getObjectName() );
        MBeanAttributeInfo[] attributeArray = info.getAttributes();
        int i = 0;
        String[] attributeNames = new String[attributeArray.length];

        for (MBeanAttributeInfo ai : attributeArray)
            attributeNames[i++] = ai.getName();

        AttributeList attributes = mbs.getAttributes(oi.getObjectName(), attributeNames);

        for (javax.management.Attribute attribute : attributes.asList()) {
            Object value = attribute.getValue();
            String v =  value == null ? "null" : value.toString();

            printStream.printf("%s%s=%s%n", printPrefix, attribute.getName(), v);
        }
    }

    @Override
    public void attach(String id) {
        if (attached)
            System.err.println("Already attached.");
        else {
            try {
                if (supportedLog(id)) {
                    currentLog = id;
                    attached = true;
                } else {
                    System.err.println("can not attach to id " + id + " with type /" + currentType);
                }
            } catch (Exception e) {
                System.err.println(e);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void detach() {
        if (!attached)
            System.err.println("Not attached.");

        currentLog = "";
        attached = false;
    }

    @Override
    public void forget(int idx) {
        if (!attached) {
            System.err.println("Not attached.");
        } else if (!currentType.contains("AtomicAction")) {
            System.err.println("Can not support this type");
        } else {
            Uid uid = new Uid(currentLog);
            EditableTransaction act = TransactionTypeManager.getInstance().getTransaction("AtomicAction", uid);
            try {
                act.moveHeuristicToPrepared(idx);
            } catch (IndexOutOfBoundsException ex) {
                System.err.println("Invalid index.");
            }
        }
    }

    @Override
    public void delete(int idx) {
        if (!attached) {
            System.err.println("Not attached.");
        } else if (!currentType.contains("AtomicAction")) {
            System.err.println("Can not support this type");
        } else {
            Uid uid = new Uid(currentLog);
            EditableTransaction act = TransactionTypeManager.getInstance().getTransaction("AtomicAction", uid);
            try {
                act.deleteHeuristicParticipant(idx);
            } catch (IndexOutOfBoundsException ex) {
                System.err.println("Invalid index.");
            }
        }

    }

    private final boolean supportedLog (String logID) throws ObjectStoreException, IOException {
        Uid id = new Uid(logID);

        if (id.equals(Uid.nullUid())) {
            System.err.println(logID + " is null Uid");
        } else if (currentType == null) {
            printStream.printf("No type selected%n");
        } else if (!currentType.contains("AtomicAction")) {
            printStream.printf("Can not support this type");
        } else {
            ObjectStoreIterator iter = new ObjectStoreIterator(StoreManager.getRecoveryStore(), "/" + currentType);
            Uid u;

            do {
                u = iter.iterate();

                if (u.equals(id))
                    return true;
            } while (Uid.nullUid().notEquals(u));
        }

        return false;
    }

    @Override
    public void start() {
        osb.start();
    }

    @Override
    public void stop() {
        osb.stop();
    }
}
