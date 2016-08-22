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

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class Options {

    private boolean isDistributed;

    private boolean isCompensate;

    private boolean isTxConfirm;

    private boolean isTxCompensate;

    private boolean isCompensatableActionCompensation;

    private boolean isCompensatableActionConfirmation;

    private String compensationScopedData;

    private String compensatableActionData;

    private String testName;

    private Options() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isDistributed() {
        return isDistributed;
    }

    public void setDistributed(boolean distributed) {
        isDistributed = distributed;
    }

    public boolean isCompensate() {
        return isCompensate;
    }

    public void setCompensate(boolean compensate) {
        isCompensate = compensate;
    }

    public boolean isTxConfirm() {
        return isTxConfirm;
    }

    public void setTxConfirm(boolean txConfirm) {
        isTxConfirm = txConfirm;
    }

    public boolean isTxCompensate() {
        return isTxCompensate;
    }

    public void setTxCompensate(boolean txCompensate) {
        isTxCompensate = txCompensate;
    }

    public boolean isCompensatableActionCompensation() {
        return isCompensatableActionCompensation;
    }

    public void setCompensatableActionCompensation(boolean compensatableActionCompensationHandler) {
        isCompensatableActionCompensation = compensatableActionCompensationHandler;
    }

    public boolean isCompensatableActionConfirmation() {
        return isCompensatableActionConfirmation;
    }

    public void setCompensatableActionConfirmation(boolean compensatableActionConfirmationHandler) {
        isCompensatableActionConfirmation = compensatableActionConfirmationHandler;
    }

    public String getCompensationScopedData() {
        return compensationScopedData;
    }

    public void setCompensationScopedData(String compensationScopedData) {
        this.compensationScopedData = compensationScopedData;
    }

    public String getCompensatableActionData() {
        return compensatableActionData;
    }

    public void setCompensatableActionData(String compensatableActionData) {
        this.compensatableActionData = compensatableActionData;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    @Override
    public String toString() {
        return "Options{" + "isDistributed=" + isDistributed + ", isCompensate=" + isCompensate + ", isTxConfirm=" + isTxConfirm
                + ", isTxCompensate=" + isTxCompensate + ", isCompensatableActionCompensation="
                + isCompensatableActionCompensation + ", isCompensatableActionConfirmation="
                + isCompensatableActionConfirmation + ", compensationScopedData='" + compensationScopedData
                + "', compensatableActionData='" + compensatableActionData + "', testName='" + testName + "'}";
    }

    public static class Builder {

        private final Options options;

        public Builder() {
            options = new Options();
        }

        public Options build() {
            return options;
        }

        public Builder isDistributed(boolean isDistributed) {
            options.isDistributed = isDistributed;
            return this;
        }

        public Builder isCompensate(boolean isCompensate) {
            options.isCompensate = isCompensate;
            return this;
        }

        public Builder isTxConfirm(boolean isTxConfirm) {
            options.isTxConfirm = isTxConfirm;
            return this;
        }

        public Builder isTxCompensate(boolean isTxCompensate) {
            options.isTxCompensate = isTxCompensate;
            return this;
        }

        public Builder isCompensatableActionCompensation(boolean isCompensatableActionCompensation) {
            options.isCompensatableActionCompensation = isCompensatableActionCompensation;
            return this;
        }

        public Builder isCompensatableActionConfirmation(boolean isCompensatableActionConfirmation) {
            options.isCompensatableActionConfirmation = isCompensatableActionConfirmation;
            return this;
        }

        public Builder compensationScopedData(String compensationScopedData) {
            options.compensationScopedData = compensationScopedData;
            return this;
        }

        public Builder compensatableActionData(String compensatableActionData) {
            options.compensatableActionData = compensatableActionData;
            return this;
        }

        public Builder testName(String testName) {
            options.testName = testName;
            return this;
        }

    }
}
