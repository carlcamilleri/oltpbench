package com.oltpbenchmark.api;

import com.oltpbenchmark.util.SocketFactoryTcpNoDelay;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

public   class ThespisHttpClient {

    private static final TrustManager TRUST_ALL_CERTS = new X509TrustManager() {
        @Override
        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
        }

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[] {};
        }
    };

    private static OkHttpClient instance = null;

    public static OkHttpClient getClient(){

        if(instance == null){
            synchronized (OkHttpClient.class) {
                if(instance == null){

                    try {
                        SSLContext sslContext = SSLContext.getInstance("SSL");
                        sslContext.init(null, new TrustManager[] { TRUST_ALL_CERTS }, new java.security.SecureRandom());

                        instance = new OkHttpClient.Builder()
                                .connectTimeout(10, TimeUnit.SECONDS)
                                .readTimeout(10, TimeUnit.SECONDS)
                                .sslSocketFactory(sslContext.getSocketFactory(),
                                        new X509TrustManager() {
                                            @Override
                                            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                                            }

                                            @Override
                                            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                                            }

                                            @Override
                                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                                return new java.security.cert.X509Certificate[]{};
                                            }
                                        })
                        //.protocols(Collections.singletonList(Protocol.H2_PRIOR_KNOWLEDGE))
                        //.dispatcher(new Dispatcher(Executors.newFixedThreadPool(256)))
                                .followRedirects(false)
                                .retryOnConnectionFailure(true)
                                .socketFactory(new SocketFactoryTcpNoDelay())
                                .connectionPool(new ConnectionPool(256,60,TimeUnit.SECONDS))


//            .eventListener(new EventListener() {
//                private long callStartNanos;
//
//                private void printEvent(String name) {
//                    long nowNanos = System.nanoTime();
//                    if (name.equals("callStart")) {
//                        callStartNanos = nowNanos;
//                    }
//                    long elapsedNanos = nowNanos - callStartNanos;
//                    System.out.printf("%.3f %s%n", elapsedNanos / 1000000000d, name);
//                }
//
//
//
//
//                @Override public void callStart(Call call) {
//                    printEvent("callStart");
//                }
//
//                @Override public void callEnd(Call call) {
//                    printEvent("callEnd");
//                }
//
//                @Override public void dnsStart(Call call, String domainName) {
//                    printEvent("dnsStart");
//                }
//
//                @Override public void dnsEnd(Call call, String domainName, List<InetAddress> inetAddressList) {
//                    printEvent("dnsEnd");
//                }
//            })
                                .build();


                    } catch (NoSuchAlgorithmException | KeyManagementException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
        return instance;

    }






}
