/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.star.test;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

/**
 * Generates a throw-away, self-signed RSA certificate at runtime and derives matching
 * server and client {@link SSLContext}s from it.
 * <p>
 * This lets the REST-AT tests run over TLS ({@code -Drts.usessl=true}) without shipping any
 * key material in the source tree (see the project rule against committing keystores). The
 * certificate carries {@code CN=localhost} and a {@code localhost}/{@code 127.0.0.1} subject
 * alternative name so the default hostname verification of the JDK HTTPS client passes.
 */
final class TestSSLContext {
    private static final String ALIAS = "localhost";
    private static final char[] PASSWORD = "changeit".toCharArray();

    private final SSLContext serverContext;
    private final SSLContext clientContext;

    private TestSSLContext(SSLContext serverContext, SSLContext clientContext) {
        this.serverContext = serverContext;
        this.clientContext = clientContext;
    }

    /** SSLContext for the embedded server (holds the private key + certificate). */
    SSLContext serverContext() {
        return serverContext;
    }

    /** SSLContext for the test client (trusts the generated certificate). */
    SSLContext clientContext() {
        return clientContext;
    }

    static TestSSLContext create() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair keyPair = kpg.generateKeyPair();

        X509Certificate certificate = selfSign(keyPair, "CN=localhost");

        // key store presented by the server
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);
        keyStore.setKeyEntry(ALIAS, keyPair.getPrivate(), PASSWORD, new Certificate[] {certificate});

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, PASSWORD);
        SSLContext serverContext = SSLContext.getInstance("TLS");
        serverContext.init(kmf.getKeyManagers(), null, null);

        // trust store used by the client to trust the (self-signed) server certificate
        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        trustStore.load(null, null);
        trustStore.setCertificateEntry(ALIAS, certificate);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        SSLContext clientContext = SSLContext.getInstance("TLS");
        clientContext.init(null, tmf.getTrustManagers(), null);

        return new TestSSLContext(serverContext, clientContext);
    }

    private static X509Certificate selfSign(KeyPair keyPair, String dn) throws Exception {
        long now = System.currentTimeMillis();
        Date notBefore = new Date(now - 60_000L);
        Date notAfter = new Date(now + 24L * 60 * 60 * 1000); // a day comfortably outlives a test run
        X500Name subject = new X500Name(dn);

        JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
                subject, BigInteger.valueOf(now), notBefore, notAfter, subject, keyPair.getPublic());
        GeneralNames subjectAltNames = new GeneralNames(new GeneralName[] {
                new GeneralName(GeneralName.dNSName, "localhost"),
                new GeneralName(GeneralName.iPAddress, "127.0.0.1")
        });
        builder.addExtension(Extension.subjectAlternativeName, false, subjectAltNames);

        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(keyPair.getPrivate());
        X509CertificateHolder holder = builder.build(signer);
        return new JcaX509CertificateConverter().getCertificate(holder);
    }
}
