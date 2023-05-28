/*
 * SPDX short identifier: Apache-2.0
 */
package io.narayana.lra.provider;

import org.eclipse.microprofile.lra.annotation.ParticipantStatus;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
@Produces(MediaType.APPLICATION_OCTET_STREAM)
public class ParticipantStatusOctetStreamProvider implements MessageBodyWriter<ParticipantStatus> {
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.isAssignableFrom(ParticipantStatus.class);
    }

    @Override
    public long getSize(ParticipantStatus participantStatus, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(ParticipantStatus participantStatus, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        entityStream.write(participantStatus.name().getBytes());
    }
}
