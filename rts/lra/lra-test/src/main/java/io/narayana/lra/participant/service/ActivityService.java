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
package io.narayana.lra.participant.service;

import io.narayana.lra.participant.model.Activity;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ActivityService {
    private Map<String, Activity> activities = new HashMap<>();

    public Activity getActivity(String txId) throws NotFoundException {
        if (!activities.containsKey(txId)) {
            throw new NotFoundException(Response.status(404).entity("Invalid activity id: " + txId).build());
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
}
