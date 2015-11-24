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

## Deployment manual

#### On Cloud Foundry 

To install AA backend in your cloud foundry space login to CF and execute:

1. Create instances with specified name for each of required services from marketplace:

    * CDH broker with name mycdh
    * Zookeeper broker with name myzookeeper
    
1. Create following user-provided services with properties filled with real values:

   Provide ${ADDRESS} with url to your dashboard instance
   
        cf cups dashboard-endpoint-ups -p "{\"host\":\"${ADDRESS}\"}"
   
   If you have deployed dashboard already then you should have user and password set previously. This is only reminder
   that same service need to be attached to backend
   
        cf cups installer-backend-user-credentials-ups -p "{\"username\":\"${USER}\",\"password\":\"${PASSWORD}\"}"
        
1. make build
1. ./cf-deploy.sh
1. Check logs and wait for application start.
