package com.arjuna.jta.distributed.example.server.impl;

import java.io.IOException;
import java.io.ObjectInputStream;

import javax.transaction.xa.XAResource;

import com.arjuna.ats.jta.recovery.SerializableXAResourceDeserializer;

/**
 * This is an additional recovery helper that allows a transport to provide a
 * deserializer for its ProxyXAResource. We need this as otherwise the
 * transaction manager would not be able to see the transports classes. Check
 * out the Javadocs on {@link SerializableXAResourceDeserializer}
 */
public class ProxyXAResourceDeserializer implements SerializableXAResourceDeserializer {

	@Override
	public boolean canDeserialze(String className) {
		if (className.equals(ProxyXAResource.class.getName())) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public XAResource deserialze(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		return (XAResource) ois.readObject();
	}

}
