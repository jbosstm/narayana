/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.tm;

import java.util.List;

//@PrioritizableResource(priority = ResourcePriority.FIRST)
public class FirstTestResource extends TestResource implements FirstResource {
    public FirstTestResource(List<TestResource> prepareOrder, List<TestResource> endOrder) {
        super(prepareOrder, endOrder);
    }
}