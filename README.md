# Real-Time-Data-Analytics
Interactive analytics for Reddit | Real-Time data analysis.
The purpose of this project was to get metrics about Reddit posts in Real time using various technologies such as Angular, Apache Kafka, Spring KStream, Apache Spark, Spring Kafka Spring Webflux with Reactor.  
It contains five main components:
* `client` which is the front end app using Angular.
* a web service (`Reddit-producer`) that calls the Reddit API and gets posts. it then sends it to a Kafka topic. Here is an example of a Rest call to the reddit API: 
![image](https://user-images.githubusercontent.com/47919190/156658903-166892c5-f94c-4a51-b39f-1f60f5dee00d.png)

* two Consumers (`Kafka-Stream-Consumer` and `Spark-Consumer`) that are basically stream processors. These get the data from Kafka as a Stream, and process it in Real-time, producing  metrics and statistics that are put back in a Kafka Topic `metrics` to be consumed later.
* `Spring-Kafka-Reactive-Backend` is a service that is connected to the reddit metrics topic and waits sends it to the frontend using a websocket.
## Architecture
![image](https://user-images.githubusercontent.com/47919190/156658469-23252980-1f6e-456e-88b0-c3e4a87e8535.png)
Note: Hive wasn't used in this project.
## Screenshots
![image](https://user-images.githubusercontent.com/47919190/156658551-a5ff827d-ac48-4ff3-9b45-05af5ee5b6b7.png)
![image](https://user-images.githubusercontent.com/47919190/156658619-44856fba-f872-48f0-b1ea-808850b6b838.png)
![image](https://user-images.githubusercontent.com/47919190/156658649-c38448d2-3a5e-4aaf-9150-e931b3d9a3f6.png)
![image](https://user-images.githubusercontent.com/47919190/156658663-35620926-5f6c-4b6a-ba9a-af3e1d9707ef.png)
![image](https://user-images.githubusercontent.com/47919190/156658680-11740e82-36c8-4297-963e-adf63bcd86dd.png)
![image](https://user-images.githubusercontent.com/47919190/156658705-7c65c13f-e8bd-4341-aff1-3e0982d1bc8e.png)
![image](https://user-images.githubusercontent.com/47919190/156658745-84e7b694-ed5e-4b75-a80c-be0ee597babf.png)
![image](https://user-images.githubusercontent.com/47919190/156658765-62d889c4-9a05-462f-8171-1a10e02cff9f.png)

