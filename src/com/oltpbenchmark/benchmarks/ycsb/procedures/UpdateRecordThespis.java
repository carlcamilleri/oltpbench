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

package com.oltpbenchmark.benchmarks.ycsb.procedures;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.RESTStmt;
import com.oltpbenchmark.api.SQLStmt;
import com.oltpbenchmark.benchmarks.ycsb.YCSBConstants;
import com.oltpbenchmark.util.json.JSONException;
import com.oltpbenchmark.util.json.JSONObject;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class UpdateRecordThespis extends Procedure {

    private static final ExecutorService pool = Executors.newFixedThreadPool(256);
    private static final Logger LOG = Logger.getLogger(UpdateRecordThespis.class);


    public void run(String thespisUrl, int keyname, String vals[]) throws SQLException {
        try {
            var url = thespisUrl + "api/query/update/ycsb/usertable?w=ycsb_key:[0]&" +
                    IntStream.rangeClosed(1, YCSBConstants.NUM_FIELDS)
                            .mapToObj(i -> "u=SET(field" + i + ",["+i+"])")
                            .collect(Collectors.joining("&"));


            var updateStmtUri = new RESTStmt(url);


            var futUpdateUserTable =
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            return updateStmtUri.executeSync(Stream.concat(Stream.of(Integer.toString(keyname)), Stream.of(vals)).toArray(String[]::new));
                        } catch (UnsupportedEncodingException e) {
                            throw new CompletionException(e);
                        }
                    }, pool);

            var resFutures = Stream.of(futUpdateUserTable)
                    .map(CompletableFuture::join).collect(Collectors.toList());

            var resUpdateUserTable = resFutures.get(0);

            boolean isSuccess = false;

            isSuccess = new JSONObject(resUpdateUserTable).getBoolean("isSuccess");


            if (!isSuccess)
                throw new RuntimeException("Invalid response: " + resUpdateUserTable);
        } catch (UserAbortException | JSONException userEx) {
            LOG.error("Caught an expected error in New Order");
            throw new RuntimeException(userEx);
        }
    }


}
