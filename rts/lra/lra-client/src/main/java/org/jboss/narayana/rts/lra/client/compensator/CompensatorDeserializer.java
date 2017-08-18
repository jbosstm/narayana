package org.jboss.narayana.rts.lra.client.compensator;

import java.io.ObjectInputStream;

/**
 * An object that knows how to recreate a compensator from its' persistent form
 */
public interface CompensatorDeserializer {
    Compensator deserialize(ObjectInputStream objectInputStream);
    Compensator recreate(byte[] recoveryState);
}
