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
package org.jboss.jbossts.xts.logging;

import org.jboss.logging.*;
import static org.jboss.logging.Logger.Level.*;
import static org.jboss.logging.Message.Format.*;

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
