/**
 * Copyright (c) 2015 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intel.databackend.datasources.hbase;

import com.intel.databackend.datastructures.Observation;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.ColumnPrefixFilter;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.trustedanalytics.hadoop.config.client.helper.Hbase;

import javax.annotation.PostConstruct;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.*;


public class DataHbaseDao implements DataDao {

    private static final Logger logger = LoggerFactory.getLogger(DataHbaseDao.class);
    private static final int ONE_YEAR_IN_SECONDS = 60 * 60 * 24 * 365;
    private final String tableName;
    private final byte[] tableNameBytes;
    private static final String DEVICE_MEASUREMENT = "_DEVICE_MEASUREMENT";

    private Connection getHbaseConnection() throws IOException {
        try {
            return  Hbase.newInstance().createConnection();
        } catch (LoginException e) {
            logger.error("Unable to login into hbase", e);
            throw new IOException(e);
        }
    }

    private Table getHbaseTable(Connection conn) throws IOException {
        return conn.getTable(TableName.valueOf(tableName));
    }

    @Autowired
    public DataHbaseDao(String hbasePrefix) {
        logger.info("Creating HBase. Zookeeper: ");

        this.tableName = hbasePrefix.toUpperCase() + DEVICE_MEASUREMENT;
        this.tableNameBytes = Bytes.toBytes(tableName);
    }

    @PostConstruct
    public boolean createTables() throws IOException {
        Admin admin = null;
        logger.info("Try to create {} in HBase.", tableName);
        try {
            admin = getHbaseConnection().getAdmin();

            if (!admin.tableExists(TableName.valueOf(tableNameBytes))) {
                HTableDescriptor table = new HTableDescriptor(TableName.valueOf(tableNameBytes));
                HColumnDescriptor family = new HColumnDescriptor(Columns.BYTES_COLUMN_FAMILY);
                family.setTimeToLive(ONE_YEAR_IN_SECONDS);
                table.addFamily(family);
                admin.createTable(table);
                logger.info("Table {} created in HBase.", tableName);
                return true;
            } else {
                HTableDescriptor table = admin.getTableDescriptor(TableName.valueOf(tableNameBytes));
                for(HColumnDescriptor family: table.getColumnFamilies()) {
                    family.setTimeToLive(ONE_YEAR_IN_SECONDS);
                    admin.modifyTable(TableName.valueOf(tableNameBytes), table);
                    logger.info("Setting TTL for column family: {}", family.getNameAsString());
                }
                logger.info("Table {} already exists in HBase.", tableName);
                return false;
            }
        } catch (IOException e) {
            logger.warn("Initialization of HBase table failed.", e);
            return false;
        } finally {
            if (admin != null) {
                admin.close();
            }
        }
    }

    @Override
    public boolean put(final Observation[] observations) {

        try (Connection conn = getHbaseConnection();Table table = getHbaseTable(conn)) {

            List<Put> puts = new ArrayList<Put>();
            for (Observation obs : observations) {
                puts.add(getPutForObservation(obs));
            }

            table.put(puts);
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    @Override
    public Observation[] scan(String accountId, String componentId, long start, long stop, Boolean gps, String[] attributes) {
        logger.debug("Scanning HBase: acc: {} cid: {} start: {} stop: {} gps: {}", accountId, componentId, start, stop, gps);

        Scan scan = new HbaseScanManager(accountId, componentId).create(start, stop).askForData(gps, attributes).getScan();
        return getObservations(accountId, componentId, gps, attributes, scan);
    }

    @Override
    public Observation[] scan(String accountId, String componentId, long start, long stop, Boolean gps, String[] attributes, boolean forward, int limit) {
        logger.debug("Scanning HBase: acc: {} cid: {} start: {} stop: {} gps: {} with limit: {}", accountId, componentId, start, stop, gps, limit);
        HbaseScanManager scanManager = new HbaseScanManager(accountId, componentId);
        if (forward) {
            scanManager.create(start, stop);
        } else {
            scanManager.create(stop, start).setReversed();
        }
        scanManager.askForData(gps, attributes);
        
        logger.debug("Scanning with limit: {}", limit);
        Scan scan = scanManager.setCaching(limit)
                .setFilter(new PageFilter(limit))
                .getScan();
        return getObservations(accountId, componentId, gps, attributes, scan);
    }

    @Override
    public String[] scanForAttributeNames(String accountId, String componentId, long start, long stop) throws IOException {

        logger.debug("Scanning HBase: acc: {} cid: {} start: {} stop: {}", accountId, componentId, start, stop);

        Scan scan = new HbaseScanManager(accountId, componentId)
                .create(start, stop)
                .setFilter(new ColumnPrefixFilter(Columns.BYTES_ATTRIBUTE_COLUMN_PREFIX))
                .getScan();

        Set<String> attributeNames = RetrieveAttributeNames(scan);
        return attributeNames.toArray(new String[attributeNames.size()]);
    }

    private Observation[] getObservations(final String accountId, final String componentId, final Boolean gps, final String[] attributes, Scan scan) {
        try (Connection conn = getHbaseConnection(); ResultScanner scanner = getHbaseTable(conn).getScanner(scan)) {
            List<Observation> observations = new ArrayList<>();
            for (Result result: scanner) {
                Observation observation = new Observation();
                AddBasicInformation(observation, accountId, componentId, result);
                AddAdditionalInformation(observation, gps, attributes, result);
                observations.add(observation);
            }
            return observations.toArray(new Observation[observations.size()]);
        } catch (IOException ex) {
            logger.error("Unable to find observation in hbase", ex);
            return null;
        }
    }

    private Put getPutForObservation(Observation o) {
        Put put = new Put(Bytes.toBytes(o.getAid() + '\0' + o.getCid() + '\0' + DataFormatter.zeroPrefixedTimestamp(o.getOn())));
        put.addColumn(Columns.BYTES_COLUMN_FAMILY, Columns.BYTES_DATA_COLUMN, Bytes.toBytes(o.getValue()));
        if (o.getLoc() != null) {
            for (int i = 0; (i < o.getLoc().size() && i < Columns.GPS_COLUMN_SIZE); i++) {
                put.addColumn(Columns.BYTES_COLUMN_FAMILY, Bytes.toBytes(DataFormatter.gpsValueToString(i)), Bytes.toBytes(o.getLoc().get(i).toString()));
            }
        }
        Map<String, String> attributes = o.getAttributes();
        if (attributes != null) {
            for (String k : attributes.keySet()) {
                put.addColumn(Columns.BYTES_COLUMN_FAMILY, Bytes.toBytes(Columns.ATTRIBUTE_COLUMN_PREFIX + k), Bytes.toBytes(attributes.get(k)));
            }
        }
        return put;
    }

    private void AddBasicInformation(Observation obs, String accountId, String componentId, Result result) {
        String key = Bytes.toString(result.getRow());
        String value = Bytes.toString(result.getValue(Columns.BYTES_COLUMN_FAMILY, Bytes.toBytes(Columns.DATA_COLUMN)));
        obs.setCid(componentId);
        obs.setAid(accountId);
        obs.setOn(DataFormatter.getTimeFromKey(key)); //0L;
        obs.setValue(value);
        obs.setAttributes(new HashMap<String, String>());
    }

    private void AddAdditionalInformation(Observation obs, Boolean gps, String[] attributes, Result result) {
        if (attributes != null) {
            AddAttributesData(attributes, result, obs);
        }
        if (gps != null && gps) {
            AddLocationData(result, obs);
        }
    }

    private void AddAttributesData(String[] attributes, Result result, Observation observation) {
        for (String a : attributes) {
            String attribute = Bytes.toString(result.getValue(Columns.BYTES_COLUMN_FAMILY, Bytes.toBytes(Columns.ATTRIBUTE_COLUMN_PREFIX + a)));
            observation.getAttributes().put(a, attribute);
        }
    }

    private void AddLocationData(Result result, Observation obs) {
        try {
            String[] coordinate = new String[Columns.GPS_COLUMN_SIZE];
            obs.setLoc(new ArrayList<Double>());
            for (int i = 0; i < Columns.GPS_COLUMN_SIZE; i++) {
                coordinate[i] = Bytes.toString(result.getValue(Columns.BYTES_COLUMN_FAMILY, Bytes.toBytes(DataFormatter.gpsValueToString(i))));
                if (coordinate != null) {
                    obs.getLoc().add(Double.parseDouble(coordinate[i]));
                }
            }
        } catch (Exception e) {
            logger.warn("problem with parsing GPS coords... not a Double?");
        }
    }

    private Set<String> RetrieveAttributeNames(Scan scan) throws IOException {
        Set<String> attributes = new HashSet<>();
        try (Connection conn = getHbaseConnection(); ResultScanner scanner = getHbaseTable(conn).getScanner(scan)) {
            for (Result result = scanner.next(); result != null; result = scanner.next()) {
                List<Cell> cells = result.listCells();
                for (Cell cell : cells) {
                    String attrName = DataFormatter.getAttrNameFromCell(cell);
                    attributes.add(attrName);
                }
            }
        }
        return attributes;
    }
}
