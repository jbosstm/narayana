/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
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
