/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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