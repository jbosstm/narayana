package io.narayana.lra.coordinator.domain.model;

import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

public final class ReportedParticipants {
    public static final ReportedParticipants EMPTY = new ReportedParticipants();

    private Map<URI, LRAStatusReason> reasons = new ConcurrentHashMap<>();

    public void addParticipant(URI recoveryId, LRAStatusReason reason) {
        reasons.put(recoveryId, reason);
    }

    public synchronized void save(OutputObjectState os) throws IOException {
        os.packInt(reasons.size());
        for (Map.Entry<URI, LRAStatusReason> entry : reasons.entrySet()) {
            os.packString(entry.getKey().toString());
            os.packString(entry.getValue().name());
        }
    }

    public synchronized ReportedParticipants restore(InputObjectState os) throws IOException {
        reasons = new HashMap<>();

        int size = os.unpackInt();

        for (int i = 0; i < size; i++) {
            addParticipant(URI.create(os.unpackString()), LRAStatusReason.valueOf(os.unpackString()));
        }

        return this;
    }

    public String report() {
        StringJoiner joiner = new StringJoiner(",");
        reasons.forEach((uri, lraStatusReason) ->
            joiner.add(String.format("%s - %s", uri.toASCIIString(), lraStatusReason.name())));

        return joiner.toString();
    }
}
