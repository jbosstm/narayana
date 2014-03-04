/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.arjuna.qa.junit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public final class ZipArchiver {

    public void createArchive(final String source, final String target) throws IOException {
        final File targetFile = new File(target);
        final File sourceFile = new File(source);
        final FileOutputStream targetFileOutputStream = new FileOutputStream(targetFile);
        final ZipOutputStream targetZipOutputStream = new ZipOutputStream(targetFileOutputStream);

        archiveFile(sourceFile, targetZipOutputStream, new ZipEntry(sourceFile.getName()));

        targetZipOutputStream.close();
        targetFileOutputStream.close();
    }

    private void archiveDirectory(final File sourceDirectory, final ZipOutputStream targetOutputStream,
            final ZipEntry zipEntry) throws IOException {

        for (final File file : sourceDirectory.listFiles()) {
            final ZipEntry fileZipEntry = new ZipEntry(zipEntry.getName() + "/" + file.getName());

            archiveFile(file, targetOutputStream, fileZipEntry);
        }
    }

    private void archiveFile(final File sourceFile, final ZipOutputStream targetOutputStream,
            final ZipEntry zipEntry) throws IOException {

        if (sourceFile.isDirectory()) {
            archiveDirectory(sourceFile, targetOutputStream, zipEntry);
        } else {
            byte[] buffer = new byte[1024];
            final FileInputStream sourceInputStream = new FileInputStream(sourceFile);
            int lenght;

            targetOutputStream.putNextEntry(zipEntry);

            while ((lenght = sourceInputStream.read(buffer)) > 0) {
                targetOutputStream.write(buffer, 0, lenght);
            }

            sourceInputStream.close();
        }
    }

}
