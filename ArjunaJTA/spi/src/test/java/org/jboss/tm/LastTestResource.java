/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.tm;

import java.util.List;

// @PrioritizableResource(priority = ResourcePriority.LAST)
public class LastTestResource extends TestResource implements LastResource {
    public LastTestResource(List<TestResource> prepareOrder, List<TestResource> endOrder) {
        super(prepareOrder, endOrder);
    }
}