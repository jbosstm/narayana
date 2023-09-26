/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


//

package org.jboss.jbossts.qa.CrashRecovery11Utils;

import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.common.arjPropertyManager;

public class Delays
{
	public static int recoveryDelay()
	{
			int _recoveryPeriod = recoveryPropertyManager.getRecoveryEnvironmentBean().getPeriodicRecoveryPeriod();

			int _backoffPeriod = recoveryPropertyManager.getRecoveryEnvironmentBean().getRecoveryBackoffPeriod();

			int _delayFactor = arjPropertyManager.getCoreEnvironmentBean().getTimeoutFactor();

			_backoffPeriod *= _delayFactor;
			_recoveryPeriod *= _delayFactor;

			return (_backoffPeriod + _recoveryPeriod + (5 * 1000) /*5 secs for processing*/);
	}
}