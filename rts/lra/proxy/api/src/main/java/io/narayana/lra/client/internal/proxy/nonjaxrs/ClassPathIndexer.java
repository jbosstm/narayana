package io.narayana.lra.client.internal.proxy.nonjaxrs;

import io.narayana.lra.logging.LRALogger;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class ClassPathIndexer {

    /**
     * Creates Jandex index from the application classpath
     */
    Index createIndex() throws IOException {
        Indexer indexer = new Indexer();
        List<URL> urls;

        ClassLoader cl = ClassLoader.getSystemClassLoader();
        if (cl instanceof URLClassLoader) {
            urls = Arrays.asList(((URLClassLoader) cl).getURLs());
        } else {
            urls = collectURLsFromClassPath();
        }

        for (URL url : urls) {
            processFile(url.openStream(), indexer);
        }

        return indexer.complete();
    }

    private List<URL> collectURLsFromClassPath() {
        List<URL> urls = new ArrayList<>();

        for (String s : System.getProperty("java.class.path").split(System.getProperty("path.separator"))) {
            if (s.endsWith(".jar")) {
                try {
                    urls.add(new File(s).toURI().toURL());
                } catch (MalformedURLException e) {
                    LRALogger.logger.warn("Cannot create URL from a JAR file included in the classpath", e);
                }
            }
        }

        return urls;
    }

    private void processFile(InputStream inputStream, Indexer indexer) throws IOException {
        ZipInputStream zis = new ZipInputStream(inputStream, StandardCharsets.UTF_8);
        ZipEntry ze = null;

        while ((ze = zis.getNextEntry()) != null) {
            String entryName = ze.getName();
            if (entryName.endsWith(".class")) {
                indexer.index(zis);
            } else if (entryName.endsWith(".war")) {
                // necessary because of the thorntail arquillian adapter
                processFile(zis, indexer);
            }
        }
    }
}
