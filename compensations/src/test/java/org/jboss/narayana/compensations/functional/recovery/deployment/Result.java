/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
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

package org.jboss.narayana.compensations.functional.recovery.deployment;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class Result {

    private List<String> compensatedData = Collections.emptyList();

    private List<String> confirmedData = Collections.emptyList();

    private Result() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<String> getCompensatedData() {
        return compensatedData;
    }

    public List<String> getConfirmedData() {
        return confirmedData;
    }

    @Override
    public String toString() {
        return "Result{" + "compensatedData=" + compensatedData + ", confirmedData=" + confirmedData + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Result result = (Result) o;

        if (!compensatedData.equals(result.compensatedData)) {
            return false;
        }

        return confirmedData.equals(result.confirmedData);

    }

    @Override
    public int hashCode() {
        int result = compensatedData.hashCode();
        result = 31 * result + confirmedData.hashCode();

        return result;
    }

    public static class Builder {

        private final Result result;

        public Builder() {
            result = new Result();
        }

        public Result build() {
            return result;
        }

        public Builder compensatedDate(List<String> compensatedData) {
            result.compensatedData = Collections.unmodifiableList(compensatedData);
            return this;
        }

        public Builder confirmedData(List<String> confirmedData) {
            result.confirmedData = Collections.unmodifiableList(confirmedData);
            return this;
        }
    }
}
