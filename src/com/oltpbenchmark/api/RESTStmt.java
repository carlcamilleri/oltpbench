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

import org.apache.log4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wrapper Class for SQL Statements
 * @author pavlo
 */
public final class RESTStmt {
    private static final Logger LOG = Logger.getLogger(RESTStmt.class);
    private static final HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();

    private static final Pattern SUBSTITUTION_PATTERN = Pattern.compile("\\?\\?");

    private String orig_uri;
    private String final_uri;


    /**
     * Constructor
     * @param uri

     */
    public RESTStmt(String uri) {

        this.orig_uri = uri;
    }

    private final String getFinalURI(String... parameters) {


        this.final_uri=this.orig_uri;
        for(int i=0;i< parameters.length;i++) {
            this.final_uri=this.final_uri.replace("["+i+"]",parameters[i]);
        }
        this.final_uri=this.final_uri.replace(" ","%20");
        return (this.final_uri);
    }

    public Future<String> execute(String... parameters){
        this.getFinalURI(parameters);
        HttpRequest req = HttpRequest.newBuilder(URI.create(this.final_uri)).GET().build();

        CompletableFuture<HttpResponse<String>> response = client.sendAsync(req, HttpResponse.BodyHandlers.ofString());
        return response.thenApply(r-> r.body());
    }
    
    protected final String getOriginalURI() {
        return (this.orig_uri);
    }

    @Override
    public String toString() {
        return "URI{" + this.final_uri + "}";
    }
    
}
