package com.intel.databackend.datasources.hbase;

import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * Copyright (c) 2015 Intel Corporation
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class TableManager {

    private static final Logger logger = LoggerFactory.getLogger(TableManager.class);
    private static final int ONE_YEAR_IN_SECONDS = 60 * 60 * 24 * 365;

    private final TableName tableName;
    private final Admin admin;

    public TableManager(Admin admin, TableName tableName) {
        this.tableName = tableName;
        this.admin = admin;
    }

    @PostConstruct
    public boolean createTables() throws IOException {
        logger.info("Try to create {} in HBase.", tableName);
        try {
            boolean isCreated = false;
            HTableDescriptor table;
            if (!admin.tableExists(tableName)) {
                table = new HTableDescriptor(tableName);
                HColumnDescriptor family = new HColumnDescriptor(Columns.BYTES_COLUMN_FAMILY);
                table.addFamily(family);
                admin.createTable(table);
                logger.info("Table {} created in HBase.", tableName);
                isCreated = true;
            } else {
                table = admin.getTableDescriptor(tableName);
                logger.info("Table {} already exists in HBase.", tableName);
            }
            setTTLforAllTables(table);
            return isCreated;
        } catch (IOException e) {
            logger.warn("Initialization of HBase table failed.", e);
            return false;
        } finally {
            if (admin != null) {
                admin.close();
            }
        }
    }

    private void setTTLforAllTables(HTableDescriptor table) throws IOException {
        for(HColumnDescriptor family: table.getColumnFamilies()) {
            family.setTimeToLive(ONE_YEAR_IN_SECONDS);
            admin.modifyTable(tableName, table);
            logger.info("Setting TTL for column family: {}", family.getNameAsString());
        }
    }
}
