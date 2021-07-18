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
import com.oltpbenchmark.benchmarks.tpcc.TPCCConstants;
import com.oltpbenchmark.benchmarks.tpcc.procedures.NewOrderThespis;
import com.oltpbenchmark.benchmarks.ycsb.YCSBConstants;
import com.oltpbenchmark.util.json.JSONArray;
import com.oltpbenchmark.util.json.JSONException;
import com.oltpbenchmark.util.json.JSONObject;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReadRecordThespis extends Procedure{

    private static final ExecutorService pool = Executors.newFixedThreadPool(256);
    private static final Logger LOG = Logger.getLogger(ReadRecordThespis.class);



	//FIXME: The value in ysqb is a byteiterator
    public void run(String thespisUrl, int keyname, String results[]) throws SQLException {
        try {
        var readStmtUri = new RESTStmt(thespisUrl+"api/query/select/ycsb/USERTABLE?w=ycsb_key:[0]");

//        var futGetUserTable =
//                CompletableFuture.supplyAsync(() -> {
//                    return readStmtUri.executeSync(new String[]{String.valueOf(keyname)});
//                }, pool);
//
//        var resFutures = Stream.of(futGetUserTable)
//                .map(CompletableFuture::join).collect(Collectors.toList());
//
//        var resGetUserTable = resFutures.get(0);

            var resGetUserTable = readStmtUri.executeSync(new String[]{String.valueOf(keyname)});;

        JSONArray jarrGetCust = null;

            jarrGetCust = new JSONObject(resGetUserTable).getJSONArray("entities");


        if(jarrGetCust.length()!=1)
            throw new RuntimeException("Invalid response: "+resGetUserTable);

        var userTableObj = jarrGetCust.getJSONObject(0).getJSONObject("data");



        for (int i = 1; i <= YCSBConstants.NUM_FIELDS; i++)
            results[i-1] = userTableObj.getString("field"+ i);



        } catch(UserAbortException | JSONException userEx)
        {
            LOG.error("Caught an expected error in New Order");
            throw new RuntimeException(userEx);
        }
    }

}
