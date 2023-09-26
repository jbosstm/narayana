/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.txbridge.utils;

import com.arjuna.ats.arjuna.common.Uid;
import org.jboss.logging.annotations.*;
import static org.jboss.logging.Logger.Level.*;
import static org.jboss.logging.annotations.Message.Format.*;

import org.jboss.jbossts.xts.bridge.at.BridgeWrapper;

/**
 * i18n log messages for the txbridge module.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-07
 */
@MessageLogger(projectCode = "ARJUNA")
public interface txbridgeI18NLogger
{
    /*
        Message IDs are unique and non-recyclable.
        Don't change the purpose of existing messages.
          (tweak the message text or params for clarification if you like).
        Allocate new messages by following instructions at the bottom of the file.
     */

    @Message(id = 33001, value = "Unable to get subordinate transaction id", format = MESSAGE_FORMAT)
    @LogMessage(level = ERROR)
    public void error_ibdp_nosub(@Cause() Throwable arg0);

    @Message(id = 33002, value = "Unable to recover subordinate transaction id {0}", format = MESSAGE_FORMAT)
    @LogMessage(level = ERROR)
    public void error_ibdp_norecovery(Uid arg0, @Cause() Throwable arg1);

    @Message(id = 33003, value = "prepare on Xid={0} returning Aborted", format = MESSAGE_FORMAT)
    @LogMessage(level = WARN)
    public void warn_ibdp_aborted(String arg0, @Cause() Throwable arg1);

    @Message(id = 33004, value = "commit on Xid={0} failed", format = MESSAGE_FORMAT)
    @LogMessage(level = ERROR)
    public void error_ibdp_commitfailed(String arg0, @Cause() Throwable arg1);

    @Message(id = 33005, value = "rollback on Xid={0} failed", format = MESSAGE_FORMAT)
    @LogMessage(level = ERROR)
    public void error_ibdp_rollbackfailed(String arg0, @Cause() Throwable arg1);


    @Message(id = 33006, value = "InboundBridgeRecoveryManager starting", format = MESSAGE_FORMAT)
    @LogMessage(level = INFO)
    public void info_ibrm_start();

    @Message(id = 33007, value = "InboundBridgeRecoveryManager stopping", format = MESSAGE_FORMAT)
    @LogMessage(level = INFO)
    public void info_ibrm_stop();

    @Message(id = 33008, value = "problem rolling back orphaned subordinate tx {0}", format = MESSAGE_FORMAT)
    @LogMessage(level = ERROR)
    public void error_ibrm_rollbackerr(String arg0, @Cause() Throwable arg1);

    @Message(id = 33009, value = "Problem whilst scanning for in-doubt subordinate transactions", format = MESSAGE_FORMAT)
    @LogMessage(level = ERROR)
    public void error_ibrm_scanerr(@Cause() Throwable arg0);


    @Message(id = 33010, value = "prepare on Xid={0} failed, setting RollbackOnly", format = MESSAGE_FORMAT)
    @LogMessage(level = WARN)
    public void warn_ibvp_preparefailed(String arg0, @Cause() Throwable arg1);

    @Message(id = 33011, value = "setRollbackOnly failed", format = MESSAGE_FORMAT)
    @LogMessage(level = WARN)
    public void warn_ibvp_setrollbackfailed(@Cause() Throwable arg0);

    @Message(id = 33012, value = "stop failed for Xid {0}", format = MESSAGE_FORMAT)
    @LogMessage(level = WARN)
    public void warn_ibvp_stopfailed(String arg0, @Cause() Throwable arg1);



    @Message(id = 33013, value = "OutboundBridgeRecoveryManager starting", format = MESSAGE_FORMAT)
    @LogMessage(level = INFO)
    public void info_obrm_start();

    @Message(id = 33014, value = "OutboundBridgeRecoveryManager stopping", format = MESSAGE_FORMAT)
    @LogMessage(level = INFO)
    public void info_obrm_stop();


    @Message(id = 33015, value = "unexpected Status {0}, treating as ROLLEDBACK", format = MESSAGE_FORMAT)
    @LogMessage(level = WARN)
    public void warn_obs_unexpectedstatus(String arg0);


    @Message(id = 33016, value = "Unable to recover subordinate transaction id={0},", format = MESSAGE_FORMAT)
    @LogMessage(level = ERROR)
    public void error_obxar_unabletorecover(String arg0, @Cause() Throwable arg1);

    @Message(id = 33017, value = "Unable to enlist BridgeXAResource or register BridgeSynchronization", format = MESSAGE_FORMAT)
    @LogMessage(level = ERROR)
    public void error_obm_unabletoenlist(@Cause() Throwable arg0);

    @Message(id = 33018, value = "Error on prepareVolatile of bridge wrapper {0}", format = MESSAGE_FORMAT)
    @LogMessage(level = ERROR)
    public void error_bridge_wrapper_prepare_volatile(BridgeWrapper bridgeWrapper);

    /*
        Allocate new messages directly above this notice.
          - id: use the next id number in sequence. Don't reuse ids.
          The first two digits of the id(XXyyy) denote the module
            all message in this file should have the same prefix.
          - value: default (English) version of the log message.
          - level: according to severity semantics defined at http://docspace.corp.redhat.com/docs/DOC-30217
          Debug and trace don't get i18n. Everything else MUST be i18n.
          By convention methods with String return type have prefix get_,
            all others are log methods and have prefix <level>_
    */
}