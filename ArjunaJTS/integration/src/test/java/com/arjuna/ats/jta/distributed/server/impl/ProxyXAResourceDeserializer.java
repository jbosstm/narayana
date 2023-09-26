/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.jta.distributed.server.impl;

import java.io.IOException;
import java.io.ObjectInputStream;

import javax.transaction.xa.XAResource;

import com.arjuna.ats.jta.recovery.SerializableXAResourceDeserializer;

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
