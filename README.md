PROJECT NOT UNDER ACTIVE MANAGEMENT

This project will no longer be maintained by Intel.

Intel has ceased development and contributions including, but not limited to, maintenance, bug fixes, new releases, or updates, to this project.  

Intel no longer accepts patches to this project.

If you have an ongoing need to use this project, are interested in independently developing it, or would like to maintain patches for the open source software community, please create your own fork of this project.  

Contact: webadmin@linux.intel.com
# IoT Analytics Advanced Analytics Backend

Advanced Analytics Backend is an app which provides API to ingest observations to HBase and retrieve them.
It is a part of IoT Analytics solution and requires dp-dashboard to manage metadata about users, accounts, devices and their components.

When you install this application, dashboard will get an ability to support data ingestion.

You can find dashboard API documentation for Data API on pages:

1. [Data API](https://github.com/enableiot/iotkit-api/wiki/Data-API)
1. [Advanced Data Inquiry API](https://github.com/enableiot/iotkit-api/wiki/Advanced-Data-Inquiry)

## Requirements 

1. Gradle 2.4
2. Java 1.8
3. Cloud Foundry CLI and Trusted Analytics Platform account (https://github.com/trustedanalytics)


## Deployment manual

#### On Trusted Analytics Platform (https://github.com/trustedanalytics)

To install backend in your Trusted Analytics Platform space, login into TAP and execute:

1. Create instances with specified name for each of required services from marketplace:

    * Hbase broker with name myhbase
    * Kafka broker with name mykafka
    * Zookeeper broker with name myzookeeper
    
1. Create following user-provided services with properties filled with real values:

        cf cups kafka-ups -p "{\"topic\":\"example_topic_name\",\"enabled\":true,\"partitions\":1,\"replication\":1,\"timeout_ms\":10000}"
        cf cs kerberos shared kerberos-service
        
1. ./cf-deploy.sh
1. Check logs and wait for application start.
