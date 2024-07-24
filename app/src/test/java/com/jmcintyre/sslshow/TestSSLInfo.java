package com.jmcintyre.sslshow;

import org.junit.Test;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import java.security.cert.X509Certificate;

/**
 * This file contains basic integration tests for SSLInfo functionality
 *
 * Author: Josh McIntyre
 */
public class TestSSLInfo {
    @Test
    public void testCertInfo() {
        String hostname = "CN=chaintuts.com";

        try (InputStream inStream = this.getClass().getClassLoader().getResourceAsStream("chaintuts.crt")) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate)cf.generateCertificate(inStream);

            SSLInfo sslInfo = new SSLInfo(certificate);

            assertEquals(sslInfo.subjectName, hostname);

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }


    }
}