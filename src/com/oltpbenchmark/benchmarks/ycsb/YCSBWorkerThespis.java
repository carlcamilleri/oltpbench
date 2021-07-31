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

package com.oltpbenchmark.benchmarks.ycsb;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.Procedure.UserAbortException;
import com.oltpbenchmark.api.TransactionType;
import com.oltpbenchmark.api.Worker;
import com.oltpbenchmark.benchmarks.ycsb.procedures.*;
import com.oltpbenchmark.distributions.CounterGenerator;
import com.oltpbenchmark.distributions.ZipfianGenerator;
import com.oltpbenchmark.types.TransactionStatus;
import com.oltpbenchmark.util.TextGenerator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * YCSBWorker Implementation
 * I forget who really wrote this but I fixed it up in 2016...
 * @author pavlo
 *
 */
public class YCSBWorkerThespis extends Worker<YCSBBenchmark> {

    private ZipfianGenerator readRecord;
    private static CounterGenerator insertRecord;
    private ZipfianGenerator randScan;

    private final char data[] = new char[YCSBConstants.FIELD_SIZE];
    private final String params[] = new String[YCSBConstants.NUM_FIELDS]; 
    private final String results[] = new String[YCSBConstants.NUM_FIELDS];
    
    private final UpdateRecordThespis procUpdateRecord;
    private final ScanRecord procScanRecord;
    private final ReadRecordThespis procReadRecord;
    private final ReadModifyWriteRecord procReadModifyWriteRecord;
    private final InsertRecordThespis procInsertRecord;
    private final DeleteRecord procDeleteRecord;

    private final String thespisUrl;

    public YCSBWorkerThespis(YCSBBenchmark benchmarkModule, int id, int init_record_count, String thespisUrl) {
        super(benchmarkModule, id,false);
        readRecord = new ZipfianGenerator(init_record_count);// pool for read keys
        randScan = new ZipfianGenerator(YCSBConstants.MAX_SCAN);

        if (insertRecord == null) {
            synchronized (YCSBWorkerThespis.class) {
                // We must know where to start inserting
                if (insertRecord == null) {
                    insertRecord = new CounterGenerator(init_record_count);
                }
            } // SYNCH
        }
        
        // This is a minor speed-up to avoid having to invoke the hashmap look-up
        // everytime we want to execute a txn. This is important to do on 
        // a client machine with not a lot of cores
        this.procUpdateRecord = this.getProcedure(UpdateRecordThespis.class);
        this.procScanRecord = this.getProcedure(ScanRecord.class);
        this.procReadRecord = this.getProcedure(ReadRecordThespis.class);
        this.procReadModifyWriteRecord = this.getProcedure(ReadModifyWriteRecord.class);
        this.procInsertRecord = this.getProcedure(InsertRecordThespis.class);
        this.procDeleteRecord = this.getProcedure(DeleteRecord.class);
        this.thespisUrl = thespisUrl;
    }

    @Override
    protected TransactionStatus executeWork(TransactionType nextTrans) throws UserAbortException, SQLException {
        Class<? extends Procedure> procClass = nextTrans.getProcedureClass();
        
        if (procClass.equals(DeleteRecord.class)) {
            deleteRecord();
        } else if (procClass.equals(InsertRecordThespis.class)) {
            insertRecord();
        } else if (procClass.equals(ReadModifyWriteRecord.class)) {
            readModifyWriteRecord();
        } else if (procClass.equals(ReadRecordThespis.class)) {
            readRecord();
        } else if (procClass.equals(UpdateRecordThespis.class)) {
            updateRecord();
        }
        else if (procClass.equals(ScanRecord.class)) {
            scanRecord();
        }
        if(conn!=null)
            conn.commit();
        return (TransactionStatus.SUCCESS);
    }

    private void updateRecord() throws SQLException {
        assert (this.procUpdateRecord!= null);
        int keyname = readRecord.nextInt();
        this.buildParameters();
        this.procUpdateRecord.run(this.thespisUrl, keyname, this.params);
    }

    private void scanRecord() throws SQLException {
        assert (this.procScanRecord != null);
        int keyname = readRecord.nextInt();
        int count = randScan.nextInt();
        this.procScanRecord.run(conn, keyname, count, new ArrayList<String[]>());
    }

    private void readRecord() throws SQLException {
        assert (this.procReadRecord != null);
        int keyname = readRecord.nextInt();
        this.procReadRecord.run(this.thespisUrl, keyname, this.results);

        //LOG.info(String.join(",", Arrays.asList(this.results)));
    }

    private void readModifyWriteRecord() throws SQLException {
        assert (this.procReadModifyWriteRecord != null);
        int keyname = readRecord.nextInt();
        this.buildParameters();
        this.procReadModifyWriteRecord.run(conn, keyname, this.params, this.results);
    }

    private void insertRecord() throws SQLException {
        assert (this.procInsertRecord != null);
        int keyname = insertRecord.nextInt();
        this.buildParameters();
        this.procInsertRecord.run(this.thespisUrl, keyname, this.params);
    }

    private void deleteRecord() throws SQLException {
        assert (this.procDeleteRecord != null);
        int keyname = readRecord.nextInt();
        this.procDeleteRecord.run(conn, keyname);
    }

    private void buildParameters() {
        Random rng = rng();
        for (int i = 0; i < this.params.length; i++) {
            this.params[i] = new String(TextGenerator.randomFastChars(rng, this.data));
        } // FOR
    }
}
