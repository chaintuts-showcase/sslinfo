/* This file contains code for fetching SSL certificate information
*
* Author: Josh McIntyre
*/
package com.jmcintyre.sslshow;

import android.util.Log;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SSLInfo
{
    // Connection related Constants
    private final int PORT = 443;
    private final String PROTOCOL = "TLS";
    private final String TAG = "SSLINFO";
    private final String CONN_ERROR = "Unable to create SSL socket for hostname ";

    // Certificate information constants
    private final String SUBJECT_STRING = "\nSubject name: ";
    private final String ISSUER_STRING = "\nIssuer name: ";
    private final String ALGORITHM_STRING = "\nSignature algorithm: ";
    private final String SERIAL_STRING = "\nSerial number: ";

    private final String NOINFO_STRING = "\nNo information available";

    // Connection information
    private Thread sslInfoThread;
    private SSLSocket sslSocket;
    private String hostname;

    // Certificate information
    private X509Certificate certificate;
    private String subjectName;
    private String issuerName;
    private String algorithm;
    private String serialNumber;
    private String issuedOn;
    private String expiresOn;

    // Formatted information consumed by callers

    private String certInfo;


    // This constructor takes the given hostname and fetches the SSL information
    public SSLInfo(String hostname) {
        this.hostname = hostname;
        fetchSSLInfo();
        constructCertInfo();
    }

    // Getter for the certInfo
    public String getCertInfo() {
        return certInfo;
    }

    // Construct the certInfo formatted string
    public void constructCertInfo() {

        // If the certificate is null, return a string indicating that no information could be gathered
        if (certificate == null) {
            certInfo = NOINFO_STRING;
            return;
        }

        // Build up a string-formatted dump of the certificate info
        StringBuilder certInfoBuilder = new StringBuilder();
        certInfoBuilder.append("\nSubject name: ");
        certInfoBuilder.append(subjectName);
        certInfoBuilder.append("\nIssuer name: ");
        certInfoBuilder.append(issuerName);
        certInfoBuilder.append("\nSignature algorithm: ");
        certInfoBuilder.append(algorithm);
        certInfoBuilder.append("\nSerial number: ");
        certInfoBuilder.append(serialNumber);
        certInfoBuilder.append("\nIssued On: ");
        certInfoBuilder.append(issuedOn);
        certInfoBuilder.append("\nExpires on: ");
        certInfoBuilder.append(expiresOn);

        certInfo = certInfoBuilder.toString();

        Log.i(TAG, certInfo);
    }

    // This function requests the SSL connection and gets certificate information
    private void fetchSSLInfo() {

        // Networking operations must be done in their own thread in Android
        // This is to prevent the UI from freezing during a long running network op
        sslInfoThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SSLContext sslContext = SSLContext.getInstance(PROTOCOL);

                    /* This block initializes a bogus TrustManager that accepts ALL certificates,
                    * including self-signed, revoked, and expired certificates
                    * You should absolutely NEVER do this in production code where the actual validity
                    * of the certificate is important for sending private information over the connection
                    * We never do anything beyond the handshake phase here
                    * The purpose of this utility is to parse and display information about SSL
                    * certificates for education, testing, etc. - so we want this code to work for invalid certs
                    * Without this block, any invalid certificate will throw and exception before we can inspect
                    * the certificates contents and display information
                    */
                    sslContext.init(null, new TrustManager[]{ new X509TrustManager() {

                        private X509Certificate[] accepted;

                        @Override
                        public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                            accepted = xcs;
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return accepted;
                        }
                    }}, null);

                    // Initialize the socket
                    SSLSocketFactory socketFactory = sslContext.getSocketFactory();
                    sslSocket = (SSLSocket) socketFactory.createSocket(hostname, PORT);

                    // Gather certificate information from the session
                    certificate = (X509Certificate) sslSocket.getSession().getPeerCertificates()[0];

                    subjectName = certificate.getSubjectX500Principal().getName();
                    issuerName = certificate.getIssuerX500Principal().getName();
                    algorithm = certificate.getSigAlgName();
                    serialNumber = certificate.getSerialNumber().toString();
                    issuedOn = certificate.getNotBefore().toString();
                    expiresOn = certificate.getNotAfter().toString();

                } catch (IOException e) {
                    Log.e(TAG, CONN_ERROR + hostname);
                    Log.e(TAG, e.toString());
                } catch (NoSuchAlgorithmException e) {
                    Log.e(TAG, e.toString());
                } catch (KeyManagementException e) {
                    Log.e(TAG, e.toString());
                }
            }
        });

        // Start the SSL information gathering thread
        sslInfoThread.start();

        /* Wait on the thread to finish before exiting anyway as callers need the certInfo to be populated
          This sort of defeats the purpose of running a thread for network ops, but in this context operations are linear anyway
          This satisfies Android's requirement of network ops running in an independent thread while also satisfying
          the need for certInfo to be populated before any callers run getCertInfo()
         */
        try {
            sslInfoThread.join();
        } catch (InterruptedException e) {
            Log.e(TAG, e.toString());
        }
    }
}
