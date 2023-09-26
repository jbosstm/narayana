/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.Utils;

import com.arjuna.ats.jts.OTSManager;
import org.omg.CosTransactions.Current;



public class OTS
{
	public static Current current()
	{
		return OTSManager.get_current();
	}

	public static Current get_current()
	{
		return OTSManager.get_current();
	}
}