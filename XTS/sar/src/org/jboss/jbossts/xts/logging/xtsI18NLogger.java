/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.xts.logging;

import org.jboss.logging.annotations.*;
import static org.jboss.logging.Logger.Level.*;
import static org.jboss.logging.annotations.Message.Format.*;

/**
 * i18n log messages for the xts service module.
 *
 * @author adinn
 */
@MessageLogger(projectCode = "ARJUNA")
public interface xtsI18NLogger
{
    /*
        Message IDs are unique and non-recyclable.
        Don't change the purpose of existing messages.
          (tweak the message text or params for clarification if you like).
        Allocate new messages by following instructions at the bottom of the file.
     */

    @Message(id = 47001, value = "Unable to load XTS initialisation class {0}", format = MESSAGE_FORMAT)
    @LogMessage(level = ERROR)
    public void error_XTSService_1(String arg0, @Cause() Throwable arg1);

    @Message(id = 47002, value = "Not an XTS initialisation class {0}", format = MESSAGE_FORMAT)
    @LogMessage(level = ERROR)
    public void error_XTSService_2(String arg0);

    @Message(id = 47003, value = "Unable to instantiate XTS initialisation class {0}", format = MESSAGE_FORMAT)
    @LogMessage(level = ERROR)
    public void error_XTSService_3(String arg0, @Cause() Throwable arg1);

    @Message(id = 47004, value = "Unable to access XTS initialisation class {0}", format = MESSAGE_FORMAT)
    @LogMessage(level = ERROR)
    public void error_XTSService_4(String arg0, @Cause() Throwable arg1);

}