1. Make sure Streams QSE is running
2. Install and launch Redis on Streams QSE
3. Clone repository
4. Checkout feature/dashboard
5. `cd dashboard`
6. Edit 'server/properties.json' and enter Streams and Redis data (Kafka section is not being used)
7. `cd services_src`
8. `gradle buildPCPToolkit build`
9. `cd ../ui`
10. Run `npm install`
11. `cd ../server`
12. Run `npm install`
13. Run `gradle build` // this will copy the SABs into the 'server/services' directory
15. Almost there...
16. In the Streams domain, create an application configuration called "patient_control_plane" and add the following property:
    name: "connection"
    value: [redis_hostname]:[redis_port] (i.e. 192.168.216.100:6379)
17. In the Streams domain, create an application configuration called "health-kafka" and add the following property:
    name: "bootstrap.servers"
    value: [kafka_hostname]:[kafka_port] (i.e. mykafka.ibm.com:9091)
18. In a new terminal, navigate to 'server' and run 'node server.js'
19. In a new terminal, navigate to 'ui' and run 'npm start'
20. In the browser, click on 'Admin' and then click the 'Start Services' button
