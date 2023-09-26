package unit;

import org.jboss.stm.STMVerticle;
import org.junit.Test;

/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


import com.arjuna.ats.arjuna.AtomicAction;

import static org.junit.Assert.assertTrue;

public class STMUnitTest {

  @Test
  public void testVerticle() {
    STMVerticle vert = new STMVerticle();

    // do something with verticle

    AtomicAction A = new AtomicAction();

    A.begin();

    int amount = vert.value();

    A.abort();

    assertTrue(vert.value() == amount);
  }
}