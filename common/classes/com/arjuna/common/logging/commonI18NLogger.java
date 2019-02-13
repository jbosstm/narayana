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
package com.arjuna.common.logging;

import static org.jboss.logging.Logger.Level.WARN;
import static org.jboss.logging.annotations.Message.Format.MESSAGE_FORMAT;

import java.net.URL;

import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

/**
 * i18n log messages for the jta module.
 * 
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-06
 */
@MessageLogger(projectCode = "ARJUNA")
public interface commonI18NLogger {

	/*
	 * Message IDs are unique and non-recyclable. Don't change the purpose of
	 * existing messages. (tweak the message text or params for clarification if
	 * you like). Allocate new messages by following instructions at the bottom
	 * of the file.
	 */
	
	@Message(id = 48001, value = "Could not find manifest {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_could_not_find_manifest(String arg0, @Cause() Throwable arg1);

	@Message(id = 48002, value = "Could not find configuration file, URL was: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_could_not_find_config_file(URL url);

    @Message(id = 48003, value = "className is null", format = MESSAGE_FORMAT)
    @LogMessage(level = WARN)
    public void warn_common_ClassloadingUtility_1();

    @Message(id = 48004, value = "attempt to load {0} threw ClassNotFound. Wrong classloader?", format = MESSAGE_FORMAT)
    @LogMessage(level = WARN)
    public void warn_common_ClassloadingUtility_2(String arg0, @Cause() Throwable arg1);

    @Message(id = 48005, value = "class {0} does not implement {1}", format = MESSAGE_FORMAT)
    @LogMessage(level = WARN)
    public void warn_common_ClassloadingUtility_3(String arg0, String arg1, @Cause() Throwable arg2);

    @Message(id = 48006, value = "cannot create new instance of {0}", format = MESSAGE_FORMAT)
    @LogMessage(level = WARN)
    public void warn_common_ClassloadingUtility_4(String arg0, @Cause() Throwable arg1);

    @Message(id = 48007, value = "cannot access {0}", format = MESSAGE_FORMAT)
    @LogMessage(level = WARN)
    public void warn_common_ClassloadingUtility_5(String arg0, @Cause() Throwable arg1);

    @Message(id = 48008, value = "cannot initialize from string {0}", format = MESSAGE_FORMAT)
    @LogMessage(level = WARN)
    public void warn_common_ClassloadingUtility_6(String arg0, @Cause() Throwable arg1);

    /*
     * Allocate new messages directly above this notice. - id: use the next id
     * number in sequence. Don't reuse ids. The first two digits of the
     * id(XXyyy) denote the module all message in this file should have the same
     * prefix. - value: default (English) version of the log message. - level:
     * according to severity semantics defined at
     * http://docspace.corp.redhat.com/docs/DOC-30217 Debug and trace don't get
     * i18n. Everything else MUST be i18n. By convention methods with String
     * return type have prefix get_, all others are log methods and have prefix
     * <level>_
     */
}
