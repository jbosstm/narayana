/*
 * SPDX short identifier: Apache-2.0
 */

package io.narayana.lra.proxy.test.service;

import io.narayana.lra.proxy.test.model.Activity;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

@ApplicationScoped
public class ActivityService {
    private Map<String, Activity> activities = new HashMap<>();

    public Activity getActivity(String txId) throws NotFoundException {
        if (!activities.containsKey(txId)) {
            String errorMsg = "Invalid activity id: " + txId;
            throw new NotFoundException(errorMsg, // 404
                    Response.status(NOT_FOUND).entity(errorMsg).build());
        }

        return activities.get(txId);
    }

    public List<Activity> findAll() {
        return new ArrayList<>(activities.values());
    }

    public void add(Activity activity) {
        activities.putIfAbsent(activity.id, activity);
    }

    public void remove(String id) {
        activities.remove(id);
    }

    public Activity getActivity(String txId, boolean failIfNotFound) {
        if (activities.containsKey(txId)) {
            return activities.get(txId);
        }

        if (failIfNotFound) {
            String errorMsg = "Invalid activity id: " + txId;
            throw new NotFoundException(errorMsg, // 404
                    Response.status(NOT_FOUND).entity(errorMsg).build());
        }

        return new Activity(txId);
    }
}