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

import com.intel.databackend.config.ZookeeperCredentialsProvider;
import com.intel.databackend.datastructures.Observation;
import com.intel.databackend.exceptions.VcapEnvironmentException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.ColumnPrefixFilter;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hadoop.hbase.HbaseTemplate;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.data.hadoop.hbase.TableCallback;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;


public class DataHbaseDao implements DataDao {

    private HbaseTemplate hBaseTemplate;

    private static final Logger logger = LoggerFactory.getLogger(DataHbaseDao.class);
    private static final int ONE_YEAR_IN_SECONDS = 60 * 60 * 24 * 365;
    private final String tableName;
    private final byte[] tableNameBytes;
    private final String zookeeperQuorum;
    private final String zookeeperPort;
    private static final int CONNECTION_TIMEOUT = 4000;
    private static final String DEVICE_MEASUREMENT = "_DEVICE_MEASUREMENT";

    private Configuration getConfig() {
        Configuration conf = HBaseConfiguration.create();
        conf.setInt("timeout", CONNECTION_TIMEOUT);
        conf.set("hbase.zookeeper.quorum", zookeeperQuorum);
        conf.set("hbase.zookeeper.property.clientPort", zookeeperPort);
        return conf;
    }

    @Autowired
    public DataHbaseDao(ZookeeperCredentialsProvider zookeeperCredentialsProvider, String hbasePrefix) {
        try {
            String[] hosts = zookeeperCredentialsProvider.getZookeeperHosts();
            String[] host_and_port = hosts[0].split(":");
            logger.info("Creating HBase. Zookeeper: " + host_and_port[0] + "@" + host_and_port[1]);

            this.zookeeperQuorum = host_and_port[0];
            this.zookeeperPort = host_and_port[1];
            this.tableName = hbasePrefix.toUpperCase() + DEVICE_MEASUREMENT;
            this.tableNameBytes = Bytes.toBytes(tableName);
            hBaseTemplate = new HbaseTemplate(getConfig());

        } catch (VcapEnvironmentException ex) {
            throw new IllegalArgumentException("Unable to read zookeeper configuration from VCAP_SERVICES Zookeeper.");
        }
    }

    @PostConstruct
    public boolean createTables() throws IOException {
        Configuration conf = getConfig();
        HBaseAdmin admin = null;
        logger.info("Try to create {} in HBase.", tableName);
        try {
            admin = new HBaseAdmin(conf);

            if (!admin.tableExists(tableNameBytes)) {
                HTableDescriptor table = new HTableDescriptor(TableName.valueOf(tableNameBytes));
                HColumnDescriptor family = new HColumnDescriptor(Columns.BYTES_COLUMN_FAMILY);
                family.setTimeToLive(ONE_YEAR_IN_SECONDS);
                table.addFamily(family);
                admin.createTable(table);
                logger.info("Table {} created in HBase.", tableName);
                return true;
            } else {
                HTableDescriptor table = admin.getTableDescriptor(tableNameBytes);
                for(HColumnDescriptor family: table.getColumnFamilies()) {
                    family.setTimeToLive(ONE_YEAR_IN_SECONDS);
                    admin.modifyTable(tableNameBytes, table);
                    //admin.majorCompact(tableNameBytes);    // Run with care - can take time to proceed
                    logger.info("Setting TTL for column family: {}", family.getNameAsString());
                }
                logger.info("Table {} already exists in HBase.", tableName);
                return false;
            }
        } catch (ZooKeeperConnectionException e) {
            logger.warn("Initialization of HBase table failed.", e);
            return false;
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

        return hBaseTemplate.execute(tableName, new TableCallback<Boolean>() {
            @Override
            public Boolean doInTable(HTableInterface table) throws Throwable {
                List<Put> puts = new ArrayList<Put>();
                for (Observation obs : observations) {
                    puts.add(getPutForObservation(obs));
                }
                table.put(puts);
                return true;
            }
        });
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
        Configuration conf = getConfig();
        HTable table = new HTable(conf, tableName);

        Scan scan = new HbaseScanManager(accountId, componentId)
                .create(start, stop)
                .setFilter(new ColumnPrefixFilter(Columns.BYTES_ATTRIBUTE_COLUMN_PREFIX))
                .getScan();

        Set<String> attributeNames = RetrieveAttributeNames(table, scan);
        return attributeNames.toArray(new String[attributeNames.size()]);
    }

    private Observation[] getObservations(final String accountId, final String componentId, final Boolean gps, final String[] attributes, Scan scan) {
        List<Observation> observations = hBaseTemplate.find(tableName, scan, new RowMapper<Observation>() {
            @Override
            public Observation mapRow(Result result, int rowNum) {
                Observation observation = new Observation();
                AddBasicInformation(observation, accountId, componentId, result);
                AddAdditionalInformation(observation, gps, attributes, result);
                return observation;
            }
        });
        return observations.toArray(new Observation[observations.size()]);
    }

    private Put getPutForObservation(Observation o) {
        Put put = new Put(Bytes.toBytes(o.getAid() + '\0' + o.getCid() + '\0' + DataFormatter.zeroPrefixedTimestamp(o.getOn())));
        put.add(Columns.BYTES_COLUMN_FAMILY, Columns.BYTES_DATA_COLUMN, Bytes.toBytes(o.getValue()));
        if (o.getLoc() != null) {
            for (int i = 0; (i < o.getLoc().size() && i < Columns.GPS_COLUMN_SIZE); i++) {
                put.add(Columns.BYTES_COLUMN_FAMILY, Bytes.toBytes(DataFormatter.gpsValueToString(i)), Bytes.toBytes(o.getLoc().get(i).toString()));
            }
        }
        Map<String, String> attributes = o.getAttributes();
        if (attributes != null) {
            for (String k : attributes.keySet()) {
                put.add(Columns.BYTES_COLUMN_FAMILY, Bytes.toBytes(Columns.ATTRIBUTE_COLUMN_PREFIX + k), Bytes.toBytes(attributes.get(k)));
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

    private Set<String> RetrieveAttributeNames(HTable table, Scan scan) throws IOException {
        Set<String> attributes = new HashSet<>();
        ResultScanner scanner = table.getScanner(scan);
        try {
            for (Result result = scanner.next(); result != null; result = scanner.next()) {
                List<Cell> cells = result.listCells();
                for (Cell cell : cells) {
                    String attrName = DataFormatter.getAttrNameFromCell(cell);
                    attributes.add(attrName);
                }
            }
        } finally {
            logger.debug("Closing scanner!");
            scanner.close();
        }
        return attributes;
    }
}
