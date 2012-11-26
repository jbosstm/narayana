package org.jboss.jbossts.xts.bytemanSupport;

import org.jboss.byteman.agent.submit.Submit;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author paul.robinson@redhat.com 22/11/2012
 */
public class BMScript {

    private static final Submit submit = new Submit();

    public static void submit(String scriptResourcePath) {

        try {
            List<InputStream> streams = getScriptFromResource(scriptResourcePath);
            submit.addRulesFromResources(streams);
        } catch (Exception e) {
            throw new RuntimeException("Failed to submit Byteman script", e);
        }
    }

    public static void remove(String scriptResourcePath) {

        try {
            List<InputStream> streams = getScriptFromResource(scriptResourcePath);
            submit.deleteRulesFromResources(streams);
        } catch (Exception e) {
            throw new RuntimeException("Failed to remove Byteman script", e);
        }
    }

    private static List<InputStream> getScriptFromResource(String scriptResourcePath) {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(scriptResourcePath);
        if (resource == null) {
            throw new RuntimeException("'" + scriptResourcePath + "' can't be found on the classpath");
        }

        List<InputStream> streams = new ArrayList<InputStream>();
        try {
            streams.add(resource.openStream());
        } catch (IOException e) {
            throw new RuntimeException("Unable to open stream at url location: " + resource.toString());
        }
        return streams;
    }

}
