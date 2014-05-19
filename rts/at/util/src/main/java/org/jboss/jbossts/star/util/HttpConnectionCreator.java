package org.jboss.jbossts.star.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public interface HttpConnectionCreator {
    HttpURLConnection open(URL url) throws IOException;
}
