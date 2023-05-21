package com.Z.NovelReader.Utils;

import android.util.Log;

import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * 解决：Trust anchor for certification path not found 问题
 * 解决：CertificateExpiredException: Certificate expired at xxx (compared to xxx) 问题
 */
public class SSLAgent {

    private final static String TAG = "SSLAgent";
    private final static boolean DEBUG = true;

    private static SSLAgent mSSLAgent;

    public static SSLAgent getInstance(){
        if(mSSLAgent == null){
            mSSLAgent = new SSLAgent();
        }
        return mSSLAgent;
    }

    /**
     * 信任所有的https证书
     * */
    public void trustAllHttpsCertificates() {
        try{
            TrustManager[] trustAllCerts = new TrustManager[1];
            TrustManager tm = new MyTrustManager();
            trustAllCerts[0] = tm;
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(mHostnameVerifier);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private HostnameVerifier mHostnameVerifier = new HostnameVerifier() {

        public boolean verify(String hostname, SSLSession session) {
            log("hostname:" + hostname);
            return true;
        }

    };

    private static class MyTrustManager implements TrustManager, X509TrustManager {

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public boolean isServerTrusted(X509Certificate[] certs) {
            return true;
        }

        public boolean isClientTrusted(X509Certificate[] certs) {
            return true;
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType) {
            return;
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType) {
            return;
        }

    }

    private void log(String msg){
        if(DEBUG){
            Log.i(TAG, msg);
        }
    }

}
