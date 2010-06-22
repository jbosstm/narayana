/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2010,
 * @author JBoss, by Red Hat.
 */
package com.arjuna.ats.tsmx.logging;

import org.jboss.logging.*;
import static org.jboss.logging.Logger.Level.*;
import static org.jboss.logging.Message.Format.*;

/**
 * i18n log messages for the tsmx module.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-06
 */
@MessageLogger(projectCode = "ARJUNA")
public interface tsmxI18NLogger {

    /*
        Message IDs are unique and non-recyclable.
        Don't change the purpose of existing messages.
          (tweak the message text or params for clarification if you like).
        Allocate new messages by following instructions at the bottom of the file.
     */

	@Message(id = 30001, value = "Failed to register MBean {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = ERROR)
	public void error_TransactionServiceMX_failedtoregistermbean(String arg0, Throwable arg1);

	@Message(id = 30002, value = "Failed to unregister MBean {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = ERROR)
	public void error_TransactionServiceMX_failedtounregistermbean(String arg0, Throwable arg1);

	@Message(id = 30003, value = "MBean {0} already registered", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_TransactionServiceMX_mbeanalreadyregistered(String arg0);

    @Message(id = 30004, value = "Error reading tool jar", format = MESSAGE_FORMAT)
    @LogMessage(level = ERROR)
    public void error_toolsClassLoader_invalidjar(Throwable arg0);

    @Message(id = 30005, value = "The URL is invalid {0}", format = MESSAGE_FORMAT)
    @LogMessage(level = ERROR)
    public void error_toolsClassLoader_invalidurl(String arg0, Throwable arg1);

    /*
        Allocate new messages directly above this notice.
          - id: use the next id number in sequence. Don't reuse ids.
          The first two digits of the id(XXyyy) denote the module
            all message in this file should have the same prefix.
          - value: default (English) version of the log message.
          - level: according to severity semantics defined at http://docspace.corp.redhat.com/docs/DOC-30217
          Debug and trace don't get i18n. Everything else MUST be i18n.
     */
}
