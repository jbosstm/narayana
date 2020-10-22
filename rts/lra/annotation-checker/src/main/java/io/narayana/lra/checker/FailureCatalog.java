/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat Middleware LLC, and individual contributors
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


package io.narayana.lra.checker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Singleton storing list of String representing found LRA definition failures.
 *
 * @author Ondra Chaloupka <ochaloup@redhat.com>
 */
public enum FailureCatalog {
    INSTANCE;

    private String END_LINE = String.format("%n");
    private List<String> failureCatalog = Collections.synchronizedList(new ArrayList<>());

    public void add(String failure) {
        failureCatalog.add(failure);
    }

    public boolean isEmpty() {
        return failureCatalog.isEmpty();
    }

    void clear() {
        failureCatalog.clear();
    }

    public String formatCatalogContent() {
        if (failureCatalog == null || failureCatalog.isEmpty()) {
            return "";
        }
        StringBuffer buffer = new StringBuffer();
        for (String failure: failureCatalog) {
            buffer.append(" -> ").append(failure).append(END_LINE);
        }
        return buffer.toString();
    }

}