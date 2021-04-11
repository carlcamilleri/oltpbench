/******************************************************************************
 *  Copyright 2015 by OLTPBenchmark Project                                   *
 *                                                                            *
 *  Licensed under the Apache License, Version 2.0 (the "License");           *
 *  you may not use this file except in compliance with the License.          *
 *  You may obtain a copy of the License at                                   *
 *                                                                            *
 *    http://www.apache.org/licenses/LICENSE-2.0                              *
 *                                                                            *
 *  Unless required by applicable law or agreed to in writing, software       *
 *  distributed under the License is distributed on an "AS IS" BASIS,         *
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *
 *  See the License for the specific language governing permissions and       *
 *  limitations under the License.                                            *
 ******************************************************************************/


package com.oltpbenchmark.api;


import com.oltpbenchmark.util.SocketFactoryTcpNoDelay;
import okhttp3.*;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.net.SocketFactory;
import java.io.IOException;

import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;

import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Wrapper Class for SQL Statements
 * @author pavlo
 */
public final class RESTStmt {
    private static final Logger LOG = Logger.getLogger(RESTStmt.class);
    //private static final HttpClient client = HttpClient.newBuilder()..version(HttpClient.Version.HTTP_2).build();
//    private static final SocketConfig socketConfig = SocketConfig.custom()
//            .setSoKeepAlive(true)
//            .setTcpNoDelay(true)
//            .build();
//    private static final IOReactorConfig reactorConfig = IOReactorConfig.custom()
//            .setTcpNoDelay(true)
//            .setIoThreadCount(200)
//            .setSoReuseAddress(true)
//            .build();
//
//    private static final CloseableHttpAsyncClient httpClient = java.net.http.HttpClient. HttpAsyncClients.custom().
//            setDefaultIOReactorConfig(reactorConfig)
//            .build();

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
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


    private static final Pattern SUBSTITUTION_PATTERN = Pattern.compile("\\?\\?");

    private String orig_uri;
    private String final_uri;


    /**
     * Constructor
     * @param uri

     */
    public RESTStmt(String uri) {
        LOG.debug("Initialised: "+uri);


        this.orig_uri = uri;
    }

    private final String getFinalURI(String... parameters) {


        this.final_uri=this.orig_uri;
        for(int i=0;i< parameters.length;i++) {
            this.final_uri=this.final_uri.replace("["+i+"]",parameters[i]);
        }
        this.final_uri=this.final_uri.replace(" ","%20");
        LOG.debug("Resolved: "+this.final_uri);
        return (this.final_uri);
    }

    public static String executeSync(String url) throws IOException {

        //LOG.debug("Calling: " + url);

        Request request = new Request.Builder()
                .url(url)
                .build();

        var call = client.newCall(request);

        try(var response = call.execute()) {
            var body = response.code();
//        var res = body.string();
            //body.close();
            response.close();
//        return res;
        }

        return "";
        //return client.newCall(request).execute().body().string();

    }

    public static CompletableFuture<String> execute(String url) {

        LOG.debug("Calling: " + url);

        Request request = new Request.Builder()
                .url(url)
                .build();

        CompletableFuture<String> future = new CompletableFuture<>();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful())
                        future.completeExceptionally(new IOException("Unexpected code " + response));

                    future.complete(responseBody.string());
                }
            }
        });
        return future;
    }

    public CompletableFuture<String> execute(String... parameters) {
        return this.execute(this.getFinalURI(parameters));
    }


    
    protected final String getOriginalURI() {
        return (this.orig_uri);
    }

    @Override
    public String toString() {
        return "URI{" + this.final_uri + "}";
    }
    
}
