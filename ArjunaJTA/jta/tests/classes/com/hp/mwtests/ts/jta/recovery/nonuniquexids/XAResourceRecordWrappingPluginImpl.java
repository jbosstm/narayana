/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.recovery.nonuniquexids;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.ObjectStoreAPI;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.internal.jta.resources.arjunacore.XAResourceRecord;
import com.arjuna.ats.internal.jta.resources.arjunacore.XAResourceRecordWrappingPlugin;
import org.jboss.tm.XAResourceWrapper;

import javax.transaction.xa.XAResource;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A plugin implementation for copying resource metadata from the JBoss AS
 * specific XAResourceWrapper class to an XAResourceRecord.
 * 
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2011-07
 */
public class XAResourceRecordWrappingPluginImpl implements XAResourceRecordWrappingPlugin {
	private ConcurrentMap<Integer, String> keyToName = new ConcurrentHashMap<Integer, String>();
	private ConcurrentMap<String, Integer> nameToKey = new ConcurrentHashMap<String, Integer>();
	private AtomicInteger nextKey = new AtomicInteger(1);
	private ObjectStoreAPI eisNameStore;
	private volatile String nodeIdentifier;

	public void transcribeWrapperData(XAResourceRecord record) {

		XAResource xaResource = (XAResource) record.value();

		if (xaResource instanceof XAResourceWrapper) {
			XAResourceWrapper xaResourceWrapper = (XAResourceWrapper) xaResource;
			record.setProductName(xaResourceWrapper.getProductName());
			record.setProductVersion(xaResourceWrapper.getProductVersion());
			record.setJndiName(xaResourceWrapper.getJndiName());
		}
	}

	public Integer getEISName(XAResource xaResource) throws IOException, ObjectStoreException {
		if (xaResource instanceof XAResourceWrapper) {
			initialize();
			String jndiName = ((XAResourceWrapper) xaResource).getJndiName();
			Integer key = nameToKey.get(jndiName);
			if (key == null) {
				synchronized (this) {
					// Recheck the resource, we do this so that we don't need to
					// synchronize if this is a read
					key = nameToKey.get(jndiName);
					if (key == null) {
						key = nextKey.getAndIncrement();
						keyToName.put(key, jndiName);
						nameToKey.put(jndiName, key);

						OutputObjectState oos = new OutputObjectState();
						oos.packString(nodeIdentifier);
						oos.packInt(key);
						oos.packString(jndiName);
						eisNameStore.write_committed(new Uid(), "EISNAME", oos);
						eisNameStore.sync();
					}
				}
			}
			return key;
		} else {
			return 0;
		}
	}

	@Override
	public String getEISName(Integer eisKey) {
		try {
			initialize();
		} catch (IOException ioe) {
			return "unloadable EIS key file: " + eisKey;
		} catch (ObjectStoreException e) {
			return "unloadable EIS key file: " + eisKey;
		}
		if (eisKey == 0) {
			return "unknown eis name";
		} else if (eisKey == -1) {
			return "foreign XID";
		} else {
			String eisName = keyToName.get(eisKey);
			if (eisName == null) {
				return "forgot eis name for: " + eisKey;
			} else {
				return eisName;
			}
		}
	}

	private void initialize() throws ObjectStoreException, IOException {
		if (this.nodeIdentifier == null) {
			synchronized (this) {
				// If we are here, check again that the node idenfier is still
				// null in case of race condition
				if (this.nodeIdentifier == null) {

					this.nodeIdentifier = TxControl.getXANodeName();
					this.eisNameStore = StoreManager.getEISNameStore();
					InputObjectState states = new InputObjectState();
					int keyMax = 0;
					boolean allObjUids = eisNameStore.allObjUids("EISNAME", states);
					while (states.notempty()) {
						Uid uid = UidHelper.unpackFrom(states);
						if (uid.equals(Uid.nullUid())) {
							break;
						} else {
							InputObjectState oState = eisNameStore.read_committed(uid, "EISNAME");
							String nodeName = oState.unpackString();
							if (nodeName.equals(nodeIdentifier)) {
								Integer key = oState.unpackInt();
								String jndiName = oState.unpackString();
								keyToName.put(key, jndiName);
								nameToKey.put(jndiName, key);
								if (key > keyMax) {
									keyMax = key;
								}
							} else {
								// logger warn that we are using a new node
							}
						}
					}
					nextKey.set(keyMax + 1);
				}
			}
		}
	}
}