package com.jboss.transaction.wstf.test;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * The test log controller.
 * @author kevin
 */
public class TestLogController
{
    /**
     * The base directory for logs, hardcoded for now.
     */
    private static File baseDir ;
    
    static
    {
        final String userHome = System.getProperty("user.home") ;
        baseDir = new File(userHome, "logs") ;
        if (!baseDir.exists())
        {
            baseDir.mkdir() ;
        }
    }

    /**
     * Read the contents of the specified log.
     * @param logName The log to retrieve.
     * @return The log contents.
     * @throws IOException for reading errors.
     */
    public static String readLog(final String logName)
        throws IOException
    {
        final File logFile = new File(baseDir, logName) ;
        if (logFile.exists() && logFile.canRead())
        {
            final FileReader reader = new FileReader(logFile) ;
            try
            {
                final StringBuffer buffer = new StringBuffer() ;
                final char[] charBuffer = new char[256] ;
                while(reader.ready())
                {
                    final int count = reader.read(charBuffer) ;
                    if (count > 0)
                    {
                        buffer.append(charBuffer, 0, count) ;
                    }
                }
                return buffer.toString() ;
            }
            finally
            {
                reader.close() ;
            }
        }
        throw new IOException("Cannot read log file: " + logName) ;
    }

    /**
     * Write the contents of the specified log.
     * @param logName The log to write.
     * @param contents The log contents.
     * @throws IOException for reading errors.
     */
    public static void writeLog(final String logName, final String contents)
        throws IOException
    {
        final File logFile = new File(baseDir, logName) ;
        final FileWriter writer = new FileWriter(logFile) ;
        try
        {
            writer.write(contents) ;
        }
        finally
        {
            writer.close() ;
        }
    }
}
